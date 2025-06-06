package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class Requests {

    public static HttpResponse headToUriForHttpResponse(String uri) {
        HttpResponse response = null;
        try {
            response = Request.Head(uri)
                    .connectTimeout(900)
                    .socketTimeout(900)
                    .execute()
                    .returnResponse();
        } catch (IOException e) {
//            throw new RuntimeException(e);
        }
        return response;
    }

    public static Response postToLmsForResponse(String json) {
//        log.info("REQUEST TO LMS: " + json);
        Content content = null;
        Response response = null;
        try {
            content = Request.Post("http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/").bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
            return null;
        }
        if (content != null) {
            response = JsonUtils.jsonToPojo(content.asString(StandardCharsets.UTF_8), Response.class);
        } else {
            log.info("ERROR RESPONSE IS EMPTY");
            return null;
        }

        return response;
    }

    public static String postToLmsForStatus(String json) {
//        log.info("REQUEST TO LMS: " + json);

        String status = null;
        try {
            status = Request.Post("http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/").bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("REQUEST ERROR " + e + "\n" + json);
            return null;
        }
        return status;
    }

    //  все запросы плеера для получения информации из Response response.result._artist

    public static String postToLmsForJsonBody(String json) {
        Content content = null;
        try {
            content = Request.Post("http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/")
                    .bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
            return null;
        }
        return content.asString(StandardCharsets.UTF_8);
    }
}























