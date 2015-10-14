package module.entities.UsernameChecker;

import module.fek.annotator.*;
import module.entities.UsernameChecker.ReportEntry;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeMap;
import module.entities.UsernameChecker.CheckOpengovUsernames;
import org.json.simple.JSONObject;

/**
 *
 * @author Christos Sardianos
 */
public class Database {

    static Connection connection = null;
    static Locale locale = new Locale("el-GR");

    /**
     * Initiates the database connection.
     *
     * @throws java.sql.SQLException
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static void init() throws SQLException, FileNotFoundException, IOException {
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
                try {
                    // Read configuration file
                    br = new BufferedReader(new FileReader(CheckOpengovUsernames.configFile));
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
                        }
                    }
                } catch (FileNotFoundException e) {
                }
                // Create the connection to database
                String DB_url = "jdbc:postgresql://" + ip_address;
                connection = DriverManager.getConnection(DB_url, user, pass);
            } catch (SQLException e) {
                System.out.println("Connection Failed! Check output console.");
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * Retrieves the articles that haven't been yet annotated from database.
     *
     * @throws java.sql.SQLException
     */
    static ArrayList<ArticleForAnnotation> FetchArticleTexts() throws SQLException {
        ResultSet rs = null;
        ArrayList<ArticleForAnnotation> arts4annotation = new ArrayList<>();
        Statement stmnt = null;
        String selectArticles = "SELECT articles.id, articles.consultation_id, articles.body "
                + "FROM articles "
                + "LEFT JOIN article_entities "
                + "ON article_entities.article_id = articles.id "
                + "WHERE article_entities.article_id IS NULL";
        stmnt = connection.createStatement();
        rs = stmnt.executeQuery(selectArticles);
        while (rs.next()) {
            int articleID = rs.getInt("id");
            int consultationID = rs.getInt("consultation_id");
            String article_text = rs.getString("body");
            ArticleForAnnotation currentArticle = new ArticleForAnnotation(articleID, consultationID, article_text);
            arts4annotation.add(currentArticle);
        }
        return arts4annotation;
    }

    /**
     * Inserts all the article law_annotations into DB.
     *
     * @param annotatedArticles - An ArrayList<ArticleForAnnotation> with the
     * articles and the annotations found over them.
     * @throws SQLException
     */
    static void InsertArticleAnnotations(ArrayList<ArticleForAnnotation> annotatedArticles) throws SQLException {
        PreparedStatement preparedStatement = null;
        String insertAnnotationSQL = "INSERT INTO article_entities"
                + "(article_id, start_index, end_index, url_pdf, entity_type, entity_text, consultation_id, entity_law) VALUES"
                + "(?,?,?,?,?,?,?,?)";
        try {
            preparedStatement = connection.prepareStatement(insertAnnotationSQL);
            connection.setAutoCommit(false);
            for (ArticleForAnnotation x : annotatedArticles) {
                for (Annotation y : x.annotations) {
                    preparedStatement.setInt(1, x.articleID);
                    preparedStatement.setInt(2, y.startIndex);
                    preparedStatement.setInt(3, y.endIndex);
                    preparedStatement.setString(4, y.url);
                    preparedStatement.setString(5, y.entityType);
                    preparedStatement.setString(6, y.entityText);
                    preparedStatement.setInt(7, x.consultationID);
                    preparedStatement.setString(8, y.entityLaw);
                    preparedStatement.addBatch();
                }
            }
            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {

            System.out.println(e.getMessage());
            System.out.println(e.getNextException().getMessage());
        } finally {
            preparedStatement.close();
        }
    }

    public static void closeConnection() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static TreeMap<Integer, String> GetOpenGovUsers() throws SQLException {
        ResultSet rs = null;
        TreeMap<Integer, String> opengovUsernames = new TreeMap<>();
        Statement stmnt = null;
        String selectArticles = "SELECT id, fullname "
                + "FROM comment_opengov "
                + "WHERE report_name IS NULL;";
        stmnt = connection.createStatement();
        rs = stmnt.executeQuery(selectArticles);
        while (rs.next()) {
            int userID = rs.getInt("id");
            String username = rs.getString("fullname");
            opengovUsernames.put(userID, username);
        }
        return opengovUsernames;
    }

    public static void UpdateOpengovUsersReportName(HashSet<ReportEntry> report_names) throws SQLException {
        PreparedStatement preparedStatement = null;
        String updateSQL = "UPDATE comment_opengov SET report_name = ?, report_type = ? WHERE id= ?";
        try {
            preparedStatement = connection.prepareStatement(updateSQL);
            connection.setAutoCommit(false);
            for (ReportEntry curEntry : report_names) {
                preparedStatement.setString(1, curEntry.report_name);
                preparedStatement.setInt(2, curEntry.report_name_type);
                preparedStatement.setInt(3, curEntry.user_id);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {

            System.out.println(e.getMessage());
            System.out.println(e.getNextException().getMessage());
        } finally {
            preparedStatement.close();
        }

    }
    
    /**
     * Starts the activity log
     *
     * @param startTime - The start time of the crawling procedure
     * @return - The activity's log id
     * @throws java.sql.SQLException
     */
    public static int LogUsernameChecker(long startTime) throws SQLException {
        String insertLogSql = "INSERT INTO log.activities (module_id, start_date, end_date, status_id, message) VALUES (?,?,?,?,?)";
        PreparedStatement prepLogCrawlStatement = connection.prepareStatement(insertLogSql, Statement.RETURN_GENERATED_KEYS);
        prepLogCrawlStatement.setInt(1, 5);
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
     * @param usernameCheckerId
     * @param obj
     * @throws java.sql.SQLException
     */
    public static void UpdateLogUsernameChecker(long endTime, int status_id, int usernameCheckerId, JSONObject obj) throws SQLException {
        String updateCrawlerStatusSql = "UPDATE log.activities SET "
                + "end_date = ?, status_id = ?, message = ?"
                + "WHERE id = ?";
        PreparedStatement prepUpdStatusSt = connection.prepareStatement(updateCrawlerStatusSql);
        prepUpdStatusSt.setTimestamp(1, new java.sql.Timestamp(endTime));
        prepUpdStatusSt.setInt(2, status_id);
        prepUpdStatusSt.setString(3, obj.toJSONString());
        prepUpdStatusSt.setInt(4, usernameCheckerId);
        prepUpdStatusSt.executeUpdate();
        prepUpdStatusSt.close();
    }

}
