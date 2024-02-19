package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Log4j2
public class SpotifyRequests {

    public static String requestForJson(String uri, Header[] headers) {
        Response response;
        String json;
        try {
            response = org.apache.http.client.fluent.Request.Post(uri)
                    .setHeaders(headers)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String requestHttpClient(String uri, Header[] headers) {
        int code;
        String responseBody;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeaders(headers);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                code = response.getStatusLine().getStatusCode();
                log.info("CODE: " + code);
//                if (code == 401 ) SpotifyAuth.action();
                if (code != 200 ) return "CODE: "+code+" ERROR: " + response.getStatusLine();
                responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return responseBody;
    }
}
