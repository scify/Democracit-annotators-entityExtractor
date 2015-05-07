package module.fek.annotator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * Sends a POST request to the National Press.
 *
 * @author herc
 */
public class PostRequestFEK {

    private final String USER_AGENT = "Mozilla/5.0";

    // HTTP POST request
    private void sendPost() throws Exception {
        String url = "http://www.et.gr/idocs-nph/search/fekForm.html#results";
        URL obj = new URL(url);
        HttpPost post = new HttpPost(url);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("year", "2009"));
        parameters.add(new BasicNameValuePair("chbIssue_1", "true"));
        parameters.add(new BasicNameValuePair("fekNumberFrom", "80"));
        parameters.add(new BasicNameValuePair("fekNumberTo", "80"));
        parameters.add(new BasicNameValuePair("fekEffectiveDateFrom", "01.01.2009"));
        parameters.add(new BasicNameValuePair("fekEffectiveDateTo", "31.12.2009"));
        parameters.add(new BasicNameValuePair("fekReleaseDateFrom", "01.01.2009"));
        parameters.add(new BasicNameValuePair("fekReleaseDateTo", "31.12.2010"));
        parameters.add(new BasicNameValuePair("pageNumber", "1"));
        post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
        String urlParameters = convertStreamToString(post.getEntity().getContent());
        HttpResponse response = httpclient.execute(post);
        HttpEntity entity1 = response.getEntity();
    }

    private void sendPost2() throws Exception {
        String url = "http://www.et.gr/idocs-nph/search/fekForm.html#results";
        URL obj = new URL(url);
        HttpURLConnection connection = null;
        HttpPost post = new HttpPost(url);
        //Create connection
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("year", "2009"));
        parameters.add(new BasicNameValuePair("chbIssue_1", "on"));
        parameters.add(new BasicNameValuePair("fekNumberFrom", "80"));
        parameters.add(new BasicNameValuePair("fekNumberTo", "80"));
        parameters.add(new BasicNameValuePair("fekEffectiveDateFrom", "01.01.2009"));
        parameters.add(new BasicNameValuePair("fekEffectiveDateTo", "31.12.2009"));
        parameters.add(new BasicNameValuePair("fekReleaseDateFrom", "01.01.2009"));
        parameters.add(new BasicNameValuePair("fekReleaseDateTo", "31.12.2010"));
        parameters.add(new BasicNameValuePair("pageNumber", "1"));
        post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
        String urlParameters = convertStreamToString(post.getEntity().getContent());

        connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream());
        wr.writeBytes(urlParameters);
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line, urlpdf = "";
        while ((line = rd.readLine()) != null) {
            if (line.contains("Το ΦΕΚ σε PDF μορφή")) {
                urlpdf = "http://www.et.gr" + line.substring(line.indexOf("href=") + 6, line.indexOf("\"  target="));
            }
        }
        rd.close();
        wr.flush();
        wr.close();
    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    /**
     * Sends a POST request to the National Press, using the parameters from each annotation.
     *
     * @param FEK_year 
     * @param FEK_issue 
     * @param FEK_number 
     * @param checkBoxIssue 
     * @return The pdfUrl from the National Press
     * @throws Exception
     */
    public String sendPost3(String FEK_year, String FEK_issue, String FEK_number, String checkBoxIssue) throws Exception {

        String url = "http://www.et.gr/idocs-nph/search/fekForm.html#results";
        URL obj = new URL(url);
        HttpURLConnection connection = null;
        HttpPost post = new HttpPost(url);
        //Create connection
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("year", FEK_year));
        parameters.add(new BasicNameValuePair(checkBoxIssue, "on"));
        parameters.add(new BasicNameValuePair("fekNumberFrom", FEK_number));
        parameters.add(new BasicNameValuePair("fekNumberTo", FEK_number));
        parameters.add(new BasicNameValuePair("fekEffectiveDateFrom", "01.01." + FEK_year));
        parameters.add(new BasicNameValuePair("fekEffectiveDateTo", "31.12." + FEK_year + 1));
        parameters.add(new BasicNameValuePair("fekReleaseDateFrom", "01.01." + FEK_year));
        parameters.add(new BasicNameValuePair("fekReleaseDateTo", "31.12." + FEK_year + 1));
        parameters.add(new BasicNameValuePair("pageNumber", "1"));
        post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
        String urlParameters = convertStreamToString(post.getEntity().getContent());

        connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line, urlpdf = "";
        // Get Response
        while ((line = rd.readLine()) != null) {
            if (line.contains("Το ΦΕΚ σε PDF μορφή")) {
                urlpdf = "http://www.et.gr" + line.substring(line.indexOf("href=") + 6, line.indexOf("\"  target="));
            }
        }
        rd.close();
        wr.flush();
        wr.close();
        return urlpdf;
    }
}
