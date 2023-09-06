package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Content;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.Response;

import java.util.*;

import static org.knovash.squeezealice.Main.serverLMS;


@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerLMS {

//    public ServerLMS() {
//
//    }

    public List<Player> players;
    public Integer counter;

    public void countPlayers() {
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

    public void updatePlayers() {
        log.info("UPDATE PLAYERS");
        this.countPlayers();
        Integer counter = this.counter;
        List<Player> players = new ArrayList<>();
        Map<String,String> namebind = new HashMap<>();
        for (Integer index = 0; index < counter; index++) {
            String name = Player.name(index.toString());
//            String id = Player.id(index.toString());
            String id = "22";

            if (this.players == null) {
                players.add(new Player(name, id));
            } else {
                log.info("CONTAINS?: " + this.players.contains(new Player(name, id)));
                if (this.players.contains(new Player(name, id))) {
                    log.info("CONTAINS YES: " + name);
                } else {
                    log.info("CONTAINS NO. ADD PLAYER: " + name);
                    this.players.add(new Player(name, id));
                }
            }
        }
        log.info("\nPLAYERS FROM SERVER:\n" + this.players);

        log.info("\nBIND:\n" + namebind);

        if (this.players == null) this.players = players;


    }

    public void writeFile() {
        log.info("\nWRITE FILE:\n");
        JsonUtils.pojoToJsonFile(serverLMS, "server.json");

    }

    public void readFile() {
        log.info("\nREAD FILE:\n");
        serverLMS = JsonUtils.jsonFileToPojo("server.json", ServerLMS.class);
        log.info("\nplayers are read into the server:\n" + serverLMS.players);
    }
}
