package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.knovash.squeezealice.Main.port;

@Log4j2
public class Server {

    public static void start() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.createContext("/", new Handler());

//        Диалог Раз Два
//        https://dialogs.yandex.ru/developer/skills/f2c654d9-d4e7-42c8-94d5-1b058e1d5afe/settings/main
//        Backend WebHook URL
//        https://unicorn-neutral-badly.ngrok-free.app/alice/
//        server.createContext("/alice", new Handler());

//        Диалог Squeezebox LMS
//        Backend WebHook URL
//        https://dialogs.yandex.ru/developer/skills/53f7314b-b845-4ec3-9a09-49aaff2e5198/settings/main
//        server.createContext("/v1.0", new Handler());
//        server.createContext("/v1.0/user/devices/query", new Handler());
//        server.createContext("/v1.0/user/devices/action", new Handler());
//        server.createContext("/v1.0/user/unlink", new Handler());
//        server.createContext("/v1.0/user/devices", new Handler());
//        server.createContext("/auth", new Handler());
//        server.createContext("/token", new Handler());
//        server.createContext("/refresh", new Handler());
//        server.createContext("/bearer", new Handler());

//        Управление через http query запросы
//        server.createContext("/cmd", new Handler());

//        Веб интерфейс настроек
//        server.createContext("/spotify", new Handler());
//        server.createContext("/yandex", new Handler());
//        server.createContext("/speakers", new Handler());
//        server.createContext("/players", new Handler());

        server.setExecutor(null);
        server.start();
        log.info("http://localhost:" + port);
    }
}