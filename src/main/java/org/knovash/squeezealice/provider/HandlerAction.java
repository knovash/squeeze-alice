package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Action;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.provider.pojo.State;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerAction implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST " + method + " " + "http://" + host + path);
        // получить хедеры
        log.info("HEADERS: " + httpExchange.getRequestHeaders().entrySet());
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
//        String query = httpExchange.getRequestURI().getQuery();
//        log.info("QUERY: " + query);

        String json;
        if (body != null) {

            // State to String .....
            String bodyState = State.jsonGetState(body); // достать State из body json
//            log.info("BODY STATE: " + bodyState);
            body = JsonUtils.replaceState(body, "\"" + bodyState + "\""); // заменить в body State на "State"
//            log.info("BODY REPLACED STATE TO STRING: " + body);


            int id = Integer.parseInt(JsonUtils.jsonGetValue(body, "id"));
            log.info("BODY ID: " + id);
            String value = JsonUtils.jsonGetValue(bodyState, "value"); // значение для изменеия состояния
            log.info("BODY VALUE: " + value);
            String relative = JsonUtils.jsonGetValue(bodyState, "relative"); // изм относительное да? нет?
            log.info("BODY RELATIVE: " + relative);
            String type = JsonUtils.jsonGetValue(body, "type"); // тип состояния
            log.info("BODY TYPE: " + type);
            String instance = JsonUtils.jsonGetValue(bodyState, "instance"); // тип состояния
            log.info("BODY INSTANCE: " + instance);
            log.info("DEVICE NAME " + SmartHome.devices.get(id).customData.lmsName);

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
            Player player = Server.playerByName(SmartHome.devices.get(id).description);
            switch (instance) {
                case ("volume"):
                    log.info("VOLUME: " + value);
                    if (relative != null && relative.equals("true")) {
                        player.volume("+" + value);
                    } else {
                        player.volume(String.valueOf(value));
                    }
                    break;
                case ("channel"):
                    log.info("CHANNEL: " + value + " LAST CHANNEL: " + SmartHome.lastChannel);
                    int channel;
                    if (relative != null && relative.equals("true")) {
                        channel = SmartHome.lastChannel + 1;
                    } else {
                        channel = Integer.parseInt(value);
                    }
                    Action.channel(player, channel);
                    SmartHome.lastChannel = channel;
                    break;
                case ("on"):
                    log.info("ON/OFF PLAY/PAUSE " + value);
                    if (value.equals("true")) Action.turnOnMusicSpeaker(player);
                    if (value.equals("false")) Action.turnOffSpeaker(player);

                    break;
            }

//        response.payload.devices.get(0).capabilities.get(0).state.action_result = new ActionResult();
//        response.payload.devices.get(0).capabilities.get(0).state.action_result.status = "DONE";
//        response.payload.devices.get(0).capabilities.get(0).state.action_result.error_code = null;
//        response.payload.devices.get(0).capabilities.get(0).state.action_result.error_message = null;
//        response.payload.devices.get(0).capabilities.get(0).state.value = value;
//        String json = JsonUtils.pojoToJson(response);


            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[{\"id\":\"" + id + "\",\n" +
                    "\"capabilities\":[{\"type\":\"" + type + "\",\n" +
                    "\"state\":{\"instance\":\"" + instance + "\"," +
                    "\"action_result\":{\"status\":\"DONE\"}}}]}]}}";
        } else {
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        }

        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("ACTION OK");
    }
}