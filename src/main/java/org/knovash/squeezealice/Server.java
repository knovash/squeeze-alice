package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.ResponseFromLms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.knovash.squeezealice.Main.SILENCE;
import static org.knovash.squeezealice.Main.server;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Server {

    public List<Player> players;
    public Integer counter;

    public void countPlayers() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.count().toString());
        if (responseFromLms == null) log.info("ERROR");
        log.info("RESPONSE: " + responseFromLms);
        server.counter = Integer.parseInt(responseFromLms.result._count);
    }

    public static boolean updatePlayers() {
        log.info("UPDATE PLAYERS");
        server.countPlayers();
        Integer counter = server.counter;
        if (counter == null) {
            log.info("UPDATE SKIPED. NO PLAYERS IN LMS");
            return false;}
        List<Player> players = new ArrayList<>();
        if (server.players == null) server.players = new ArrayList<>();
        for (Integer index = 0; index < counter; index++) {
            String name = Player.name(index.toString());
            String id = Player.id(index.toString());
            if (!server.players.contains(new Player(name, id))) {
                log.info("ADD NEW PLAYER: " + name + " " + id);
                server.players.add(new Player(name, id));
            } else {
                log.info("SKIP PLAYER: " + name + " " + id);
            }
        }
        log.info("PLAYERS:");
        log.info(server.players);
        log.info("WRITE 'server.json'");
        JsonUtils.pojoToJsonFile(server, "server.json");
        Utils.generateAltNamesFile();
        return true;
    }

    public void writeServerFile() {
        log.info("WRITE FILE server.json");
        JsonUtils.pojoToJsonFile(server, "server.json");
    }

    public void readServerFile() {
        log.info("TRY READING FILE server.json");
        File file = new File("server.json");
        if (file.exists()) {
            try {
                server = JsonUtils.jsonFileToPojoTrows("server.json", Server.class);
                log.info("FILE READING OK server.json");
                log.info("READ PLAYERS:");
                log.info(server.players);
            } catch (IOException e) {
                log.info("ERROR READING FILE server.json");
                log.info(e);
                log.info("Previous server state lost :(");
            }
        } else {
            log.info("FILE NOT FOUND server.json Previous server state not available");
        }
    }

    public static Player playerByName(String name) {
        return server.players.stream()
                .filter(player -> player.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Player playingPlayer(String currentName) {
        log.info("Search for playing player...");
        Player playing = server.players
                .stream()
                .filter(player -> player.mode().equals("play"))
                .findFirst()
                .orElse(null);
        if (playing == null ||
                playing.path().equals(SILENCE) ||
                playing.name.equals(currentName)) {
            log.info("NO PLAYING");
            return null;
        } else {
            log.info("PLAYING: " + playing.name);
        }
        return playing;
    }
}