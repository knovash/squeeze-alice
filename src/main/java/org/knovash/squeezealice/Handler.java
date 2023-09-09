package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

@Log4j2
public class Handler implements HttpHandler {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");

    @Override
    public void handle(HttpExchange httpExchange) {
        log.info(" ----===[ REQUEST ]===----");
        log.info("URI: " + httpExchange.getRequestURI().getQuery());
        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> parameters = new HashMap<>();
        Arrays.asList(query.split("&")).stream()
                .map(s -> s.split("="))
                .forEach(s -> parameters.put(s[0], s[1]));
        String action = parameters.get("action").replace("_", "");

        String name = null;
        Player player = null;
        if (parameters.get("player") != null) {
            name = parameters.get("player");
// проверка альтернативных имен плееров
            if (bundle.containsKey(name)) {
                name = bundle.getString(name);
            } else {
                log.info("NO ALTER NAME FOR " + name);
            }
            player = Server.playerByName(name);
            if (player == null) {
                log.info("NO PLAYER: " + name + " TRY UPDATE FROM SERVER");
                Server.updatePlayers();
                player = Server.playerByName(name);
                if (player == null) {
                    log.info("NO PLAYER: " + name + " ON SERVER");
                    return;
                }
            }
            if (player.isBlack()) {
                log.info("PLAYER: " + name + " IN BLACK");
                return;
            }
        }

        String actionStatus;

        switch (action) {
            case ("channel"):
                Action.channel(player, Integer.valueOf(parameters.get("value")));
                actionStatus = "ACTION COMPLETE";
                break;
            case ("volume"):
                Action.volume(player, parameters.get("value"));
                actionStatus = "ACTION COMPLETE";
                break;
            case ("alllowhigh"):
                Action.allLowHigh(parameters.get("value"));
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turnonmusic"):
                Action.turnOnMusic(player);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turnoffmusic"):
                Action.turnOffMusic();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turnonspeaker"):
                Action.turnOnSpeaker(player);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turnoffspeaker"):
                Action.turnOffSpeaker(player);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("updateplayers"):
                Server.updatePlayers();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("updatefavorites"):
                Server.updatePlayers();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("silence"):
                log.info("SILENCE");
                player.playSilence();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("changevalue"):
                log.info("CHANGE PLAYER VALUE");
                Utils.changePlayerValue(parameters);
                actionStatus = "ACTION COMPLETE";
                break;
            default:
                actionStatus = "ACTION NOT FOUND";
                break;
        }
        handleResponse(httpExchange, actionStatus);
    }

    private void handleResponse(HttpExchange httpExchange, String actionStatus) {
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = "OK " + actionStatus ;
        try {
            httpExchange.sendResponseHeaders(200, htmlResponse.length());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            outputStream.write(htmlResponse.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}