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
        //channel       homepod     1/2/3.../9      Алиса включи канал --
        //volume        homepod     1/2/.../100     Алиса музыку громче/тише
        //preset_high   homepod     high            Алиса все громко
        //preset_low    homepod     low             Алиса все тихо
        //turnon        homepod                     Алиса включи музыку (включить/подключить колонку в комнате)
        //turnoff       homepod                     Алиса выключи музыку (все плееры на паузу)
        //spotify       homepod                     Алиса ключи Спотифай (трансфер на колонку в комнате)

        switch (action) {
            case ("channel"):
                Action.channel(parameters.get("player"), parameters.get("channel"));
                break;
            case ("volume"):
                Player.volumeSet(parameters.get("player"), parameters.get("value"));
                break;
            case ("preset_low"):
                Action.presetLow();
                break;
            case ("preset_high"):
                Action.presetHigh();
                break;
            case ("turnon"):
                Action.turnOnMusic(parameters.get("player"));
                break;
            case ("turnoff"):
                Action.stopAll();
                break;
            case ("spotify"):
                break;
            case ("sync"):
                break;
            case ("unsync"):
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
        handleResponse(httpExchange);
    }

    private void handleResponse(HttpExchange httpExchange) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        String response = "{value\":\"100\"}";
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, response.length());
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}