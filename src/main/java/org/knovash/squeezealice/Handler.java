package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.Player;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

@Log4j2
public class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("\nREQUEST\n");
        log.info("URI " + httpExchange.getRequestURI().getQuery());
        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> parameters = new HashMap<>();
        Arrays.asList(query.split("&")).stream()
                .map(s -> s.split("="))
                .forEach(s -> parameters.put(s[0], s[1]));
        log.info("QUERY PARAMETERS: " + parameters);
        String action = String.valueOf(parameters.get("action"));
        log.info("ACTION: " + action);

        //ACTION        PLAYER      VALUE

        //turnon        homepod                     Алиса включи музыку
        //turnoff       homepod                     Алиса выключи музыку
        //channel       homepod     1/2/3.../9      Алиса включи канал --
        //volume        homepod     1/2/.../100     Алиса музыку громче/тише
        //high          homepod                     Алиса все громко
        //low           homepod                     Алиса все тихо
        //spotify       homepod                     Алиса ключи Спотифай (трансфер на колонку в комнате)

        String response = "";

        switch (action) {
            case ("channel"):
                Action.channel(parameters.get("player"), parameters.get("value"));
                break;
            case ("volume"):
                Action.volume(parameters.get("player"), parameters.get("value"));
                break;
            case ("low"):
                Action.low();
                break;
            case ("high"):
                Action.high();
                break;
            case ("turnon"):
                Action.turnon(parameters.get("player"));
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
            case ("get_state"):
//                response = Player.stateget(parameters.get("player"));
                response = "{\"value\":true}";
                break;
            case ("get_volume"):
//                response = Player.volumeget(parameters.get("player"));
                response = "{\"value\":12}";
                break;
            case ("get_channel"):
//                response = Player.channelget(parameters.get("player"));
                response = "{\"value\":3}";
                break;
            default:
                break;
        }
        handleResponse(httpExchange, response);
    }

    private void handleResponse(HttpExchange httpExchange, String response) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
//        String response = "{\"value\":\"4\"}";
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, response.length());
        log.info("MY RESPONSE: " + response);
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}