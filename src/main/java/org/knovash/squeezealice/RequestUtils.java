package org.knovash.squeezealice;
import java.util.Arrays;

public class RequestUtils {

    public static RequestData create(String playerName, String[] params) {
        /** request sample {"id": 1, "method": "slim.request", "params":["HomePod", ["mixer", "volume", "?"]]} */
        RequestData request = new RequestData();
        request.setId("1");
        request.setMethod("slim.request");
        request.setParams(Arrays.asList(playerName, Arrays.asList(params)));
        return request;
    }
}
