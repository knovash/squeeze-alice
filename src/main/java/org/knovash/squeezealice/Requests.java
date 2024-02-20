package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Requests {

    public static HttpResponse headToUriForHttpResponse(String uri) {
//  получить хедер для проверки это LMS или нет
        HttpResponse response = null;
        try {
            response = Request.Head(uri)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse();
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
        return response;
    }

    public static String postToLmsForStatus(String json) {
//  все запросы плеера на действия. возвращают статус выполнения
        log.info("REQUEST TO LMS: " + json);
        String status = null;
        try {
            status = Request.Post(lmsUrl).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse()
                    .getStatusLine() // возвращает статус выполнения
                    .toString();
        } catch (IOException e) {
            log.info("ERROR " + e);
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIP + ":" + lmsPort);
        }
        return status;
    }

    public static Response postToLmsForContent(String json) {
//  все запросы плеера для получения информации из Response response.result._artist
        log.info("REQUEST TO LMS: " + json);
        Content content = null;
        Response response = null;
        try {
            content = Request.Post(lmsUrl).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIP + ":" + lmsPort);
        }
        if (content != null) {
            response = JsonUtils.jsonToPojo(content.asString(), Response.class);
        } else {
            log.info("ERROR RESPONSE IS EMPTY");
        }
        return response;
    }

    public static HttpResponse postByHeadersJsonForResponse(String uri, Header[] headers, String json) {
        log.info("REQUEST URI: " + uri);
        log.info("REQUEST HEADERS: " + headers[0]);
        log.info("REQUEST BODY: " + json);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse response = null;
        try {
            HttpPost request = new HttpPost(uri);
            StringEntity params = new StringEntity(json);
            request.addHeader("Authorization", "Basic " + SpotifyAuth.encoded);
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");
            request.setEntity(params);
            response = httpClient.execute(request);
        } catch (Exception ex) {
        } finally {
            // @Deprecated httpClient.getConnectionManager().shutdown();
        }
        log.info(response);
        try {
            log.info(response.getEntity().getContent().readAllBytes().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info(response.getStatusLine().getReasonPhrase());

        return response;
    }

    public static Response postUriJsonBodyForResponse(String uri, String json) {
        log.info("REQUEST URI: " + uri);
        log.info("REQUEST BODY: " + json);
        Content content = null;
        Response response = null;
        try {
            content = Request.Post(uri).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
        }
        if (content != null) {
            response = JsonUtils.jsonToPojo(content.asString(), Response.class);
        } else {
            log.info("ERROR RESPONSE IS EMPTY");
        }
        return response;
    }

    public static HttpResponse getToUriForHttpResponse(String uri) {
        log.info("REQUEST URI: " + uri);
        HttpResponse response = null;
        try {
            response = Request
                    .Get(uri)
                    .connectTimeout(5000)
                    .socketTimeout(5000)
                    .execute().returnResponse();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        return response;
    }

    public static String requestHttpClientPostBody(String uri, Header[] headers, String json) {
        log.info("URI: " + uri);
        log.info("HEADERS: " + uri);
        log.info("BODY: " + uri);
        int code;
        String responseBody;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpPost httpPost = new HttpPost(uri);
            httpPost.setHeaders(headers);
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
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