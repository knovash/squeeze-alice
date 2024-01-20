package org.knovash.squeezealice.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.LmsPlayers;
//import org.knovash.squeezealice.provider.pojoQueryResponse.*;
import org.knovash.squeezealice.provider.pojoQueryResponse.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class ActionQuery {

    public static Context action(Context context) {
        log.info("1");
        String body = context.body;
        String xRequestId = context.xRequestId;
        Root bodyPojo = JsonUtils.jsonToPojo(body, Root.class);
        String json;
        log.info("2");
        if (body == null) {
            json = "{\"request_id\":\"" + xRequestId + "\",\"payload\":{\"devices\":[]}}";
        } else {
            log.info("3");
            log.info("bodyPojo" + bodyPojo);
            log.info("bodyPojo" + bodyPojo.devices.size());
            // запрос о состоянии девайса id=0
            int device_id = Integer.parseInt(bodyPojo.devices.get(0).id);
            log.info("STATE FOR DEVICE ID: " + device_id +
                    " NAME: " + SmartHome.devices.get(device_id).name +
                    " NAME: " + SmartHome.getByDeviceId(device_id).name +
                    " LMS NAME: " + SmartHome.getByDeviceId(device_id).customData.lmsName);

            Response response = new Response();
            response.request_id = xRequestId;
            List<Device> jsonDevices = bodyPojo.devices.stream().map(d -> updateDevice(Integer.valueOf(d.id))).collect(Collectors.toList());
            log.info("LIST DEV: " + jsonDevices);
            response.payload = new Payload();
            response.payload.devices = jsonDevices;
            log.info("LIST DEV: " + jsonDevices);
            json = JsonUtils.pojoToJson(response);
            json = json.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1 $2");

            context.json = json;
            context.code = 200;
        }
        return context;
    }

    public static Device updateDevice(Integer device_id) {
        // обратиться к девайсу и обновить все его значения
        Player player = LmsPlayers.playerByName(SmartHome.getByDeviceId(device_id).customData.lmsName);
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
        capability1.state = new State();
        capability1.state.instance = "volume";
        capability1.state.value = String.valueOf(volume);
        log.info("device: " + capability1);

        capability2.type = "devices.capabilities.range";
        capability2.state = new State();
        capability2.state.instance = "channel";
        capability2.state.value = "2";

        capability3.type = "devices.capabilities.on_off";
        capability3.state = new State();
        capability3.state.instance = "on";
        capability3.state.value = String.valueOf(power);

        Capability capability4 = new Capability();
        capability4.type = "devices.capabilities.toggle";
        capability4.state = new State();
        capability4.state.instance = "pause";
        capability4.state.value = String.valueOf(power);

        device.capabilities = new ArrayList<>();

        device.capabilities.add(capability1);
        device.capabilities.add(capability2);
        device.capabilities.add(capability3);
//        device.capabilities.add(capability4);
        device.id = String.valueOf(device_id);
        log.info("device: " + device);
        return device;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Log4j2
    public static class Root{
        public ArrayList<Device4> devices;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Log4j2
    public static class Device4{
        public String id;
    }


}