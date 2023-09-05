package org.knovash.squeezealice.lms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Content;
import org.knovash.squeezealice.Fluent;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.Response;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ServerLMS {

    public static List<Player> players;
    public static Integer counter;

    public static void countPlayers() {
        Content content = Fluent.post(Requests.count().toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        counter = Integer.parseInt(response.result._count);
    }

    public static void updatePlayers() {
        log.info("UPDATE PLAYERS");
        ServerLMS.countPlayers();
        Integer counter = ServerLMS.counter;
        List<Player> players = new ArrayList<>();
        for (Integer index = 0; index < counter; index++) {
            String name = Player.name(index.toString());
            String id = Player.id(index.toString());

            players.add(new Player(name, id));
        }
        log.info("\nPLAYERS:\n" + players);
        ServerLMS.players = players;
    }
}
