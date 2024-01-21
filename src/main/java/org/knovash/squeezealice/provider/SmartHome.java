package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.Device;

import java.util.LinkedList;

@Log4j2
@Data
public class SmartHome {

    //  направить пользователя после авторизации сюда
    public static String redirectUri = "https://unicorn-neutral-badly.ngrok-free.app/redirect";
    public static String user_id = "konstantin";
    public static LinkedList<Device> devices = new LinkedList<>();
    public static Integer channel;

    public static Integer fakeVolume = 1;
    public static Integer lastChannel = 1;
    public static boolean fakePlayPause = true;
    public static boolean fakeMute = false;
    public static boolean fakeOnOff = true;

    public static Device getByDeviceId(int deviceId) {
        String index = String.valueOf(deviceId);
        return devices.stream().filter(d -> d.id.equals(index)).findFirst().get();
    }
}