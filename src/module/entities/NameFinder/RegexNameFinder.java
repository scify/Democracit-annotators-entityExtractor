/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package module.entities.NameFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Locale;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Retrieves the name and position of the person that have countersigned each
 * consultation.
 *
 * @author Christos Sardianos
 */
public class RegexNameFinder {

    public static Locale locale = new Locale("el-GR");
    public static HashSet<String> names = new HashSet();
    public static HashSet<String> surnames = new HashSet();
    public static int regexerId;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {

        if(args.length==1){
            Config.configFile=args[0];
        }
        long lStartTime = System.currentTimeMillis();
        Timestamp startTime = new Timestamp(lStartTime);
        System.out.println("Regex Name Finder process started at: " + startTime);
        DB.initPostgres();
        regexerId = DB.LogRegexFinder(lStartTime);
        initLexicons();
        JSONObject obj = new JSONObject();
        TreeMap<Integer, String> consultations = DB.getDemocracitConsultationBody();
        Document doc;
        int count = 0;
        TreeMap<Integer, String> consFoundNames = new TreeMap<>();
        TreeMap<Integer, String> consFoundRoles = new TreeMap<>();
        for (int consId : consultations.keySet()) {
            String consBody = consultations.get(consId);
            String signName = "", roleName = "";
            doc = Jsoup.parse(consBody);
            Elements allPars = new Elements();
            Elements paragraphs = doc.select("p");
            for (Element par : paragraphs) {
                if (par.html().contains("<br>")) {
                    String out = "<p>" + par.html().replaceAll("<br>", "</p><p>") + "</p>";
                    Document internal_doc = Jsoup.parse(out);
                    Elements subparagraphs = internal_doc.select("p");
                    allPars.addAll(subparagraphs);
                } else {
                    allPars.add(par);
                }
//                System.out.println(formatedText);
            }
            String signature = getSignatureFromParagraphs(allPars);
//            System.out.println(signature);
            if (signature.contains("#")) {
                String[] sign_tokens = signature.split("#");
                signName = sign_tokens[0];
                if (sign_tokens.length > 1) {
                    roleName = sign_tokens[1];
                }
                consFoundNames.put(consId, signName.trim());
                consFoundRoles.put(consId, roleName.trim());
                count++;
            } else {
                System.err.println("--" + consId);
            }
//           
        }
        DB.insertDemocracitConsultationMinister(consFoundNames, consFoundRoles);

        TreeMap<Integer, String> consultationsCompletedText = DB.getDemocracitCompletedConsultationBody();
        Document doc2;
        TreeMap<Integer, String> complConsFoundNames = new TreeMap<>();
        int count2 = 0;
        for (int consId : consultationsCompletedText.keySet()) {
            String consBody = consultationsCompletedText.get(consId);
            String signName = "", roleName = "";
            doc2 = Jsoup.parse(consBody);
//            if (doc.text().contains("<br>")) {
//                doc.text().replaceAll("(<[Bb][Rr]>)+", "<p>");
//            }
            Elements allPars = new Elements();
            Elements paragraphs = doc2.select("p");
            for (Element par : paragraphs) {

                if (par.html().contains("<br>")) {
                    String out = "<p>" + par.html().replaceAll("<br>", "</p><p>") + "</p>";
                    Document internal_doc = Jsoup.parse(out);
                    Elements subparagraphs = internal_doc.select("p");
                    allPars.addAll(subparagraphs);
                } else {
                    allPars.add(par);
                }
            }
            String signature = getSignatureFromParagraphs(allPars);
            if (signature.contains("#")) {
                String[] sign_tokens = signature.split("#");
                signName = sign_tokens[0];
                if (sign_tokens.length > 1) {
                    roleName = sign_tokens[1];
                }
                consFoundNames.put(consId, signName.trim());
                consFoundRoles.put(consId, roleName.trim());
//                System.out.println(consId);
//                System.out.println(signName.trim());
//                System.out.println("***************");
                count2++;
            } else {
                System.err.println("++" + consId);
            }
        }
        DB.insertDemocracitConsultationMinister(complConsFoundNames, consFoundRoles);
        long lEndTime = System.currentTimeMillis();
        System.out.println("Regex Name Finder process finished at: " + startTime);
        obj.put("message", "Regex Name Finder finished with no errors");
        obj.put("details", "");
        DB.UpdateLogRegexFinder(lEndTime, regexerId, obj);
        DB.close();
    }

