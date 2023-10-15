package org.knovash.squeezealice.provider;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.pojo.Device;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
public class Yandex {

//    мое приложение
//    https://oauth.yandex.ru/client/0d17cba2ab254d838ac1ddcedabc4191

    public static String bearerToken = "y0_AgAAAAAYbWLzAAqd-QAAAADuroaJKlTGViNIS5KOAPmaK7spjdDQCBw";
//  идентификатор приложения для получения OAuth token
    public static String clientId = "0d17cba2ab254d838ac1ddcedabc4191";
    public static String clientSecret = "b0966cd53b9647b9989bd20a3c9140d8";
//  направить пользователя после авторизации сюда
    public static String redirectUri = "https://sqtest.loca.lt/redirect";
    public static String user_id = "konstantin";
    public static List<Device> devices = new ArrayList<>();

}