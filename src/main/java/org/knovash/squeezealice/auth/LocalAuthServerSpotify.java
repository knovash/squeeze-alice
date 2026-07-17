package org.knovash.squeezealice.auth;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.auth.LocalAuthBase;

@Log4j2
public class LocalAuthServerSpotify extends LocalAuthBase {

    @Override
    protected String getDisplayName() {
        // Если нужно показывать имя пользователя Spotify, можно реализовать позже
        return "";
    }

    @Override
    protected String getAuthUrl(String sessionId) {
        return "https://alice-lms.zeabur.app/authorize_spotify?state=" + sessionId;
    }

    @Override
    protected String getMqttAction() {
        return "token_spotify";
    }

    @Override
    protected String getLinkText() {
        return "Авторизоваться в Spotify";
    }
}