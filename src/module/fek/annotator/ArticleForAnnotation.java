
package module.fek.annotator;

import java.util.ArrayList;

/**
 *
 * @author Christos Sardianos
 */
public class ArticleForAnnotation {
    
    public int articleID;
    public int consultationID;
    public String articleText;
    public ArrayList<Annotation> annotations;
    
    public ArticleForAnnotation(int articleID, int consultationID, String articleText) {
        this.articleID = articleID;
        this.consultationID = consultationID;
        this.articleText = articleText;
        this.annotations = new ArrayList<>();
    }
}
