/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package module.entities.UsernameChecker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeMap;
import org.json.simple.JSONObject;

/**
 *
 * @author Christos Sardianos
 */
public class CheckOpengovUsernames {

    public static Locale locale = new Locale("el-GR");
    public static String configFile;
    public static HashSet<String> names = new HashSet();
    public static HashSet<String> surnames = new HashSet();
    public static HashSet<String> organizations = new HashSet();
    public static int usernameCheckerId;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {
//        args = new String[1];
//        args[0] = "searchConf.txt";
        Date d = new Date();
        long milTime = d.getTime();
        long execStart = System.nanoTime();
        Timestamp startTime = new Timestamp(milTime);
        long lStartTime;
        long lEndTime = 0;
        int status_id = 1;
        JSONObject obj = new JSONObject();
        if (args.length != 1) {
            System.out.println("None or too many argument parameters where defined! "
                    + "\nPlease provide ONLY the configuration file name as the only argument.");
        } else {
            try {
                configFile = args[0];
                initLexicons();
                Database.init();
                lStartTime = System.currentTimeMillis();
                System.out.println("Opengov username identification process started at: " + startTime);
                usernameCheckerId = Database.LogUsernameChecker(lStartTime);
                TreeMap<Integer, String> OpenGovUsernames = Database.GetOpenGovUsers();
                HashSet<ReportEntry> report_names = new HashSet<>();
                if (OpenGovUsernames.size() > 0) {
                    for (int userID : OpenGovUsernames.keySet()) {
                        String DBusername = Normalizer.normalize(OpenGovUsernames.get(userID).toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                        String username = "";
                        int type;
                        String[] splitUsername = DBusername.split(" ");
                        if (checkNameInLexicons(splitUsername)) {
                            for (String splText : splitUsername) {
                                username += splText + " ";
                            }
                            type = 1;
                        } else if (checkOrgInLexicons(splitUsername)) {
                            for (String splText : splitUsername) {
                                username += splText + " ";
                            }
                            type = 2;
                        } else {
                            username = DBusername;
                            type = -1;
                        }
                        ReportEntry cerEntry = new ReportEntry(userID, username.trim(), type);
                        report_names.add(cerEntry);
                    }
                    status_id = 2;
                    obj.put("message", "Opengov username checker finished with no errors");
                    obj.put("details", "");
                    Database.UpdateOpengovUsersReportName(report_names);
                    lEndTime = System.currentTimeMillis();
                } else {
                    status_id = 2;
                    obj.put("message", "Opengov username checker finished with no errors");
                    obj.put("details", "No usernames needed to be checked");
                    lEndTime = System.currentTimeMillis();
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
                status_id = 3;
                obj.put("message", "Opengov username checker encountered an error");
                obj.put("details", ex.getMessage().toString());
                lEndTime = System.currentTimeMillis();
            }
        }
        long execEnd = System.nanoTime();
        long executionTime = (execEnd - execStart);
        System.out.println("Total process time: " + (((executionTime / 1000000) / 1000) / 60) + " minutes.");
        Database.UpdateLogUsernameChecker(lEndTime, status_id, usernameCheckerId, obj);
        Database.closeConnection();
    }

    public static boolean checkNameInLexicons(String[] splitedText) {
        boolean found = false;
        outerloop:
        for (String splText : splitedText) {
            if (names.contains(splText) || surnames.contains(splText)) {
                found = true;
                break outerloop;
            }
        }
        return found;
    }

    public static boolean checkOrgInLexicons(String[] splitedText) {
        boolean found = false;
        for (String splText : splitedText) {
            if (organizations.contains(splText)) {
                found = true;
            }
        }
        return found;
    }

    public static void initLexicons() throws UnsupportedEncodingException, IOException {

        BufferedReader brNames = null;
        BufferedReader brSurNames = null;
        BufferedReader brOrgs = null;
        String line = "";
        try {
            brNames = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NameLexicons.namesFile)), "UTF8"));
            while ((line = brNames.readLine()) != null) {
//                names.add(Normalizer.normalize(line.toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", ""));
                names.add(line);
            }
            brSurNames = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NameLexicons.surnamesFiles)), "UTF8"));
            while ((line = brSurNames.readLine()) != null) {
                surnames.add(line);
//                surnames.add(Normalizer.normalize(line.toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", ""));
            }
            brOrgs = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NameLexicons.organizationFiles)), "UTF8"));
            while ((line = brOrgs.readLine()) != null) {
                organizations.add(line);
//                surnames.add(Normalizer.normalize(line.toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", ""));
            }
        } catch (FileNotFoundException e) {
        }
    }

//    public static boolean checkNameInLexicons(String uname) {
//        boolean found = false;
//        if (names.contains(uname) || surnames.contains(uname)) {
//            found = true;
//        }
//        return found;
//    }
}
