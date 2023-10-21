package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.pojo.lms_pojo.ResponseFromLms;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class Fluent {

    public static String postGetStatus(String json) {
        log.info("REQUEST TO LMS: " + json);
        String status = null;
        try {
            status = Request.Post(lmsServer).bodyString(json, ContentType.APPLICATION_JSON)
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

    public static ResponseFromLms postGetContent(String json) {
        log.info("REQUEST TO LMS: " + json);
        Content content = null;
        ResponseFromLms responseFromLms = null;
        try {
            content = Request.Post(lmsServer).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIP + ":" + lmsPort);
        }
        if (content != null) {
            responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        } else {
            log.info("ERROR RESPONSE IS EMPTY");
        }
        return responseFromLms;
    }

    public static String getUriGetStatus(String uri) {
        log.info("REQUEST URI: " + uri);
        String status = null;
        try {
            status = Request.Get(uri)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        return status;
    }

    public static HttpResponse uriGetHeader(String uri) {
//        log.info("REQUEST URI: " + uri);
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


    public static HttpResponse httpPostJsondToUriGetResponse(String uri) {
//        log.info("REQUEST URI: " + uri);
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


    public static Content httpPostJsonToUriGetContent(String uri, int rCode, String json) {
        log.info("REQUEST POST JSON TO URI: " + uri);
        log.info("JSON: " + json);
        Content content = null;
        try {
            content = Request.Post(uri).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(2000)
                    .socketTimeout(2000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
        }
        return content;
    }

    public static Content httpGetQueryToUri(String uri, String query) {
        log.info("REQUEST POST JSON TO URI: " + uri);
        log.info("JSON: " + query);
        uri = uri + query;
        Content content = null;
        try {
            content = Request.Get(uri).bodyString(query, ContentType.APPLICATION_JSON)
                    .connectTimeout(2000)
                    .socketTimeout(2000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR " + e);
        }
        return content;
    }
}

//  response = Request.Post("https://accounts.spotify.com/api/token?grant_type=client_credentials")
//          .setHeader("Authorization", "Basic " + base64)
//          .setHeader("Content-Type", "application/x-www-form-urlencoded")
//          .execute();
//          json = response.returnContent().asString();