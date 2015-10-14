/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package module.entities.NameFinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Sardianos
 */
public class DB {

    public static Connection connection = null;
    static Locale locale = new Locale("el-GR");
    static SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy, HH:mm", locale);
    public static int batchSize = 0;

    /**
     * Initiates the database connection.
     *
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void init() throws IOException {
        if (connection == null) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
                e.printStackTrace();
                return;
            }

            try {
                BufferedReader br = null;
                String line = "";
                String splitBy = "=";
                String ip_address = null;
                String user = null;
                String pass = null;
//                configFile = "files/config.txt";
                try {
                    br = new BufferedReader(new FileReader(Config.configFile));
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("IP_ADDRESS")) {
                            String[] lineParts = line.split(splitBy, 2);
                            ip_address = lineParts[1];
                        } else if (line.startsWith("USERNAME")) {
                            String[] lineParts = line.split(splitBy, 2);
                            user = lineParts[1];
                        } else if (line.startsWith("PASSWORD")) {
                            String[] lineParts = line.split(splitBy, 2);
                            pass = lineParts[1];
                        } else if (line.startsWith("LEXICONS_LANG")) {
                            String[] lineParts = line.split(splitBy, 2);
                            Config.lang = lineParts[1];
                        }
                    }
                } catch (FileNotFoundException e) {
                }
                String DB_url = "jdbc:mysql://" + ip_address + "?characterEncoding=UTF-8";
                connection = DriverManager.getConnection(DB_url, user, pass);
            } catch (SQLException e) {
                System.out.println("Connection Failed! Check output console.");
                e.printStackTrace();
                return;
            }
            if (connection != null) {
                System.out.println("Connection to the database created succesfully!");
            } else {
                System.out.println("Failed to make connection!");
            }
        }
    }

    public static void close() throws SQLException {
        if (connection != null) {
            connection.close();
            Connection connection = null;
        }
    }

    /**
     * 
     *
     * @return
     * @throws java.sql.SQLException
     */
    public static TreeMap<Integer, String> GetAnnotatedData() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM assignmentsdone;");
        TreeMap<Integer, String> dbJsonString = new TreeMap<>();
//        ArrayList<String> dbJsonString = new ArrayList<>();
        while (rs.next()) {
            int json_id = rs.getInt("text_id");
            String json = rs.getString("json_out");
            dbJsonString.put(json_id, json);
        }
        return dbJsonString;
    }

    public static void InsertLexiconEntry(PreparedStatement statement, String lexiconType, String lemma, String entity, String lang) throws SQLException {
        try {
            statement.setString(1, lexiconType);
            statement.setString(2, lemma);
            statement.setString(3, entity);
            statement.setString(4, lang);
            statement.addBatch();
            batchSize++;
            if (batchSize == 1000) {
                statement.executeBatch();
                statement.clearBatch();
                batchSize = 0;
            }
        } catch (SQLException ex) {
            System.err.println("Skipping:" + lemma + " " + entity + " " + ex.getMessage());
        }
    }

    public static void flushBatchLexiconEntry(PreparedStatement statement) throws SQLException {
        try {
            statement.executeBatch();
            statement.clearBatch();
            batchSize = 0;
        } catch (SQLException ex) {
            System.err.println("Skipping:" + ex.getMessage());
        }
    }
    
    public static void InsertJsonLemmas(TreeMap<EntityEntry, Integer> docEntities, int text_id, int jsonKey) throws SQLException {
        String insertSQL = "INSERT INTO json_annotated_lemmas "
                + "(lemma_text,lemma_category,lemma_text_id,lemma_jsonKey,lemma_count) VALUES"
                + "(?,?,?,?,?)";
        PreparedStatement prepStatement = connection.prepareStatement(insertSQL);
        for (Map.Entry<EntityEntry, Integer> ent : docEntities.entrySet()) {

            prepStatement.setString(1, ent.getKey().text);
            prepStatement.setString(2, ent.getKey().category);
            prepStatement.setInt(3, text_id);
            prepStatement.setInt(4, jsonKey);
            prepStatement.setInt(5, ent.getValue().intValue());
            prepStatement.addBatch();
        }
        prepStatement.executeBatch();
        prepStatement.close();

    }

    public static void InsertNewLemma2DB(String lemma, String category) throws SQLException {
        String selectSQL = "SELECT * FROM  WHERE  = ? ";
        PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
        preparedStatement.setString(1, lemma);
        preparedStatement.setString(2, category);
        ResultSet rs = preparedStatement.executeQuery();
        String insertSQL = "INSERT INTO  "
                + "(lemma_text,lemma_category) VALUES"
                + "(?,?)";
        PreparedStatement prepStatement = connection.prepareStatement(insertSQL);
        int id = -1;
        if (rs.next()) {
            id = rs.getInt(1);
        } else {
            prepStatement.setString(1, lemma);
            prepStatement.setString(2, category);
            prepStatement.addBatch();
        }
        prepStatement.executeBatch();
        prepStatement.close();
    }

    public static PreparedStatement GetInsertLexiconEntryStatement() throws SQLException {
//        String insertSQL = "INSERT INTO lexicons_gr "
//                + "(lexicon_type,lexicon_lemma,lexicon_entity,lexicon_lang) VALUES"
//                + "(?,?,?,?)";
//        PreparedStatement prepStatement = connection.prepareStatement(insertSQL);

        String insertSQL = "INSERT INTO lexicons_gr_temp "
                + "(lexicon_type,lexicon_lemma,lexicon_entity,lexicon_lang) VALUES"
                + "(?,?,?,?)";

        return connection.prepareStatement(insertSQL);
    }

    public static void InsertJsonLemmas(String text, String category, int text_id, int jsonKey) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void saveClusterAnnotatedDocuments(TreeMap<Integer, String> docs) throws SQLException {
        String insertSQL = "INSERT INTO assignments_herc (annotator_id,text_id,json_out) VALUES (-1,?,?)";
        PreparedStatement stmt = connection.prepareStatement(insertSQL);
        for (Map.Entry<Integer, String> pair : docs.entrySet()) {
            stmt.setInt(1, pair.getKey());
            stmt.setString(2, pair.getValue());
            stmt.addBatch();
        }
        stmt.executeBatch();
    }

    public static TreeMap<Integer, String> getClusterDocuments(int clid) throws SQLException {
        TreeMap<Integer, String> all = new TreeMap<Integer, String>();
        String selectSQL = "SELECT palo_id,raw_text FROM texts_herc where text_id=" + clid + ";";
        ResultSet rs = connection.createStatement().executeQuery(selectSQL);
        while (rs.next()) {
            int tid = rs.getInt(1);
            String s = rs.getString(2);
            all.put(tid, s);
        }
        return all;
    }

    public static TreeMap<Integer, String> getDemocracitJSON(int consultation_id) throws SQLException {
        TreeMap<Integer, String> all = new TreeMap<Integer, String>();
        String selectSQL1 = "SELECT distinct consultation_id FROM enhancedentities;";
        ResultSet rs1 = connection.createStatement().executeQuery(selectSQL1);
        while (rs1.next()) {
            System.out.println(rs1.getString(1));
        }
        String selectSQL = "SELECT article_id,json_text FROM enhancedentities where consultation_id=" 
                + consultation_id + ";";
        ResultSet rs = connection.createStatement().executeQuery(selectSQL);
        while (rs.next()) {
            int tid = rs.getInt(1);
            String s = rs.getString(2);
            all.put(tid, s);
        }
        return all;
    }

    
    public static TreeMap<Integer, String> getDemocracitArticles(int consId) throws SQLException {
        TreeMap<Integer, String> all = new TreeMap<>();
        String sql = "SELECT id, body "
                + "FROM articles "
                + "WHERE consultation_id = " + consId + " AND id NOT IN (SELECT enhancedentities.article_id FROM enhancedentities);";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
//        PreparedStatement preparedStatement = connection.prepareStatement(sql);
//        preparedStatement.setInt(1, consId);
//        System.out.println(sql);
//        ResultSet rs = preparedStatement.executeQuery();
        Document doc;
        while (rs.next()) {
            int articleID = rs.getInt("id");
            String article_text = rs.getString("body");
            doc = Jsoup.parseBodyFragment(article_text);
            all.put(articleID, doc.text());
        }
        return all;
    }

    public static void initPostgres() throws IOException {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
                e.printStackTrace();
                return;
            }

            try {
                BufferedReader br = null;
                String line = "";
                String splitBy = "=";
                String ip_address = null;
                String user = null;
                String pass = null;
//                configFile = "files/config.txt";
                try {
                    br = new BufferedReader(new FileReader(Config.configFile));
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("IP_ADDRESS")) {
                            String[] lineParts = line.split(splitBy, 2);
                            ip_address = lineParts[1];
                        } else if (line.startsWith("USERNAME")) {
                            String[] lineParts = line.split(splitBy, 2);
                            user = lineParts[1];
                        } else if (line.startsWith("PASSWORD")) {
                            String[] lineParts = line.split(splitBy, 2);
                            pass = lineParts[1];
                        } else if (line.startsWith("LEXICONS_LANG")) {
                            String[] lineParts = line.split(splitBy, 2);
                            Config.lang = lineParts[1];
                        }
                    }
                } catch (FileNotFoundException e) {
                }
                String DB_url = "jdbc:postgresql://" + ip_address;
                connection = DriverManager.getConnection(DB_url, user, pass);
            } catch (SQLException e) {
                System.out.println("Connection Failed! Check output console.");
                e.printStackTrace();
                return;
            }
            if (connection != null) {
                System.out.println("Connection to the database created succesfully!");
            } else {
                System.out.println("Failed to make connection!");
            }
        }
    }

    public static ArrayList<Integer> getConsultationIds() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id FROM consultation;");
        ArrayList<Integer> ids = new ArrayList<>();
        while (rs.next()) {
            ids.add(rs.getInt(1));
        }
        return ids;
    }

    public static void insertJsonResponse(int curConsId, TreeMap<Integer, String> input) throws SQLException {
        try {
            String insertSQL = "INSERT INTO enhancedentities "
                    + "(consultation_id,article_id,json_text) VALUES"
                    + "(?,?,?);";
            PreparedStatement prepStatement = connection.prepareStatement(insertSQL);
//            connection.setAutoCommit(false);
            for (int curArticle : input.keySet()) {
                String json_text = input.get(curArticle);
                prepStatement.setInt(1, curConsId);
                prepStatement.setInt(2, curArticle);
                prepStatement.setString(3, json_text);
//                prepStatement.executeUpdate();
                prepStatement.addBatch();
            }
            prepStatement.executeBatch();
//            connection.commit();
            prepStatement.close();

//            for (int i = 0; i<x.length; i++){
//                System.out.println(x[i]);
//            }
        } catch (BatchUpdateException ex) {
            ex.printStackTrace();
//            System.out.println(ex.getNextException());
        }
    }

    public static TreeMap<Integer, String> getDemocracitConsultationBody() throws SQLException {
        TreeMap<Integer, String> cons_body = new TreeMap<>();
        String sql = "SELECT id, short_description, completed_text "
                + "FROM consultation "
                + "WHERE id "
//                + "=3338;";
                +"NOT IN (SELECT consultations_ner.id FROM consultations_ner);";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            int consID = rs.getInt("id");
            String cons_desc = rs.getString("short_description");
//            String cons_compl = rs.getString("completed_text");
//            if (cons_compl != null) {
//                String cons_text = cons_compl + "\n" + cons_desc;
//                cons_body.put(consID, cons_text);
//            } else {
            cons_body.put(consID, cons_desc);
//            }
        }
        return cons_body;
    }

    public static TreeMap<Integer, String> getDemocracitCompletedConsultationBody() throws SQLException {
        TreeMap<Integer, String> cons_compl_desc = new TreeMap<>();
        String sql = "SELECT id, completed_text "
                + "FROM consultation "
                + "WHERE completed = 1 AND id NOT IN (SELECT consultations_ner.id FROM consultations_ner);";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            int consID = rs.getInt("id");
            String cons_compl_text = rs.getString("completed_text");
//            String cons_compl = rs.getString("completed_text");
//            if (cons_compl != null) {
//                String cons_text = cons_compl + "\n" + cons_desc;
//                cons_body.put(consID, cons_text);
//            } else {
            cons_compl_desc.put(consID, cons_compl_text);
//            }
        }
        return cons_compl_desc;
    }

    public static void insertDemocracitConsultationMinister(TreeMap<Integer, String> consultationCountersign, TreeMap<Integer, String> consultationCounterrole) throws SQLException {
        try {
            String sql = "INSERT INTO consultations_ner "
                    + "(id,countersigned_name, countersigned_position) VALUES "
                    + "(?,?,?)";
            PreparedStatement prepStatement = connection.prepareStatement(sql);
            for (int consId : consultationCountersign.keySet()) {
                prepStatement.setInt(1, consId);
                prepStatement.setString(2, consultationCountersign.get(consId));
                if(consultationCounterrole.get(consId)!=null){
                    prepStatement.setString(3, consultationCounterrole.get(consId));
                }else{
                    prepStatement.setString(3, "");
                }
                prepStatement.addBatch();
            }
            prepStatement.executeBatch();
            prepStatement.close();
        } catch (BatchUpdateException ex) {
//            ex.printStackTrace();
            System.out.println(ex.getNextException());
        }
    }
    
    /**
     * Starts the activity log
     *
     * @param startTime - The start time of the crawling procedure
     * @return - The activity's log id
     * @throws java.sql.SQLException
     */
    public static int LogRegexFinder(long startTime) throws SQLException {
        String insertLogSql = "INSERT INTO log.activities (module_id, start_date, end_date, status_id, message) VALUES (?,?,?,?,?)";
        PreparedStatement prepLogCrawlStatement = connection.prepareStatement(insertLogSql, Statement.RETURN_GENERATED_KEYS);
        prepLogCrawlStatement.setInt(1, 4);
        prepLogCrawlStatement.setTimestamp(2, new java.sql.Timestamp(startTime));
        prepLogCrawlStatement.setTimestamp(3, null);
        prepLogCrawlStatement.setInt(4, 1);
        prepLogCrawlStatement.setString(5, null);
        prepLogCrawlStatement.executeUpdate();
        ResultSet rsq = prepLogCrawlStatement.getGeneratedKeys();
        int crawlerId = 0;
        if (rsq.next()) {
            crawlerId = rsq.getInt(1);
        }
        prepLogCrawlStatement.close();
        return crawlerId;
    }
    
    
    /**
     * Update the activity log
     *
     * @param endTime
     * @param status_id
     * @param regexerId
     * @param obj
     * @throws java.sql.SQLException
     */
    public static void UpdateLogRegexFinder(long endTime, int regexerId, JSONObject obj) throws SQLException {
        String updateCrawlerStatusSql = "UPDATE log.activities SET "
                + "end_date = ?, status_id = ?, message = ?"
                + "WHERE id = ?";
        PreparedStatement prepUpdStatusSt = connection.prepareStatement(updateCrawlerStatusSql);
        prepUpdStatusSt.setTimestamp(1, new java.sql.Timestamp(endTime));
        prepUpdStatusSt.setInt(2, 2);
        prepUpdStatusSt.setString(3, obj.toJSONString());
        prepUpdStatusSt.setInt(4, regexerId);
        prepUpdStatusSt.executeUpdate();
        prepUpdStatusSt.close();
    }
    

//    public static void InsertLexiconEntry(String lexiconType, String lemma, String lang) throws SQLException {
//        String selectSQL = "SELECT id FROM lexicons_gr WHERE (lexicon_type = ? AND lexicon_lemma = ? AND lexicon_entity = ?)";
//        PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
//        preparedStatement.setString(1, lexiconType);
//        preparedStatement.setString(2, lemma);
//        preparedStatement.setString(3, entity);
//        ResultSet rs = preparedStatement.executeQuery();
//        String insertSQL = "INSERT INTO lexicons_gr "
//                + "(lexicon_type,lexicon_lemma,lexicon_entity,lexicon_lang) VALUES"
//                + "(?,?,?,?)";
//        PreparedStatement prepStatement = connection.prepareStatement(insertSQL);
//        int id = -1;
//        if (rs.next()) {
//            id = rs.getInt(1);
////            System.out.println(id);
//        } else {
//            prepStatement.setString(1, lexiconType);
//            prepStatement.setString(2, lemma);
//            prepStatement.setString(3, entity);
//            prepStatement.setString(4, lang);
//            prepStatement.addBatch();
//        }
//        prepStatement.executeBatch();
//        prepStatement.close();
//    }

}
