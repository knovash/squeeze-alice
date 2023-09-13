package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

import static org.knovash.squeezealice.Utils.altNames;

@Log4j2
public class Handler implements HttpHandler {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");

    @Override
    public void handle(HttpExchange httpExchange) {
        String actionStatus;
        String name = null;
        Player player = null;
        log.info(" ----===[ REQUEST ]===----");
        log.info("URI: " + httpExchange.getRequestURI().getQuery());
        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> parameters = new HashMap<>();
        Arrays.asList(query.split("&")).stream()
                .map(s -> s.split("="))
                .forEach(s -> parameters.put(s[0], s[1]));
        String action = parameters.get("action");
        log.info("ACTION: " + action);
        if (parameters.get("player") != null) {
            name = parameters.get("player");
            log.info("NAME: " + name);
            name = Utils.altPlayerName(name);
            log.info("ALT NAME: " + name);
            player = Server.playerByName(name);
            if (player == null) {
                log.info("NO PLAYER : " + name + " TRY UPDATE FROM LMS AND RETRY");
                Server.updatePlayers();
                player = Server.playerByName(name);
                if (player == null) {
                    log.info("NO PLAYER: " + name + " ON SERVER");
                    handleResponse(httpExchange, "ERROR: NO PLAYER IN LMS " + name + "Try check alt names: " + altNames);
                    return;
                }
            }
            if (player.isBlack()) {
                log.info("PLAYER: " + name + " IN BLACK");
                handleResponse(httpExchange, "PLAYER IN BLACKLIST " + name);
                return;
            }
        }

        switch (action) {
            case ("channel"):
                Action.channel(player, Integer.valueOf(parameters.get("value")));
                actionStatus = "ACTION COMPLETE";
                break;
            case ("volume"):
                Action.volume(player, parameters.get("value"));
                actionStatus = "ACTION COMPLETE";
                break;
            case ("all_low_high"):
                Action.allLowOrHigh(parameters.get("value"));
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turn_on_music"):
            case ("turn_on_speaker"):
                Action.turnOnMusicSpeakers(player);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turn_off_music"):
                Action.turnOffMusic();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("turn_off_speaker"):
                Action.turnOffSpeaker(player);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("update_players"):
                Server.updatePlayers();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("show_log"):
            case ("log"):
                log.info("SHOW LOG");
                actionStatus = Utils.logLastLines(parameters);
                break;
            case ("silence"):
                log.info("SILENCE");
                player.playSilence();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("change_value"):
                log.info("CHANGE PLAYER VALUE");
                Utils.changePlayerValue(parameters);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("alt_name_add"):
                log.info("ALT NAME ADD");
                Utils.altNameAdd(parameters);
                actionStatus = "ACTION COMPLETE";
                break;
            case ("remove"):
                log.info("REMOVE PLAYER");
                player.remove();
                actionStatus = "ACTION COMPLETE";
                break;
            case ("state"):
                log.info("SEND SERVER STATE");
                actionStatus = Utils.state();
                break;
            default:
                log.info("ACTION NOT FOUND: " + action);
                actionStatus = "ACTION NOT FOUND: " + action;
                break;
        }
        handleResponse(httpExchange, actionStatus);
    }

    private void handleResponse(HttpExchange httpExchange, String actionStatus) {
        OutputStream outputStream = httpExchange.getResponseBody();
        String htmlResponse = actionStatus;
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