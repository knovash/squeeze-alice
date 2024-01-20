package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.HttpUtils;

import java.util.HashMap;

@Log4j2
public class YandexAuth {

    public static Context action(Context context) {
        log.info("");
        String body = context.body;
        Headers headers = context.headers;
        String path = context.path;
        String xRequestId = context.xRequestId;
        String query = context.query;

        String scope = null;
        String state = null;
        String redirect_uri = null;
        String client_id = null;
        if (query != null) {
            HashMap<String, String> parameters = HttpUtils.getQueryMap(query);
            scope = HttpUtils.getValueFromMap(parameters, "scope");
            state = HttpUtils.getValueFromMap(parameters, "state");
            redirect_uri = HttpUtils.getValueFromMap(parameters, "redirect_uri");
            client_id = HttpUtils.getValueFromMap(parameters, "client_id");
            log.info("scope: " + scope);
            log.info("state: " + state);
            log.info("redirect_uri: " + redirect_uri);
            log.info("client_id: " + client_id);
        }
        String response = "REDIRECT";
        String code = "12345";
        String location = redirect_uri + "?client_id=" + client_id + "&state=" + state + "&code=" + code;
        log.info("redirectUri: " + location);

        headers.add("Location", location);
        context.headers = headers;
        context.json = response;
        context.code = 302;
        return context;
    }
}