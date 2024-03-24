package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayers {

    public Integer counter;
    public List<Player> players;
    public List<String> playersOnlineNames;
    public String lastPath;
    public int lastChannel = 1;
    public String lastPlayTime;
    public String lastAliceId;

    public void count() {
        Response response = Requests.postToLmsForContent(RequestParameters.count().toString());
        if (response == null) {
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIP + ":" + lmsPort);
            lmsPlayers.counter = 0;
            return;
        }
        lmsPlayers.counter = Integer.parseInt(response.result._count);
    }

    public List<String> favorites() {
        String playerName = lmsPlayers.players.get(0).name;
        Response response = Requests.postToLmsForContent(RequestParameters.favorites(playerName, 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return playlist;
    }

    public void update() {
        log.info("UPDATE PLAYERS FROM LMS");
        lmsPlayers.count();
        Integer counter = lmsPlayers.counter; // получить количество плееров в LMS
        if (counter == null || counter == 0) {
            log.info("UPDATE ERROR. NO PLAYERS IN LMS");
            return;
        }
        playersOnlineNames = new ArrayList<>();
        for (int index = 0; index < counter; index++) { // для каждого плеера по id
            String name = Player.name(Integer.toString(index)); // запросить имя
            String id = Player.id(Integer.toString(index)); // запросить id/mac

            if (lmsPlayers.getPlayerByName(name) == null) { // если плеера еще нет в сервере, то добавить
                log.info("FOUND NEW PLAYER: " + name);
                Player newPlayer = new Player(name, id);
                log.info("ADD NEW PLAYER: " + newPlayer);
                lmsPlayers.players.add(newPlayer);
                write();
            }

            Player player = lmsPlayers.getPlayerByName(name);
            player.online = true;
            if (player.mode().equals("play")) player.saveLastTime();
            playersOnlineNames.add(name); // добавить плеер в список активных
        }
    }

    public void clear() {
        log.info("CLEAR PLAYERS");
        lmsPlayers.players = new ArrayList<>();
        write();
    }

    public void write() {
        log.info("WRITE FILE lms_players.json");
        JsonUtils.pojoToJsonFile(lmsPlayers, "lms_players.json");
    }

    public void read() {
        log.info("READ");
        LmsPlayers lp = JsonUtils.jsonFileToPojo("lms_players.json", LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            lmsPlayers = lp;
        }
    }

    public Player getPlayerByName(String name) {
        if (lmsPlayers.players == null) return null;
        return lmsPlayers.players.stream()
                .filter(player -> player.getName().toLowerCase().equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public Player getPlayerByNameInQuery(String name) {
        if (name == null) return null;
        if (lmsPlayers.players == null) return null;
        return lmsPlayers.players.stream()
                .filter(player -> player.getNameInQuery().toLowerCase().equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public Player getPlayingPlayer(String currentName) {
        log.info("SEARCH FOR PLAYING " + lmsPlayers.playersOnlineNames);
        Player playing = lmsPlayers.playersOnlineNames
                .stream()
                .map(n -> getPlayerByName(n))
                .filter(p -> !p.separate)
                .filter(p -> p.online)
                .filter(p -> !p.name.equals(currentName))
                .filter(p -> !p.path().equals(silence))
                .filter(p -> p.mode().equals("play"))
                .findFirst()
                .orElse(null);
        log.info("PLAYING: " + playing);
        return playing;
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        String name = parameters.get("name");
        Player player = lmsPlayers.getPlayerByName(name);
        log.info("PLAYER FOR EDIT: " + player);
        log.info("name: " + parameters.get("name"));
        log.info("delay: " + parameters.get("delay"));
        log.info("step: " + parameters.get("step"));
        log.info("black: " + parameters.get("black"));
        log.info("schedule: " + parameters.get("schedule"));
        log.info(Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":"));
        player.wake_delay = Integer.valueOf(parameters.get("delay"));
        player.volume_step = Integer.valueOf(parameters.get("step"));
        player.black = Boolean.parseBoolean(parameters.get("black"));
        player.timeVolume = Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":");
        write();
        return "OK";
    }

    public String playerRemove(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        String name = parameters.get("name");
        Player player = lmsPlayers.getPlayerByName(name);
        log.info("PLAYER FOR EDIT: " + player);
        log.info("name: " + parameters.get("name"));
        lmsPlayers.players.remove(player);
        write();
        return "OK";
    }
}