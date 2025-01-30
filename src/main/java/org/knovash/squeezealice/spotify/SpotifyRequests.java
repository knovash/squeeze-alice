package org.knovash.squeezealice.spotify;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Log4j2
public class SpotifyRequests {

    public static String requestWithRetryGet(String uri) {
        log.info("START");
        String json = SpotifyRequests.requestGetClosable(uri);
        log.info("JSON="+json);
        if (json.equals("401")) {
            log.info("401 RUN REFRESH TOKEN");
            SpotifyAuth.callbackRequestRefresh();
            json = SpotifyRequests.requestGetClosable(uri);
        }
        if (json == null || json.equals("400")) {
            log.info("REQUEST ERROR JSON="+json);
            return null;
        }
        if (json.equals("204")) {
//            log.info("204");
            return null;
        }
        return json;
    }

    public static String requestWithRetryPut(String uri) {
        log.info("START");
        String json = SpotifyRequests.requestPutClosable(uri);
        if (json.equals("401")) {
            log.info("401 RUN REFRESH TOKEN");
            SpotifyAuth.callbackRequestRefresh();
            json = SpotifyRequests.requestPutClosable(uri);
        }
        if (json == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        if (json.equals("204")) {
//            log.info("204");
            return null;
        }
        return json;
    }

    public static String requestWithRetryPost(String uri) {
        log.info("START");
        String json = SpotifyRequests.requestPostClosable(uri);
        if (json.equals("401")) {
            log.info("401 RUN REFRESH TOKEN");
            SpotifyAuth.callbackRequestRefresh();
            json = SpotifyRequests.requestPostClosable(uri);
        }
        if (json == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        if (json.equals("204")) {
//            log.info("204");
            return null;
        }
        return json;
    }

    public static String requestPost(String uri, Header[] headers) {
        String json;
        try {
            Response response = org.apache.http.client.fluent.Request.Post(uri)
                    .setHeaders(headers)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String requestGet(String uri) { // для получения линка для LMS из поиска
        log.info("uri: " + uri);
        Response response = null;
        String json = null;
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        try {
            response = Request.Get(uri)
                    .setHeaders(headers)
                    .execute();
            log.info("RESPONSE: " + response);
            json = response.returnContent().asString();
        } catch (IOException e) {
            return null;
        }
        return json;
    }

    public static String requestGetClosable(String uri) {
        log.info("START");
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        int code;
        String json;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeaders(headers);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                code = response.getStatusLine().getStatusCode();
                log.info("CODE: " + code);
                if (code != 200) return String.valueOf(code);
                json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        log.info("JSON: " + json);
        return json;
    }

    public static String requestPutClosable(String uri) {
        log.info("START");
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        int code;
        String json;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPut httpPut = new HttpPut(uri);
            httpPut.setHeaders(headers);
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                code = response.getStatusLine().getStatusCode();
                log.info("CODE: " + code);
                if (code != 200) return String.valueOf(code);
                json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String requestPostClosable(String uri) {
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        int code;
        String json;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeaders(headers);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                code = response.getStatusLine().getStatusCode();
//                log.info("CODE: " + code);
                if (code != 200) return String.valueOf(code);
                json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    public static String requestPutHttpClient(String uri) {
        Header[] headers = {
                new BasicHeader("Authorization", SpotifyAuth.bearer_token)
        };
        int code;
        String json;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPut httpPut = new HttpPut(uri);
            httpPut.setHeaders(headers);
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                code = response.getStatusLine().getStatusCode();
//                log.info("CODE: " + code);
                if (code != 200) return String.valueOf(code);
                json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}