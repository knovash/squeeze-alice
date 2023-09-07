package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.ResponseFromLms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.knovash.squeezealice.Main.server;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    public List<Player> players;
    public Integer counter;

    public void countPlayers() {
        Response response =  Fluent.post(Requests.count().toString());
//        Response response2 = response;
        Content content;
        HttpResponse httpResponse;
        try {
            content = response.returnContent();
//            httpResponse = response2.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("RESPONSE: " + responseFromLms);
//        log.info("SATUS: " + httpResponse.getStatusLine());
//        return re.resultFromLms._path;
        server.counter = Integer.parseInt(responseFromLms.result._count);
    }

    public static void updatePlayers() {
        log.info("UPDATE PLAYERS");
        server.countPlayers();
        Integer counter = server.counter;
        List<Player> players = new ArrayList<>();
        for (Integer index = 0; index < counter; index++) {
            String name = Player.name(index.toString());
            String id = Player.id(index.toString());
            if (server.players == null) {
                log.info("PLAYERS NULL");
                players.add(new Player(name, id));
            } else {
                if (server.players.contains(new Player(name, id))) {
                    log.info("SKIP PLAYER: " + name + " CONTAINS: " + server.players.contains(new Player(name, id)));
                } else {
                    log.info("ADD NEW PLAYER: " + name + " CONTAINS: " + server.players.contains(new Player(name, id)));
                    server.players.add(new Player(name, id));
                }
            }
        }
        log.info("THIS PLAYERS:\n" + server.players);
        log.info("FOUND PLAYERS:\n" + players);
        if (server.players == null) server.players = players;
        log.info("THIS PLAYERS:\n" + server.players);
    }

    public void writeFile() {
        log.info("WRITE FILE:\n");
        JsonUtils.pojoToJsonFile(server, "server.json");
    }

    public void readFile() {
        log.info("READ FILE:\n");
        File file = new File("server.json");
        if (file.exists()) {
            server = JsonUtils.jsonFileToPojo("server.json", Server.class);
            log.info("THIS PLAYERS:\n" + server.players);
        } else {
            log.info("NO FILE");
        }
    }

    public static Player playerByName(String name) {
        return server.players.stream()
                .filter(player -> player.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Player playingPlayer() {
        log.info("Search for playing player (instance)");
        return server.players
                .stream()
                .filter(player -> player.mode().equals("play"))
                .findFirst().orElse(null);
    }
}
