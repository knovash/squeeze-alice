package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.ResourceBundle;

import static org.knovash.squeezealice.Main.argsLMS;

@Log4j2
public class Fluent {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");
    private static final String LMS = bundle.getString("lms");

    public static Response post(String json) {
        log.info("REQUEST TO LMS: " + json);
        Response postResult;
        if (argsLMS == null){ argsLMS = LMS;}
        try {
            postResult = Request.Post(argsLMS).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute();
        } catch (IOException e) {
            log.info("ERROR FLUENT POST " + e);
            postResult = null;
//            throw new RuntimeException(e);
        }
        return postResult;
    }
}