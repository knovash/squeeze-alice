package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerQuery implements HttpHandler {

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
//        String query = httpExchange.getRequestURI().getQuery();
//        log.info("QUERY: " + query);

        // запрос о состоянии девайса id=0
//        BODY: {"devices":[{"id":"0"}]}
        Integer id = Integer.valueOf(JsonUtils.jsonGetValue(body, "id"));
        log.info("QUERY FOR STATE DEVICE ID: " + id +
                " NAME: " + Home.devices.get(id).name +
                " LMS NAME: " + Home.devices.get(id).customData.lmsName);

        // обратиться к девайсу и обновить все его значения
        Player player = Server.playerByName(Home.devices.get(id).customData.lmsName);
        String volume = player.volume();
        log.info("VOLUME: " + volume);

        String playerMode = player.mode();
        Boolean mode = false;
        Boolean power = false;
        Boolean mute = false;
        if (playerMode.equals("play")) mode = true;
        if (playerMode.equals("play")) power = true;
        if (!playerMode.equals("play")) mute = true;
        log.info("MODE: " + playerMode + " mode " + mode);
        log.info("POWER: " + playerMode + " power " + power);
        log.info("MUTE: " + playerMode + " mute " + mute);

//        Device device = Yandex.devices.get(id);
//        device.capabilities.get(0).state.value = Integer.parseInt(volume);
//        Response response = new Response();
//        response.request_id = xRequestId;
//        response.payload.devices.add(device);
//        String json = JsonUtils.pojoToJson(response);

//        log.info("RESPONSE FAKE: " + json);
//
        String json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[{" +
                "\"id\":\"" + id + "\"," +
                "\"capabilities\":[{" +
                "\"type\":\"devices.capabilities.range\"," +
                "\"state\":{\"instance\":\"volume\",\"value\":" + volume + "}" +
                "," +
                "\"type\":\"devices.capabilities.range\"," +
                "\"state\":{\"instance\":\"channel\",\"value\":" + Home.channel + "}" +
                "," +
                "\"type\":\"devices.capabilities.toggle\"," +
                "\"state\":{\"instance\":\"pause\",\"value\":" + mode + "}" +
                "," +
                "\"type\":\"devices.capabilities.on_off\"," +
                "\"state\":{\"instance\":\"on\",\"value\":" + power + "}" +
                "," +
                "\"type\":\"devices.capabilities.toggle\"," +
                "\"state\":{\"instance\":\"mute\",\"value\":" + mute + "}" +
                "}]}]}}";

        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("QUERY OK");
    }
}