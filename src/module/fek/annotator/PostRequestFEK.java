package module.fek.annotator;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
     * Sends a POST request to the National Press, using the parameters from
     * each annotation.
     *
     * @param FEK_year
     * @param FEK_issue
     * @param FEK_number
     * @param checkBoxIssue
     * @return The pdfUrl from the National Press
     * @throws Exception
     */
    public String sendPost3(String FEK_year, String FEK_issue, String FEK_number, String checkBoxIssue) throws Exception {
        int year = Integer.parseInt(FEK_year);
        if (year < 30) {
            FEK_year = "" + (2000 + year);
        } else if (year < 100) {
            FEK_year = "" + (1900 + year);
        } else {
            FEK_year = "" + year;
        }

        String url = "http://www.et.gr/idocs-nph/search/fekForm.html";
        URL obj = new URL(url);
        HttpURLConnection connection = null;
        HttpPost post = new HttpPost(url);
        int fekyear = Integer.parseInt(FEK_year);
        //Create connection
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("year", FEK_year));
        parameters.add(new BasicNameValuePair(checkBoxIssue, "on"));
        parameters.add(new BasicNameValuePair("fekNumberFrom", FEK_number));
        parameters.add(new BasicNameValuePair("fekNumberTo", FEK_number));
        parameters.add(new BasicNameValuePair("fekEffectiveDateFrom", "01.01." + fekyear));
        parameters.add(new BasicNameValuePair("fekEffectiveDateTo", "31.12." + fekyear));
        parameters.add(new BasicNameValuePair("fekReleaseDateFrom", "01.01." + fekyear));
        parameters.add(new BasicNameValuePair("fekReleaseDateTo", "31.12." + (fekyear + 1)));
        parameters.add(new BasicNameValuePair("pageNumber", "1"));
        post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
        String urlParameters = convertStreamToString(post.getEntity().getContent());

        connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        String line, urlpdf = "";
        // Get Response
        while ((line = rd.readLine()) != null) {
            if (line.contains("Το ΦΕΚ σε PDF μορφή")) {
                urlpdf = "http://www.et.gr" + line.substring(line.indexOf("href=") + 6, line.indexOf("\"  target="));
//                System.out.println(urlpdf);
            }
        }
        rd.close();
//        wr.flush();
//        wr.close();
        return urlpdf;
    }

    String sendPost4(String FEK_year, String item_number, String entity_Type) throws MalformedURLException, UnsupportedEncodingException, Exception {
        int year = Integer.parseInt(FEK_year);
        if (year < 30) {
            FEK_year = "" + (2000 + year);
        } else if (year < 100) {
            FEK_year = "" + (1900 + year);
        } else {
            FEK_year = "" + year;
        }
        String url = "http://www.et.gr/idocs-nph/search/lawForm.html";
        URL obj = new URL(url);
        HttpURLConnection connection = null;
        HttpPost post = new HttpPost(url);
        String radios = "1";
        //Create connection
        if (entity_Type.equals("pd")) {
            radios = "2";
        }

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("lawNumber", item_number));
        parameters.add(new BasicNameValuePair("radios", radios));
        parameters.add(new BasicNameValuePair("pageNumber", "1"));
        post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
        String urlParameters = convertStreamToString(post.getEntity().getContent());

        connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.132 Safari/537.36");
        connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
        connection.setRequestProperty("Content-Language", "en-US");

        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
//        System.out.println(urlParameters);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        String line, urlpdf = "";
        InputStream is = connection.getInputStream();

        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        // Get Response
        boolean found = false;
        StringBuffer jsoupInto = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            jsoupInto.append(line);
        }
        Document doc = Jsoup.parse(jsoupInto.toString());
        Elements paragraphs = doc.select("table#result_table");

        if (paragraphs.size() > 0) {
            Elements rows = paragraphs.get(0).select("tr");
            outerloop:
            for (Element row : rows) {
                if (row.select("h14").text().trim().contains("." + FEK_year)) {
                    Elements alinks = row.select("a");
                    for (Element link : alinks) {
                        if (link.attr("title").contains("Το ΦΕΚ σε PDF μορφή")) {
                            urlpdf = "http://www.et.gr" + link.attr("href");
                            break outerloop;
                        }
                    }
                }
            }
        }
        rd.close();

//        wr.flush();
//        wr.close();
        return urlpdf;
    }

}
