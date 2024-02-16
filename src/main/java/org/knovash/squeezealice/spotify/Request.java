package org.knovash.squeezealice.spotify;

import org.apache.http.Header;
import org.apache.http.client.fluent.Response;
import org.apache.http.message.BasicHeader;

import java.io.IOException;

public class Request {

    public static String requestForJson(String uri, Header[] headers) {
        Response response;
        String json;
        try {
            response = org.apache.http.client.fluent.Request.Post(uri)
                    .setHeaders(headers)
                    .execute();
            json = response.returnContent().asString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }


}
