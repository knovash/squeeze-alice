package org.knovash.squeezealice.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientFactory {

    private static final HttpClient httpClient;

    static {
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(5000)   // время ожидания соединения из пула (мс)
                .setConnectTimeout(5000)             // таймаут установки соединения
                .setSocketTimeout(10000)             // таймаут чтения данных
                .build();

        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .disableCookieManagement()           // отключаем куки, чтобы избежать предупреждений о невалидных expires
                .setUserAgent("SqueezeAlice/1.0")
                .build();
    }

    public static HttpClient getHttpClient() {
        return httpClient;
    }
}