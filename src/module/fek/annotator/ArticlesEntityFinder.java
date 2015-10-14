package module.fek.annotator;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import org.json.simple.JSONObject;

/**
 * Starts the FEK entity annotation task. This is the main class to run in order
 * to start the execution of annotator.
 *
 * @author Christos Sardianos
 */
public class ArticlesEntityFinder {

    public static String configFile;
    public static int annotatorId;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException, Exception {

        Date d = new Date();
        long milTime = d.getTime();
        long execStart = System.nanoTime();
        Timestamp startTime = new Timestamp(milTime);
        JSONObject obj = new JSONObject();
        long lStartTime;
        long lEndTime = 0;
        int status_id = 1;
        // Check if config file is provided as input
        if (args.length != 1) {
            System.out.println("None or too many argument parameters where defined! "
                    + "\nPlease provide ONLY the configuration file name as the only argument.");
        } else {
            configFile = args[0];
            // Initialize database connection
            Database.init();
            lStartTime = System.currentTimeMillis();
            System.out.println("Article entity extraction process started at: " + startTime);
            annotatorId = Database.LogArticleEntityFinder(lStartTime);
            // Fetch the non-annotated articles from the db
            ArrayList<ArticleForAnnotation> articleForAnnotation = Database.FetchArticleTexts();
            for (ArticleForAnnotation curArticle : articleForAnnotation) {
                ArrayList<ArticleForAnnotation> annotatedArticles = new ArrayList<>();
                // Perform regex over each article
                RegexFEK re = new RegexFEK();
                try {
                    ArticleForAnnotation anArt = re.FindFekRegex(curArticle);
                    // If at least one annotation exists, then add this article to the list
                    if (anArt.annotations.size() > 0) {
                        annotatedArticles.add(anArt);
                    }
                    if (annotatedArticles.size() > 0) {
                        // After recognizing entities over an article,
                        // insert these articles into DB
                        Database.InsertArticleAnnotations(annotatedArticles);
                        status_id = 2;
                        obj.put("message", "Article entity extractor finished with no errors");
                        obj.put("details", "");
                    }
                } catch (Exception ex) {
                    System.err.println(curArticle.consultationID + ":" + curArticle.articleID);
                    status_id = 3;
                    obj.put("message", "Article entity extractor encountered an error");
                    obj.put("details", curArticle.consultationID + ":" + curArticle.articleID + "->" + ex.getMessage());
                }
            }
            lEndTime = System.currentTimeMillis();
        }
        long execEnd = System.nanoTime();
        long executionTime = (execEnd - execStart);
        System.out.println("Total process time: " + (((executionTime / 1000000) / 1000) / 60) + " minutes.");
        Database.UpdateLogArticleEntityFinder(lEndTime, status_id, annotatorId, obj);
        Database.closeConnection();
    }
}
