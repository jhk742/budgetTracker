package Connectors;
import ExceptionHandler.ExceptionHandler;
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

    public JSONObject executeGetRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        return handleResponse(response);
    }

    //if response is valid, return it as a JSONObject (json.org)
    private JSONObject handleResponse(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();
            JSONObject jsonObj = new JSONObject(EntityUtils.toString(entity));
            return entity != null ? jsonObj : null;
        } else {
            throw new IOException(String.valueOf(statusCode));
        }
    }
}
