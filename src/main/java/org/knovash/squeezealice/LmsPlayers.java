package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.ResponseFromLms;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayers {

    public List<Player> players;
    public Integer counter;

    public void countPlayers() {
        ResponseFromLms responseFromLms = Requests.postByJsonForResponse(RequestParameters.count().toString());
        if (responseFromLms == null) {
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIP + ":" + lmsPort);
            lmsPlayers.counter = 0;
            return;
        }
        log.info("RESPONSE: " + responseFromLms);
        lmsPlayers.counter = Integer.parseInt(responseFromLms.result._count);
    }

    public static void updatePlayers() {
        log.info("UPDATE PLAYERS FROM LMS");
        lmsPlayers.countPlayers();
        Integer counter = lmsPlayers.counter;
        if (counter == null) {
            log.info("UPDATE SKIPED. NO PLAYERS IN LMS");
            return;
        }
        List<Player> players = new ArrayList<>();
        if (lmsPlayers.players == null) lmsPlayers.players = new ArrayList<>();
        for (Integer index = 0; index < counter; index++) {
            String name = Player.name(index.toString());
            String id = Player.id(index.toString());
            if (!lmsPlayers.players.contains(new Player(name, id))) {
                log.info("ADD NEW PLAYER: " + name + " " + id);
                lmsPlayers.players.add(new Player(name, id));
            } else {
                log.info("SKIP PLAYER: " + name + " " + id);
            }
        }
        log.info("PLAYERS:");
        log.info(lmsPlayers.players);
        log.info("WRITE lms_players.json");
        JsonUtils.pojoToJsonFile(lmsPlayers, "lms_players.json");
        Utils.generatePlayersAltNamesToFile();
    }

    public void writeServerFile() {
        log.info("WRITE FILE lms_players.json");
        JsonUtils.pojoToJsonFile(lmsPlayers, "lms_players.json");
    }

    public void writeServerFile(String fileName) {
        log.info("WRITE FILE " + fileName);
        JsonUtils.pojoToJsonFile(lmsPlayers, fileName + ".json");
    }

    public void readServerFile() {
        log.info("READ PREVIOUS PLAYERS STATE FROM FILE lms_players.json");
        File file = new File("lms_players.json");
        if (file.exists()) {
            try {
                lmsPlayers = JsonUtils.jsonFileToPojoTrows("lms_players.json", LmsPlayers.class);
                log.info("PLAYERS:");
                log.info(lmsPlayers.players);
            } catch (IOException | InaccessibleObjectException e) {
                log.info("ERROR READ lms_players.json");
                log.info(e);
            }
        } else {
            log.info("FILE NOT FOUND lms_players.json");
        }
    }

    public static Player playerByName(String name) {
        return lmsPlayers.players.stream()
                .filter(player -> player.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public static Player playerByAliceId(String alice_id) {
        return lmsPlayers.players.stream()
                .filter(player -> player.getAlice_id().equals(alice_id))
                .findFirst()
                .orElse(null);
    }

    public static String playerNameByAliceId(String alice_id) {
        if (alice_id == null) return null;
        Player player = lmsPlayers.players.stream()
                .filter(p -> p.getAlice_id().equals(alice_id))
                .findFirst()
                .orElse(null);
        if (player == null) return null;
        return player.name;
    }

    public static Player playingPlayer(String currentName) {
        log.info("Search for playing player...");
        Player playing = lmsPlayers.players
                .stream()
                .filter(player -> player.mode().equals("play"))
                .findFirst()
                .orElse(null);
        log.info("PLAYING: " + playing);
        if (playing == null ||
                playing.path().equals(silence) ||
                playing.name.equals(currentName)) {
            log.info("NO PLAYING");
            return null;
        } else {
            log.info("PLAYING: " + playing.name);
        }
        return playing;
    }

    public static String editPlayerSettings(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        String name = parameters.get("name");
        Player player = LmsPlayers.playerByName(name);
        log.info("PLAYER FOR EDIT: " + player);
        log.info("name: " + parameters.get("name"));
        log.info("alice_id: " + parameters.get("alice_id"));
        log.info("delay: " + parameters.get("delay"));
        log.info("step: " + parameters.get("step"));
        log.info("black: " + parameters.get("black"));
        log.info("schedule: " + parameters.get("schedule"));
        log.info(Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":"));
        player.wake_delay = Integer.valueOf(parameters.get("delay"));
        player.volume_step = Integer.valueOf(parameters.get("step"));
        player.black = Boolean.parseBoolean(parameters.get("black"));
        player.alice_id = parameters.get("alice_id");
        player.timeVolume = Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":");
        JsonUtils.pojoToJsonFile(lmsPlayers, "lms_players.json");
        return "EDITED";
    }
}