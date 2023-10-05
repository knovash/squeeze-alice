package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.JsonUtils;
import org.knovash.squeezealice.pojo.alice.Alice;
import org.knovash.squeezealice.pojo.alice.ResponseAlice;

@Log4j2
public class CreateResponse {

    public static String createResponse(String text) {
        Alice alice = new Alice();
        ResponseAlice responseAlice = new ResponseAlice();
        responseAlice.text = text;
        alice.version = "1.0";
        alice.response = responseAlice;
        String response = JsonUtils.pojoToJson(alice);
        log.info("RESPONSE JSON: " + response);
        return response;
    }
}