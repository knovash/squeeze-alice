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
//        log.info("QUERY PARAMETERS: " + parameters);
        String action = parameters.get("action");
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
                Action.low();
                break;
            case ("high"):
                Action.high();
                break;
            case ("turnon"):
                Action.turnon(name);
                break;
            case ("turnoff"):
                Action.turnoff();
                break;
            case ("update_players"):
                Action.updatePlayers();
                break;
            case ("update_favorites"):
                Action.updatePlayers();
                break;
            default:
                break;
        }
    }
}