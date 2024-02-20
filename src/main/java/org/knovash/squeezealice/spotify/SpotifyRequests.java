package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Log4j2
public class SpotifyRequests {

    public static String requestForLinkJson(String uri) { // для получения линка для LMS из поиска
        log.info("uri: " + uri);
        Response response = null;
        String json = null;
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        log.info("Authorization: " + SpotifyAuth.bearer_token);
        try {
            response = Request.Get(uri)
                    .setHeaders(headers)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            log.info("JSON: " + json);
            return null;
//            throw new RuntimeException(e);
        }
        log.info("JSON: " + json);
        return json;
    }

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
                if (code == 401) return "401"; // no auth - try  get refresh token
                if (code != 200) return "CODE: " + code + " ERROR: " + response.getStatusLine();
                responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return responseBody;
    }
}