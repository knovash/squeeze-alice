package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.Server;

import org.knovash.squeezealice.provider.pojoQuery.Query;
import org.knovash.squeezealice.provider.pojoQueryResponse.*;
import org.knovash.squeezealice.utils.HttpUtils;
import org.knovash.squeezealice.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class HandlerQuery implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("START -------------------------------");
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String host = HttpUtils.getHeaderValue(httpExchange, "Host");
        log.info("REQUEST: " + method + " " + "http://" + host + path);
        String xRequestId = HttpUtils.getHeaderValue(httpExchange, "X-request-id");
        String body = HttpUtils.httpExchangeGetBody(httpExchange);
        log.info("BODY: " + body);
        if (body == null) return;
        Query query = JsonUtils.jsonToPojo(body, Query.class);

        log.info("----- DEVICES: " + query.devices);
        log.info("----- ID: " + query.devices.get(0).id);
        int id = Integer.parseInt(query.devices.get(0).id);

        String json;
        if (body == null) {
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        } else {

            // запрос о состоянии девайса id=0
            int device_id = Integer.parseInt(query.devices.get(0).id);
            log.info("STATE FOR DEVICE ID: " + device_id +
                    " NAME: " + SmartHome.devices.get(device_id).name +
                    " NAME: " + SmartHome.getByDeviceId(device_id).name +
                    " LMS NAME: " + SmartHome.getByDeviceId(device_id).customData.lmsName);

            Response response = new Response();
            response.request_id = xRequestId;

            List<Device> jsonDevices = query.devices.stream().map(d -> updateDevice(Integer.valueOf(d.id))).collect(Collectors.toList());
            log.info("LIST DEV: " + jsonDevices);

            response.payload = new Payload();
            response.payload.devices = jsonDevices;
            log.info("LIST DEV: " + jsonDevices);
            json = JsonUtils.pojoToJson(response);
            json = json.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1 $2");

        }

        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(200, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("QUERY OK");
    }

    public static Device updateDevice(Integer device_id) {
        // обратиться к девайсу и обновить все его значения
        Player player = Server.playerByName(SmartHome.getByDeviceId(device_id).customData.lmsName);
        Integer volume = 0;
        Boolean power = false;
        if (player != null) {
            volume = Integer.valueOf(player.volume());
            String playerMode = player.mode();
            // TODO get channel saved in player
            player.mode();
            if (playerMode.equals("play")) power = true;
            log.info("POWER: " + playerMode + " power " + power);
        }
        Device device = new Device();
        log.info("device: " + device);

        Capability capability1 = new Capability();
        Capability capability2 = new Capability();
        Capability capability3 = new Capability();

        capability1.type = "devices.capabilities.range";
        capability1.state= new State();
        capability1.state.instance = "volume";
        capability1.state.value = String.valueOf(volume);
        log.info("device: " + capability1);

        capability2.type = "devices.capabilities.range";
        capability2.state= new State();
        capability2.state.instance = "channel";
        capability2.state.value = "2";

        capability3.type = "devices.capabilities.on_off";
        capability3.state= new State();
        capability3.state.instance = "on";
        capability3.state.value = String.valueOf(power);

        Capability capability4 = new Capability();
        capability4.type = "devices.capabilities.toggle";
        capability4.state= new State();
        capability4.state.instance = "pause";
        capability4.state.value = String.valueOf(power);

        device.capabilities = new ArrayList<>();

        device.capabilities.add(capability1);
        device.capabilities.add(capability2);
        device.capabilities.add(capability3);
//        device.capabilities.add(capability4);
        device.id = String.valueOf(device_id);
        log.info("device: " + device);

//log.info("DEV --- "+JsonUtils.pojoToJson(device));
//
//        String ddddd = "{\"id\":\"" + device_id + "\",\"capabilities\":[" +
//                "{\"type\":\"devices.capabilities.range\"," +
//                "\"state\":{\"instance\":\"volume\",\"value\":" + volume + "}}" +
//                "," +
//                "{\"type\":\"devices.capabilities.range\"," +
//
//                "\"state\":{\"instance\":\"channel\",\"value\":" + 2 + "}}" +
//                "," +
//                "{\"type\":\"devices.capabilities.on_off\"," +
//                "\"state\":{\"instance\":\"on\",\"value\":" + power + "}}" + "]}";
//        log.info("----TODO json " + ddddd);

        return device;
    }
}