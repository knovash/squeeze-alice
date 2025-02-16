package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;

import static org.knovash.squeezealice.Main.*;

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
//  все запросы плеера для получения информации из Response response.result._artist
//        log.info(">>>>>>>>>>>> REQUEST TO LMS: " + json);
//                ОТПРАВИТЬ ОТВЕТ ОТ СЕРВЕРА В БРОКЕР
//        try {
//            log.info("TRY PUBLISH");
//            CombinedServer.publishToMqtt("test", "TO LMS: " + json);
//        } catch (MqttException e) {
//            throw new RuntimeException(e);
//        }

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
        log.info("RESPONSE LMS FINISH <<<<<<<<<<<<<<");
        return response;
    }

    public static String postToLmsForStatus(String json) {
        log.info("REQUEST TO LMS: " + json);

//        log.info(">>>>>>>>>>>> REQUEST TO LMS: " + json);
//                ОТПРАВИТЬ ОТВЕТ ОТ СЕРВЕРА В БРОКЕР
//        try {
//            log.info("TRY PUBLISH");
//            CombinedServer.publishToMqtt("test", "TO LMS: " + json);
//        } catch (MqttException e) {
//            throw new RuntimeException(e);
//        }

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

    public static String postToLmsForJsonBody(String json) {
//  все запросы плеера для получения информации из Response response.result._artist
        log.info("REQUEST TO LMS: " + json);

//        log.info(">>>>>>>>>>>> REQUEST TO LMS: " + json);
//                ОТПРАВИТЬ ОТВЕТ ОТ СЕРВЕРА В БРОКЕР
//        try {
//            log.info("TRY PUBLISH");
//            CombinedServer.publishToMqtt("test", "TO LMS: " + json);
//        } catch (MqttException e) {
//            throw new RuntimeException(e);
//        }

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

    public static void autoRemoteRefresh() {
        log.info("TASKER AUTO REMOTE REFRESH");
        String uri = "https://autoremotejoaomgcd.appspot.com/sendmessage?key=fovfKw-pC3A:APA91bFz1IHu4FIo9BpJaxwW0HgOulJtoXHF-khXptkSmn6QjhBIywkgi0-w9f4DvMK5y-hoOOTWsXDrv7ASE4S4BADhV8SQz6Y0XOJ5XWbF0pmprdOdmA7aEZ5hfQAWZ2Cd9RW_rShf&message=re";
        try {
            Request.Post(uri)
                    .execute();
        } catch (IOException e) {
            log.info("TASKER ERROR");
        }
    }


}