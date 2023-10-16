package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.provider.pojo.Response;
import org.knovash.squeezealice.provider.pojo.State;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerAction implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info("REQUEST:" +
                " method: " + httpExchange.getRequestMethod() +
                " path: https://squeeze.serveo.net" + httpExchange.getRequestURI().getPath());
        // получить хедеры
//        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String authorization = HttpUtils.getHeaderValue(httpExchange, "Authorization");
        String contentType = HttpUtils.getHeaderValue(httpExchange, "Content-Type");
//        log.info("HEADER X-request-id : " + xRequestId);
//        log.info("HEADER Authorization : " + authorization);
//        log.info("HEADER Content-Type : " + contentType);
        // получить боди
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        // получить кюри
        String query = httpExchange.getRequestURI().getQuery();
//        log.info("QUERY: " + query);


        // запрос на изменение сосотояния

        // State to String .....
        String bodyState = State.jsonGetState(body); // достать State из body json
        log.info("BODY STATE: " + bodyState);
        body = JsonUtils.replaceState(body, "\"" + bodyState + "\""); // заменить в body State на "State"
        log.info("BODY REPLACED STATE TO STRING: " + body);


        Integer id = Integer.valueOf(JsonUtils.jsonGetValue(body, "id")); // значение для изменеия состояния
        String value = JsonUtils.jsonGetValue(bodyState, "value"); // значение для изменеия состояния
        String type = JsonUtils.jsonGetValue(body, "type"); // тип состояния
        String instance = JsonUtils.jsonGetValue(bodyState, "instance"); // тип состояния

        log.info("BODY ID: " + id);
        log.info("BODY VALUE: " + value);
        log.info("BODY TYPE: " + type);
        log.info("BODY INSTANCE: " + instance);
        log.info("Yandex Device ID: " + id + " " + Home.devices.get(id).name
                + " " + Home.devices.get(id).customData.lmsName);
        log.info("ACTION INSTANCE: " + instance + " VALUE: " + value);


//        Response response = JsonUtils.jsonToPojo(body, Response.class); //
//        log.info("ACTION BODY: " + response);
//        Integer id = Integer.valueOf(response.payload.devices.get(0).id);
//        log.info("Yandex Device ID: " + id + " " + Home.devices.get(id).name
//                + " " + Home.devices.get(id).customData.lmsName);
//        String state = response.payload.devices.get(0).capabilities.get(0).state;
//        Integer value = Integer.valueOf(State.getValue(state, "value"));
//        String instance = State.getValue(state, "value");

//        Integer value = response.payload.devices.get(0).capabilities.get(0).state.value;
//        String instance = response.payload.devices.get(0).capabilities.get(0).state.instance;
//        log.info("ACTION INSTANCE: " + instance + " VALUE: " + value);


        // обратиться к девайсу и изменить его состояние
        Player player = Server.playerByName(Home.devices.get(id).description);
        switch (instance) {
            case ("volume"):
                log.info("VOLUME: " + value);
                player.volume(String.valueOf(value));
                break;
            case ("channel"):
                log.info("CHANNEL: " + value + "HOME CHANNEL: " + Home.channel);
                Home.channel = Integer.valueOf(value);
                player.play(Integer.valueOf(value));
                break;
            case ("pause"):
                log.info("PLAY/PAUSE " + value);
                if (value == "false") player.play();
                if (value == "true") player.pause();
                break;
            case ("on"):
                log.info("ON/OFF PLAY/PAUSE " + value);
                if (value == "true") player.play();
                if (value == "false") player.pause();
                break;
            case ("mute"):
                log.info("MUTE " + value);
                if (value == "false") player.play();
                if (value == "true") player.pause();
                break;
        }

//        response.payload.devices.get(0).capabilities.get(0).state.action_result = new ActionResult();
//        response.payload.devices.get(0).capabilities.get(0).state.action_result.status = "DONE";
//        response.payload.devices.get(0).capabilities.get(0).state.action_result.error_code = null;
//        response.payload.devices.get(0).capabilities.get(0).state.action_result.error_message = null;
//        response.payload.devices.get(0).capabilities.get(0).state.value = value;
//
//        String json = JsonUtils.pojoToJson(response);
//        log.info("RESPONSE FAKE: " + json);

        String json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[{\"id\":\"" + id + "\",\n" +
                "\"capabilities\":[{\"type\":\"" + type + "\",\n" +
                "\"state\":{\"instance\":\"" + instance + "\",\"action_result\":{\"status\":\"DONE\"}}}]}]}}";

//        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("ACTION OK");
    }
}