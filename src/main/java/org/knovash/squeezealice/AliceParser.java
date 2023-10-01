package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.alice.Alice;
import org.knovash.squeezealice.requests.alice.ResponseAlice;

@Log4j2
public class AliceParser {

    public static String createResponse(String text) {
        Alice alice = new Alice();
        ResponseAlice responseAlice = new ResponseAlice();
        responseAlice.text = text;
        responseAlice.end_session = true;
        alice.version = "1.0";
        alice.response = responseAlice;
        String response = JsonUtils.pojoToJson(alice);
        log.info("RESPONSE JSON: " + response);
        return response;
    }
}