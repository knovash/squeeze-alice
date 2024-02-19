package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Requests {

    public static String postByJsonForStatus(String json) {
        log.info("REQUEST TO LMS: " + json);
        String status = null;
        try {
            status = Request.Post(lmsUrl).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("ERROR " + e);
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIP + ":" + lmsPort);
        }
        return status;
    }

    public static Response postByJsonForResponse(String json) {
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

    public static Response postUriJsonBodyForResponse(String uri, String json) {
        log.info("REQUEST TO LMS: " + json);
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

    public static String getByUriForStatus(String uri) {
        log.info("REQUEST URI: " + uri);
        String status = null;
        try {
            status = Request.Get(uri)
                    .connectTimeout(5000)
                    .socketTimeout(5000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        return status;
    }

    public static String postByUriForStatus(String uri) {
        log.info("REQUEST URI: " + uri);
        String status = null;
        try {
            status = Request.Get(uri)
                    .connectTimeout(5000)
                    .socketTimeout(5000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        return status;
    }

    public static HttpResponse headByUriForResponse(String uri) {
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
}