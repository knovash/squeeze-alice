package org.knovash.squeezealice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.spotify.SpotifyPlayerCommands;
import org.knovash.squeezealice.cmd.HandlePathCmd;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.voice.HandleVoiceAlice;
import org.knovash.squeezealice.web.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.knovash.squeezealice.web.ActionsRunner.actionsRunner;
import static org.knovash.squeezealice.web.Settings.settings;
import static org.knovash.squeezealice.web.SettingsButtons.settingsButtons;
import static org.knovash.squeezealice.web.SettingsLms.settingsLms;
import static org.knovash.squeezealice.web.SettingsTasker.settingsTasker;
import static org.knovash.squeezealice.web.SettingsVoice.settingsVoice;

@Log4j2
public class HandlerAll implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String path = httpExchange.getRequestURI().getPath();

        if (path.startsWith("/music/")) {
            serveSoundFile(httpExchange, path); // отдать звуковой файл для уведомлений
            return;
        }

        Context context = Context.contextCreate(httpExchange);
        log.debug("CLIENT ID IN QUERY: " + context.queryMap.get("client_id"));
        context = HandlerAll.switchPath(context);
        String response = context.bodyResponse;

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().putAll(context.responseHeaders);
        httpExchange.sendResponseHeaders(context.code, responseBytes.length);
        try (OutputStream outputStream = httpExchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    public static Context switchPath(Context context) {
        String path = context.path;
        switch (path) {
            //    YANDEX PROVIDER управление устройствами умного дома
            case "/v1.0":
                return ProviderCheck.providerCheckRun(context);
            case "/v1.0/user/unlink":
                return ProviderUserUnlink.providerUserUnlinkRun(context);
            case "/v1.0/user/devices":
                return ProviderUserDevices.providerUserDevicesRun(context);
            case "/v1.0/user/devices/query":
                return ProviderQuery.providerQueryRun(context);
            case "/v1.0/user/devices/action":
                return ProviderAction.providerActionRun(context);
            //    YANDEX VOICE команды дополнительного голосового навыка
            case "/alice/":
                return HandleVoiceAlice.processContext(context);
            //    COMMANDS команды из Tasker или Пульта
            case "/cmd":
                return HandlePathCmd.action(context);
            // WEB
            case "/":
                return PageIndex.action(context);
            case "/players":
                return PagePlayers.action(context);
            case "/settings":
                return settings.action(context);
            case "/settings_tasker":
                return settingsTasker.action(context);
            case "/settings_voice":
                return settingsVoice.action(context);
            case "/actions":
                return actionsRunner.action(context);
            case "/settings_buttons":
                return settingsButtons.action(context);
            case "/settings_lms":
                return settingsLms.action(context);
            case "/manual":
                return PageManual.action(context);
            case "/lms":
                return PageLms.action(context);
            //    SPOTIFY управление плеером
            case "/spotify":
                return SpotifyPlayerCommands.action(context);
            default:
                log.info("WARNING! NOT FOUND PATH: " + path);
                return PageIndex.action(context);
        }
    }

    private static void serveSoundFile(HttpExchange exchange, String path) throws IOException {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        File file = new File("/home/music/" + fileName);
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        exchange.getResponseHeaders().set("Content-Type", "audio/mpeg");
        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }
    }
}