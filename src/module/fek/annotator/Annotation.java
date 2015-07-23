
package module.fek.annotator;

/**
 *
 * @author Christos Sardianos
 */
public class Annotation {

    public String entityText;
    public int startIndex;
    public int endIndex;
    public String url;
    public String entityType;
    public String entityLaw;

    public Annotation(String entityText, int startIndex, int endIndex, String url, String entityType, String entityLaw) {
        this.entityText = entityText;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.url = url;
        this.entityType = entityType;
        this.entityLaw = entityLaw;
    }

}
