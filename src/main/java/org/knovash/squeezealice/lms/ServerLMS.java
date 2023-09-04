package org.knovash.squeezealice.lms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Content;
import org.knovash.squeezealice.Fluent;
import org.knovash.squeezealice.ServerController;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.Response;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ServerLMS {

    public static List<Player> players;
    public static Integer counter;

    public static void countPlayers() {
        Response response;
        Content content;
        String json;
        ObjectMapper objectMapper = new ObjectMapper();
        content = Fluent.post(Requests.getCount().toString());
        json = content.asString();
        try {
            response = objectMapper.readValue(json, Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        counter = Integer.parseInt(response.result._count);
        log.info(counter);
    }

    public static void updatePlayers() {
        log.info("UPDATE PLAYERS");
        ServerLMS.countPlayers();
        Integer counter = ServerLMS.counter;
        List<Player> players = new ArrayList<>();

        for (Integer index = 0; index < counter; index++) {
            String name = Player.name(index.toString());
            String id = Player.id(index.toString());
//            log.info("INDEX: " + index + " NAME: " + name + " ID: " + id);
            players.add(new Player(name, id));
        }
        log.info("\nPLAYERS:\n" + players);
        ServerLMS.players = players;
    }

    public static void updateFavorites() {
        log.info("updateFavorites");
    }

    public static Player getPlayerByName(String name) {
        return ServerLMS.players.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }
}
