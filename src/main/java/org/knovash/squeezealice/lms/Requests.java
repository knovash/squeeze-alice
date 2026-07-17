package org.knovash.squeezealice.lms;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.http.HttpResponseResult;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class Requests {

    private static final HttpClientWrapper httpClient = new HttpClientWrapper();

    public static Response postToLmsForResponse(String json) {
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        log.info(uri + " " + json);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        HttpResponseResult result = httpClient.doPost(uri, json, headers);
        if (result.isSuccess()) {
            String body = result.getBody();
            if (body == null) return null;
            return JsonUtils.jsonToPojo(body, Response.class);
        } else {
            log.error("LMS request failed: " + result.getStatusCode());
            return null;
        }
    }

    public static String postToLmsForStatus(String json) {
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        log.info(uri + " " + json);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        HttpResponseResult result = httpClient.doPost(uri, json, headers);
        if (result.isSuccess()) {
            return "HTTP/1.1 " + result.getStatusCode() + " OK";
        } else {
            log.info("ERROR " + uri);
            return null;
        }
    }

    public static String postToLmsForStatusNoLog(String json) {
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        HttpResponseResult result = httpClient.doPost(uri, json, headers);
        if (result.isSuccess()) {
            return "HTTP/1.1 " + result.getStatusCode() + " OK";
        } else {
            return null;
        }
    }

    public static String postToLmsForJsonBody(String json) {
        String uri = "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/";
        log.info(uri + " " + json);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        HttpResponseResult result = httpClient.doPost(uri, json, headers);
        if (result.isSuccess()) {
            return result.getBody();
        } else {
            log.debug("LMS request error: " + result.getStatusCode());
            return null;
        }
    }
}