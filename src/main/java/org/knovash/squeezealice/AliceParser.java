package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Alice;
import org.knovash.squeezealice.requests.ResponseAlice;

@Log4j2
public class AliceParser {

    public static void parser(String command) {

    }


    public static String getResponse() {

//        String response = "{\"response\":{\"text\":\"ответ\"}, \"end_session\": false, \"version\":\"1.0\"}";
//        String response = "{'response':{'text':' '}, 'end_session': 'false', 'version':'1.0'}";
//        String response = "{\"response\":{\"text\":\"Здравствуйте!\",\"end_session\":false},\"version\":\"1.0\"}";
//        String response = "ssssssssssssss";

        Alice alice = new Alice();
        ResponseAlice responseAlice = new ResponseAlice();
        responseAlice.text = "1 2 3 ффф 5";
        responseAlice.end_session = false;
        alice.version = "1.0";
        alice.response = responseAlice;

        String response = JsonUtils.pojoToJson(alice);

        log.info("RESPONSE JSON: " + response);

        return response;

    }


}

//{"response":{"text":"Здравствуйте!","tts":"Здравствуйте!"},"version":"1.0"}

//{ "response":{"text":"Здравствуйте!","end_session":false,"version":"1.0"}}

//{"response":{"text":"Здравствуйте!","end_session":false,"directives":{}},"version":"1.0"}