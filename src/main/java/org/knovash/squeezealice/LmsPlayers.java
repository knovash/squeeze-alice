package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatusByName;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayers {

    public List<Player> players;
    public List<String> playersNames = new ArrayList<>();
    public List<String> playersNamesOnLine = new ArrayList<>();
    public List<String> playersNamesOffLine = new ArrayList<>();
    public String lastPath;
    public int lastChannel = 1;
    public String btPlayerInQuery = "homepod";
    public String btPlayerName = "HomePod";
    public int delayUpdate = 5; // MINUTES
    public int delayExpire = 10; // MINUTES
    public static ServerStatusByName serverStatusByName = new ServerStatusByName();

    public List<String> favorites() {
        log.info("START");
        String playerName = lmsPlayers.players.get(0).name;
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites(playerName, 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return playlist;
    }

    public void updateServerStatus() {
//        log.info("UPDATE SERVER STATUS FROM LMS");
        LocalTime time1 = LocalTime.now(zoneId);
        playersNames = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
//        log.info("PLAYERS: " + playersNames);
        playersNamesOffLine = new ArrayList<>();
        playersNamesOffLine.addAll(playersNames);
        playersNamesOnLine = new ArrayList<>();
        String json = Requests.postToLmsForJsonBody(RequestParameters.serverstatusname().toString());
        if (json == null) return;
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
        serverStatusByName = JsonUtils.jsonToPojo(json, ServerStatusByName.class);
        log.info("SEARCH AND ADD NEW PLAYERS FROM LMS");
        if (serverStatusByName == null) return;
        serverStatusByName.result.players_loop.forEach(p -> updatePlayer(p));
//        log.info("PLAYERS ONLINE: " + lmsPlayers.playersNamesOnLine + " " + Duration.between(time1, LocalTime.now(zoneId)));
    }

    public void updatePlayer(ServerStatusByName.PlayersLoop p) {
        playersNamesOnLine.add(p.name);
        playersNamesOffLine.remove(p.name);
        Player playerByName = lmsPlayers.getPlayerByCorrectName(p.name);
        if (playerByName == null) {
            log.info("ADD NEW PLAYER: " + p.name);
            Player player = new Player(p.name, p.playerid);
            if (p.isplaying == 1) {
                player.playing = true;
                player.saveLastTime();
            } else {
                player.playing = false;
            }
            lmsPlayers.players.add(player);
        } else {
            Player player = playerByName;
            player.connected = true;
            if (p.isplaying == 1) {
                player.playing = true;
                player.saveLastTime();
            } else {
                player.playing = false;
            }
        }
//        log.info("UPDATE PLAYER: " + p.name +
//                " CONNECTED: " + p.connected +
//                " INDEX: " + p.playerindex +
//                " PLAYING: " + p.isplaying);
    }

    public void write() {
        log.info("WRITE FILE lms_players.json");
        JsonUtils.pojoToJsonFile(lmsPlayers, "lms_players.json");
    }

    public void read() {
        log.info("READ LMS PLAYERS FROM lms_players.json");
        lmsPlayers.players = new ArrayList<>();
        LmsPlayers lp = JsonUtils.jsonFileToPojo("lms_players.json", LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            lmsPlayers = lp;
//            log.info("LAST PATH: " + lmsPlayers.lastPath);
//            log.info("LAST CHANNEL: " + lmsPlayers.lastChannel);
//            log.info("BT REMOTE: " + lmsPlayers.btPlayerInQuery);
        }
        log.info("PLAYERS: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public Player getPlayerByCorrectName(String name) {
//        log.info("BY NAME: " + name);
        Player player = new Player();
        if (name == null) return null;
        if (lmsPlayers.players == null) return null;
        player = lmsPlayers.players.stream()
                .filter(p -> (p.getName() != null))
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (player == null) log.info("PLAYER BY NAME NOT FOUND " + name);
        log.info("PLAYER BY NAME: " + player);
        return player;
    }

    public Player getPlayerByCorrectRoom(String room) {
        log.info("ROOM: " + room);
        Player player = new Player();
        Optional optionalPlayer = lmsPlayers.players.stream()
                .filter(p -> (p.room != null))
                .peek(p -> log.info("0: " + p.name + " " + p.room + " = " + room + " " + p.room.equals(room)))
                .filter(p -> p.room.equals(room))
                .filter(Objects::nonNull).findFirst();
        player = (Player) optionalPlayer.orElse(null);
        if (player != null) log.info("ROOM: " + room + " PLAYER: " + player.name);
        if (player == null) log.info("PLAYER NOT FOUND WHITH ROOM " + room);
        return player;
    }

    public Player getPlayerByNameInQuery(String name) {
//        log.info("NAME: " + name);
        if (name == null) return null;
        if (name.equals("btremote")) {
            log.info("BT PLAYER: " + btPlayerInQuery);
            return lmsPlayers.getPlayerByNameInQuery(btPlayerInQuery);
        }
        if (lmsPlayers.players == null) return null;
        return lmsPlayers.players.stream()
                .filter(player -> player.getNameInQuery().toLowerCase().equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    public Player getPlayingPlayer(String exceptName) {
        LocalTime time1 = LocalTime.now(zoneId);
        log.info("SEARCH FOR PLAYING. EXCEPT " + exceptName);
        lmsPlayers.updateServerStatus();
        Player playing = lmsPlayers.players.stream()
//                .peek(p -> log.info("PLAYER: " + p.name + " SEPARATE: " + p.separate + " ONLINE: " + p.connected + " PLAYING: " + p.playing))
                .filter(p -> !p.separate)
//                .filter(p -> p.connected)
                .filter(p -> p.playing)
                .filter(p -> !p.name.equals(exceptName))
                .filter(p -> {
                    String pp = p.path();
                    if (pp == null) return false;
                    if (pp.equals(silence)) return false;
                    return true;
                })
                .findFirst()
                .orElse(null);
        log.info("PLAYING: " + playing);
        return playing;
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String name = parameters.get("name");
        String room = parameters.get("room");
        String delay = parameters.get("delay");
        String schedule = parameters.get("schedule");
        Player player = lmsPlayers.getPlayerByCorrectName(name);
        player.delay = Integer.valueOf(delay);
        player.schedule = Utils.stringSplitToIntMap(schedule, ",", ":");
        log.info("PLAYER: " + player);
        log.info("name: " + parameters.get("name"));
        log.info("room: " + parameters.get("room"));
        log.info("delay: " + parameters.get("delay"));
        log.info("schedule: " + parameters.get("schedule"));
        log.info(Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":"));

        SwitchVoiceCommand.room = room;
        SwitchVoiceCommand.selectPlayerByCorrectPlayer(name);

        write();
        return "OK";
    }

    public String playerRemove(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        String name = parameters.get("name");
        Player player = lmsPlayers.getPlayerByCorrectName(name);
        log.info("PLAYER REMOVE: " + player);
        lmsPlayers.players.remove(player);
        write();
        return "OK";
    }

    public List<String> getSeparatePlayers(Player excludePlayer) {
        log.info("TRY GET SEPARATED PLAYERS");
        lmsPlayers.updateServerStatus();
        List<String> separatePlayers =
                lmsPlayers.players.stream()
                        .peek(p -> log.info(p.name + " separate " + p.separate))
                        .filter(p -> p.separate)
                        .filter(p -> !p.name.equals(excludePlayer.name))
//                        .peek(p -> log.info(p.name + " filter separate " + p.separate))
                        .map(p -> {
                            if (p.playing) return " играет " + p.name;
                            return " не играет " + p.name;
                        })
                        .collect(Collectors.toList());
        log.info("SEPARATED PLAYERS: " + separatePlayers);
        return separatePlayers;
    }

    public Player getPlayerByDeviceId(String id) {
        if (id == null) {
            log.info("ERROR NO PLAYER BY ID: " + id);
            return null;
        }
        Player player = lmsPlayers.players.stream()
                .filter(p -> p.room != null)
                .filter(p -> p.deviceId != null)
                .filter(p -> p.deviceId.equals(id))
                .findFirst().orElse(null);
        log.info("ID: " + id + " PLAYER: " + player);
        return player;
    }

    public String getPlayerNameByDeviceId(String id) {
        Player player = getPlayerByDeviceId(id);
        if (player == null) return "";
        return player.name;
    }
}