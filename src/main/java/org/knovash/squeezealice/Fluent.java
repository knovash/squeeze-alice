package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;

@Log4j2
public class Fluent {

    public static Content post(String json) {
        log.info("POST JSON: " + json);
        final Content postResult;
        try {
            postResult = Request
                    .Post("http://localhost:9000/jsonrpc.js")
//                    .Post("http://192.168.1.52:9000/jsonrpc.js")
                    .bodyString(json, ContentType.APPLICATION_JSON)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("RESPONSE: " + postResult.asString());
        return postResult;
    }
}