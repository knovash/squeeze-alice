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

    public static HttpResponse headToUriForHttpResponse(String uri) {
//  получить хедер для проверки это LMS или нет
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

    public static String postToLmsForStatus(String json) {
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
            log.info("REQUEST ERROR " + e);
            return null;
        }
        return status;
    }

    public static Response postToLmsForResponse(String json) {
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
            return null;
        }
        if (content != null) {
            response = JsonUtils.jsonToPojo(content.asString(), Response.class);
        } else {
            log.info("ERROR RESPONSE IS EMPTY");
            return null;
        }
        return response;
    }

    public static String postToLmsForJsonBody(String json) {
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
            return null;
        }
        return content.asString();
    }

//    https://autoremotejoaomgcd.appspot.com/sendmessage?key=fovfKw-pC3A:APA91bFz1IHu4FIo9BpJaxwW0HgOulJtoXHF-khXptkSmn6QjhBIywkgi0-w9f4DvMK5y-hoOOTWsXDrv7ASE4S4BADhV8SQz6Y0XOJ5XWbF0pmprdOdmA7aEZ5hfQAWZ2Cd9RW_rShf&message=re

    public static void autoRemoteRefresh() {
        log.info("TASKER AURO REMOTE REFRESH");
        String uri = "https://autoremotejoaomgcd.appspot.com/sendmessage?key=fovfKw-pC3A:APA91bFz1IHu4FIo9BpJaxwW0HgOulJtoXHF-khXptkSmn6QjhBIywkgi0-w9f4DvMK5y-hoOOTWsXDrv7ASE4S4BADhV8SQz6Y0XOJ5XWbF0pmprdOdmA7aEZ5hfQAWZ2Cd9RW_rShf&message=re";
        try {
            Request.Post(uri)
//                    .setHeader("Authorization", "OAuth " + yandex.bearer)
                    .execute();
        } catch (IOException e) {
            log.info("SAY ERROR");
        }
    }


}