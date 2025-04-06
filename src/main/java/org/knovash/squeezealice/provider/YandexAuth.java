package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.HashMap;

import static org.knovash.squeezealice.provider.Yandex.yandex;

import java.net.URLEncoder;
        import java.nio.charset.StandardCharsets;
        import java.security.SecureRandom;
        import java.util.Base64;
        import java.util.Map;
@Log4j2
public class YandexAuth {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static Context action(Context context) {
        try {
            // 1. Валидация входных параметров
            Map<String, String> params = context.queryMap;
            String redirectUri = params.get("redirect_uri");
            String state = params.get("state");
            String clientId = params.get("client_id");

            if (!validateParams(redirectUri, state, clientId)) {
                context.code = 400;
                context.bodyResponse = "invalid_request";
                return context;
            }

            // 2. Генерация безопасного кода
            String authCode = generateSecureCode();

            // 3. Кодирование параметров
            String encodedLocation = buildEncodedRedirectUrl(redirectUri, state, clientId, authCode);

            // 4. Настройка ответа
            Headers headers = new Headers();
            headers.add("Location", encodedLocation);

            context.code = 302;
            context.headers = headers;
            context.bodyResponse = "Redirecting...";

            // 5. Сохранение кода (реализовать отдельно)
            storeAuthorizationCode(authCode, clientId);

            return context;

        } catch (Exception e) {
            context.code = 500;
            context.bodyResponse = "server_error";
            return context;
        }
    }

    private static boolean validateParams(String redirectUri, String state, String clientId) {
        // Валидация обязательных полей
        if (redirectUri == null || state == null || clientId == null) {
            return false;
        }

        // Проверка зарегистрированного client_id и redirect_uri
        return isValidClient(clientId) && isValidRedirectUri(clientId, redirectUri);
    }

    private static String generateSecureCode() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private static String buildEncodedRedirectUrl(String baseUrl, String state,
                                                  String clientId, String code) throws Exception {
        return baseUrl +
                "?state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) +
                "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
    }

    // Пример заглушек для проверок (реализовать отдельно)
    private static boolean isValidClient(String clientId) {
        return "9aa97fffe29849bb945db5b82b3ee015".equals(clientId);
    }

    private static boolean isValidRedirectUri(String clientId, String redirectUri) {
        return "https://social.yandex.net/broker/redirect".equals(redirectUri);
    }

    private static void storeAuthorizationCode(String code, String clientId) {
        // Реализация хранения кода с привязкой к клиенту и времени жизни
    }


    public static Context action2(Context context) {
        log.info("REQUEST AUTH REDIRECT");

        log.info("REDIRECT: " + context.queryMap.get("redirect_uri"));
        context.bodyResponse = "REDIRECT";
        context.code = 302;
        String redirectUri = context.queryMap.get("redirect_uri");
        log.info("REDIRECT URI FROM QUERY: " + redirectUri);
        String state = context.queryMap.get("state");
        log.info("STATE FROM QUERY: " + state);

        String location = redirectUri + "?" + // обязательно (если рандом стр - ошибка)
                "state=" + state +  // обязательно (если рандом стр - ошибка)
              "&client_id=" + context.queryMap.get("client_id") + // необязательно любой стринг
                "&code=" + "scope"; // обязательно! неважночто
        log.info("REDIRECTURI: " + location);
        Headers headers = new Headers();
        headers.add("Location", location);
        context.headers = headers;
        return context;
    }

    public static void clientIdSave(HashMap<String, String> parameters) {
        log.info("CLIENT ID SAVE PARAMETERS: " + parameters);
        yandex.clientId = parameters.get("yandex_clientid");
        log.info("clientId: " +  yandex.clientId);
        writeCredentialsYandex();
    }

    public static void bearerSave(HashMap<String, String> parameters) {
        log.info("BEARER SAVE PARAMETERS: " + parameters);
        yandex.bearer  = parameters.get("yandex_bearer");
        log.info("clientId: " +  yandex.bearer);
        writeCredentialsYandex();
    }

    public static void writeCredentialsYandex() {
        log.debug("TRY WRITE YANDEX CREDENTIALS TO yandex.json");
        log.info("YANDEX: " + yandex);
//        yandex.clientId = parameters.get("client_id");
//        yandex.clientSecret = parameters.get("client_secret");
//        yandex.bearer = getBearerToken();
        JsonUtils.pojoToJsonFile(yandex, "yandex.json");
        log.info("WRITE YANDEX CREDENTIALS TO yandex.json");
    }
}