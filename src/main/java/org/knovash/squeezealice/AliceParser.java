package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Alice;
import org.knovash.squeezealice.requests.ResponseAlice;

@Log4j2
public class AliceParser {

    public static void parser(String command) {

    }

    public static String createResponse(String text) {
        Alice alice = new Alice();
        ResponseAlice responseAlice = new ResponseAlice();
//        responseAlice.text = "сейчас, мой господин";
        responseAlice.text = text;
        responseAlice.end_session = false;
        alice.version = "1.0";
        alice.response = responseAlice;
        String response = JsonUtils.pojoToJson(alice);
        log.info("RESPONSE JSON: " + response);
        return response;
    }
}