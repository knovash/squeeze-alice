package org.knovash.squeezealice.auth;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.auth.LocalAuthBase;
import org.knovash.squeezealice.yandex.YandexJwtUtils;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class LocalAuthServerYandex extends LocalAuthBase {

    @Override
    protected String getDisplayName() {
        String token = config.yandexToken;
        if (token != null && !token.isEmpty()) {
            return YandexJwtUtils.getValueByTokenAndKey(token, "display_name");
        }
        return "";
    }

    @Override
    protected String getAuthUrl(String sessionId) {
        return "https://alice-lms.zeabur.app/authorize?state=" + sessionId;
    }

    @Override
    protected String getMqttAction() {
        return "token";
    }

    @Override
    protected String getLinkText() {
        return "Авторизоваться через Яндекс";
    }
}