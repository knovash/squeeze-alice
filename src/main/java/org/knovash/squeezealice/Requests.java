package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        log.info(uri + " " + json);
        Content content = null;
        Response response = null;
        try {
            content = Request.Post(uri).bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
            return null;
        }
        if (content != null) {

            // TODO fix UTF
//            response = JsonUtils.jsonToPojo(content.asString(StandardCharsets.UTF_8), Response.class);

            byte[] bytes = content.asBytes();
            String utf8String = new String(bytes, StandardCharsets.UTF_8);
            if (utf8String.contains("\uFFFD")) {
                try {
                    utf8String = new String(bytes, "Windows-1251");
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
            response = JsonUtils.jsonToPojo(utf8String, Response.class);

        } else {
            log.info("ERROR RESPONSE IS EMPTY");
            return null;
        }

        return response;



    }

    public static String postToLmsForStatus(String json) {
//        log.info("REQUEST TO LMS: " + json);
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        log.info(uri + " " + json);
        String status = null;
        try {
            status = Request.Post(uri).bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
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

    public static String postToLmsForStatusNoLog(String json) {
//        log.info("REQUEST TO LMS: " + json);
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
//        log.info(uri + " " + json);
        String status = null;
        try {
            status = Request.Post(uri).bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
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
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        log.info(uri + " " + json);
        try {
            content = Request.Post(uri)
                    .bodyString(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8))
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.debug("ERROR " + e);
            return null;
        }

        // TODO fix UTF
//        return content.asString(StandardCharsets.UTF_8);
        byte[] bytes = content.asBytes();
// Пробуем прочитать как UTF-8
        String utf8String = new String(bytes, StandardCharsets.UTF_8);
// Если строка содержит знак замены (�) – пробуем CP1251
        if (utf8String.contains("\uFFFD")) {
            try {
                utf8String = new String(bytes, "Windows-1251");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return utf8String;
    }
}























