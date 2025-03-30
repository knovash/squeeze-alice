package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import java.util.UUID;

@Log4j2
public class YandexToken {
    public static String userToken = "noname";

    public static Context action(Context context) {
        log.info("/TOKEN START");

        log.info("CONTEXT: " + context);

        userToken = generateSecureToken("fff");

        String json = " {\"access_token\":\"" + userToken + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";
        log.info("JSON"+json);
        context.bodyResponse = json;
        context.code = 200;

        log.info("/TOKEN FINISH");
        return context;
    }

    private static String generateSecureToken(String userId) {
        // Генерация криптостойкого токена с привязкой к пользователю
        return UUID.randomUUID().toString()
                + "_" 
//                + userId.hashCode()
                ;
    }

}