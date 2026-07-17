package org.knovash.squeezealice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.spotify.SwitchSpotify;
import org.knovash.squeezealice.cmd.HandlePathCmd;
import org.knovash.squeezealice.provider.*;
import org.knovash.squeezealice.voice.HandlePathAlice;
import org.knovash.squeezealice.web.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
public class HandlerAll implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
//        log.info(Main.start);
        // --- НОВАЯ ОБРАБОТКА /music/ ---
        String path = httpExchange.getRequestURI().getPath();
//        log.info("HANDLE PATH: "+path);

        if (path.startsWith("/music/")) {
            serveSoundFile(httpExchange, path); // отдать звукаовой файл
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
//        log.info(Main.finish);
    }

    public static Context switchPath(Context context) {
        String path = context.path;
//        log.info("SWITCH CONTEXT PATH: " + path);
        switch (path) {
            case "/":
                return PageIndex.action(context);
            case "/players":
                return PagePlayers.action(context);
            case "/manual":
                return PageManual.action(context);
            case "/lms":
                return PageLms.action(context);
            case "/cmd":
                context = HandlePathCmd.action(context);
                break;
            case "/spotify":
                context = SwitchSpotify.action(context);
                break;
            case "/alice/":
                context = HandlePathAlice.processContext(context);
                break;
            case "/v1.0":
                context = ProviderCheck.providerCheckRun(context);
                break;
            case "/v1.0/user/unlink":
                context = ProviderUserUnlink.providerUserUnlinkRun(context);
                break;
            case "/v1.0/user/devices":
                context = ProviderUserDevices.providerUserDevicesRun(context);
                break;
            case "/v1.0/user/devices/query":
                context = ProviderQuery.providerQueryRun(context);
                break;
            case "/v1.0/user/devices/action":
                context = ProviderAction.providerActionRun(context);
                break;
            default:
                log.info("PATH ERROR " + path);
                context = PageIndex.action(context);
                break;
        }
        return context;
    }

    private static void serveSoundFile(HttpExchange exchange, String path) throws IOException {
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        File file = new File("/home/music/" + fileName);
//        log.info("SOUND FILE: " + fileName);
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