    public static void initLexicons() throws UnsupportedEncodingException, IOException {

        BufferedReader brNames = null;
        BufferedReader brSurNames = null;
        String line = "";
        try {
//             new FileInputStream(fileDir), "UTF8")
            brNames = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NameLexicons.namesFile)), "UTF8"));
            while ((line = brNames.readLine()) != null) {
//                names.add(Normalizer.normalize(line.toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", ""));
                names.add(line);
            }
            brSurNames = new BufferedReader(new InputStreamReader(new FileInputStream(new File(NameLexicons.surnamesFiles)), "UTF8"));
//            brSurNames = new BufferedReader(new FileReader(NameLexicons.surnamesFiles));
            while ((line = brSurNames.readLine()) != null) {
                surnames.add(line);
//                surnames.add(Normalizer.normalize(line.toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", ""));
            }
        } catch (FileNotFoundException e) {
        }

    }

    public static String getName(String[] splitedText) {
        String name = "";
        for (int z = 0; z < splitedText.length; z++) {
            String splText = splitedText[z].replaceAll("[\\s.]", "").replaceAll("\u00a0", "").replaceAll("»", "").replaceAll(",", "");
            if (names.contains(splText) || surnames.contains(splText)) {
                name += splText + " ";
            }
        }
        return name;
    }

    public static boolean isPerson(String[] splitedText) {
        boolean isPerson = false;
        for (int z = 0; z < splitedText.length; z++) {
            String splText = splitedText[z].replaceAll("[\\s.]", "").replaceAll("\u00a0", "").replaceAll("»", "").replaceAll(",", "");
            if (names.contains(splText) || surnames.contains(splText)) {
                isPerson = true;
            }
        }
        return isPerson;
    }

    public static String getSignatureFromParagraphs(Elements paragraphs) {
        String signature = "";
        String signName = "", roleName = "";
        int signIdx = 0, roleIdx = 0;
        int row = 0;
        TreeMap<Integer, String> roles = new TreeMap<Integer, String>();
        for (Element n : paragraphs) {
            row++;
            String formatedText = Normalizer.normalize(n.text().toUpperCase(locale), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            if (formatedText.contains(" ") && !formatedText.matches(".*[0-9].*")) {
//                  if (formatedText.contains("<br>")) {
//                      formatedText = formatedText.replaceAll("<br\\s*/>", " ");
//                   }
                String[] splitedText = formatedText.split(" ");
//                    System.out.println(splitedText.length);
                if (splitedText.length < 7) {
                    boolean isSign = false;
                    String text = "";
                    for (int z = 0; z < splitedText.length; z++) {
                        String splText = splitedText[z].replaceAll("[\\s.]", "").replaceAll("\u00a0", "").replaceAll("»", "").replaceAll(",", "");
                        if (names.contains(splText) || surnames.contains(splText)) {
                            signName += splText + " ";
                            signIdx = row;
                            isSign = true;
                        }
                        text += splText + " ";
//                            if (z == splitedText.length-1){
//                                System.out.println(signName.trim());
//                            }
                    }
                    if (!isSign) {
                        roleIdx = row;
                        if (!text.contains(" ΦΙΛ") && !text.contains("ΕΥΧΑ")) {
                            roles.put(roleIdx, text.trim());
                        }
                    }
                }
            }
        }
        for (Integer roleRow : roles.keySet()) {
            //                    if (signName.length() == 0) {
            if (Math.abs(signIdx - roleRow) < 4) {
                roleName += roles.get(roleRow) + " ";
            }

        }

        if (signName.length() > 0) {
            signature = signName + "#" + roleName;
        }
        return signature;
    }
}
