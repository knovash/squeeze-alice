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
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIp + ":" + lmsPort);
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
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIp + ":" + lmsPort);
        }
        if (content != null) {
            response = JsonUtils.jsonToPojo(content.asString(), Response.class);
        } else {
            log.info("ERROR RESPONSE IS EMPTY");
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
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIp + ":" + lmsPort);
        }
        return content.asString();
    }
}