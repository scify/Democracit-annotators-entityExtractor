
package module.entities.UsernameChecker;

/**
 *
 * @author Christos Sardianos
 */
public class ReportEntry {

    public int user_id;
    public String report_name;
    public int report_name_type;

    public ReportEntry(int user_id, String report_name, int report_name_type) {
        this.user_id = user_id;
        this.report_name = report_name;
        this.report_name_type = report_name_type;
    }

}
