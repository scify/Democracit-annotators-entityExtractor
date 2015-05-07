package module.fek.annotator;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Starts the FEK entity annotation task.
 * This is the main class to run in order to start the execution of annotator.
 *
 * @author Christos Sardianos
 */
public class ArticlesEntityFinder {

    public static String configFile;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException, Exception {
        
        Date d = new Date();
        long milTime = d.getTime();
        long execStart = System.nanoTime();
        Timestamp startTime = new Timestamp(milTime);
        ArrayList<ArticleForAnnotation> annotatedArticles = new ArrayList<>();
        // Check if config file is provided as input
        if (args.length != 1) {
            System.out.println("None or too many argument parameters where defined! "
                    + "\nPlease provide ONLY the configuration file name as the only argument.");
        } else {
            configFile = args[0];
            System.out.println("Annotation task started at: " + startTime);
            // Initialize database connection
            Database.init();
            // Fetch the non-annotated articles from the db
            ArrayList<ArticleForAnnotation> articleForAnnotation = Database.FetchArticleTexts();
            for (ArticleForAnnotation curArticle : articleForAnnotation) {
                // Perform regex over each article
                RegexFEK re = new RegexFEK();
                ArticleForAnnotation anArt = re.FindFekRegex(curArticle);
                // If at least one annotation exists, then add this article to the list
                if (anArt.annotations.size() > 0) {
                    annotatedArticles.add(anArt);
                }
            }
        }
        // After recognizing entities over all articles,
        // insert these articles into DB
        Database.InsertArticleAnnotations(annotatedArticles);
        long execEnd = System.nanoTime();
        long executionTime = (execEnd - execStart);
        System.out.println("Total process time: " + (((executionTime/1000000)/1000)/60) + " minutes.");
    }
}
