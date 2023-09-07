package org.knovash.squeezealice;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.ResourceBundle;

@Log4j2
public class Fluent {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    private static final String LMS = bundle.getString("lms");

    public static Response post(String json)  {
        log.info("REQUEST TO LMS: " + json);
        Response postResult = null;
        try {
            postResult = Request.Post(LMS).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute();
//                    .returnResponse();
//                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR FLUENT POST " + e);
            throw new RuntimeException(e);
        }
        return postResult;
    }
}