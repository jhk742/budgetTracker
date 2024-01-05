package Connectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;


public class HttpProvider {
    private final HttpClient httpClient;

    public HttpProvider() {
        this.httpClient = HttpClients.createDefault();
    }

    /**
        the first method, just have it accept a string (url)
        and return the JSONObject to the calling-method within
     currencyExchangeForm and manipulate the response-data there.
     Name the first method here: makeRequest, not compareCurrency. The latter's for
     the form page. This is just the connectionProvider. Also, take the key and url
     to the formPage.
     */

    public JSONObject executeRequest(String url) throws IOException {
        try {
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet);
            return handleResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //pair conversion

    //if response is valid, return it as a JSONObject (json.org)
    private JSONObject handleResponse(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            JSONObject jsonObj = new JSONObject(EntityUtils.toString(entity));
            return entity != null ? jsonObj : null;
        } else {
            throw new IOException("HTTP request failed with status code: " + statusCode);
        }
    }
}
