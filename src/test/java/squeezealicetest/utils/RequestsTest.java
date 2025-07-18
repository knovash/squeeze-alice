package squeezealicetest.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static squeezealicetest.utils.MainTest.configTest;

@Log4j2
public class RequestsTest {

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
            content = Request.Post("http://" + "192.168.1.110" + ":" + "9000" + "/jsonrpc.js/").bodyString(json, ContentType.APPLICATION_JSON)
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

    public static String postToLmsForStatus(String json) {
//        log.info("REQUEST TO LMS: " + json);

        String status = null;
        try {
            status = Request.Post("http://" + "192.168.1.110" + ":" + "9000" + "/jsonrpc.js/").bodyString(json, ContentType.APPLICATION_JSON)
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
//        log.info("REQUEST TO LMS: " + json);

        Content content = null;
        Response response = null;
        try {
            content = Request.Post("http://" + "192.168.1.110" + ":" + "9000" + "/jsonrpc.js/").bodyString(json, ContentType.APPLICATION_JSON)
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
// запрос обновления виджетов таскера выполняется при
// действии пульта или таскера SwitchQueryCommand
// действии приложения Умного дома ProviderAction
// SwitchVoiceCommand тут есть действия pleer и надо добавить после них autoRemoteRefresh
        log.info("REQUEST TO TASKER AUTO REMOTE FOR REFRESH");
//        String uri = lmsPlayers.autoRemoteDevicesUris;

        lmsPlayers.autoRemoteUrls.stream().forEach(uri ->{
            log.info("POST REFRESH TO AUTOREMOTE URI: " + uri);
            if (uri == null) return;
            try {
                Request.Post(uri)
                        .execute();
            } catch (IOException e) {
                log.info("TASKER ERROR");
            }
        });
    }
}























