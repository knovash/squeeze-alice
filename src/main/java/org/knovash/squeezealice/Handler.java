package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

@Log4j2
public class Handler implements HttpHandler {

    private static ResourceBundle bundle = ResourceBundle.getBundle("config");

    @Override
    public void handle(HttpExchange httpExchange) {
        log.info("\n\nREQUEST");
        log.info("URI: " + httpExchange.getRequestURI().getQuery());
        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> parameters = new HashMap<>();
        Arrays.asList(query.split("&")).stream()
                .map(s -> s.split("="))
                .forEach(s -> parameters.put(s[0], s[1]));
        String action = parameters.get("action").replace("_","");
        log.info("ACTION: " + action);
        String name = bundle.getString(parameters.get("player"));
        log.info("PLAYER: " + parameters.get("player") + " = " + name);

        switch (action) {
            case ("channel"):
                Action.channel(name, Integer.valueOf(parameters.get("value"))-1);
                break;
            case ("volume"):
                Action.volume(name, parameters.get("value"));
                break;
            case ("low"):
                Action.allLow();
                break;
            case ("high"):
                Action.allHigh();
                break;
            case ("turnonmusic"):
                Action.turnOnMusic(name);
                break;
            case ("turnoffmusic"):
                Action.turnOffMusic();
                break;
            case ("updateplayers"):
                Action.updatePlayers();
                break;
            case ("updatefavorites"):
                Action.updatePlayers();
                break;
            default:
                break;
        }
    }
}