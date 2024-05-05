package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatusName;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    public List<String> playersNames = new ArrayList<>();
    public List<String> playersNamesOnLine = new ArrayList<>();
    public List<String> playersNamesOffLine = new ArrayList<>();
    public String lastPath;
    public int lastChannel = 1;
    public String lastAliceId;
    public String btPlayerInQuery = "homepod";
    public String btPlayerName = "HomePod";
    public int delayUpdate = 5; // MINUTES
    public int delayExpire = 10; // MINUTES

    public void count() {
        Response response = Requests.postToLmsForResponse(RequestParameters.count().toString());
        if (response == null) {
            log.info("ERROR NO RESPONSE FROM LMS check that the server is running on http://" + lmsIp + ":" + lmsPort);
            lmsPlayers.counter = 0;
            return;
        }
        lmsPlayers.counter = Integer.parseInt(response.result._count);
    }

    public List<String> favorites() {
        String playerName = lmsPlayers.players.get(0).name;
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites(playerName, 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return playlist;
    }

    public void update() {
        log.info("");
        log.info("UPDATE PLAYERS START >>>>>>>>>>");
        LocalTime time1 = LocalTime.now();
        playersNames = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        playersNamesOffLine = new ArrayList<>();
        playersNamesOffLine.addAll(playersNames);
        playersNamesOnLine = new ArrayList<>();
        String json = Requests.postToLmsForJsonBody(RequestParameters.serverstatusname().toString());
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
        ServerStatusName serverStatusName = JsonUtils.jsonToPojo(json, ServerStatusName.class);
        serverStatusName.result.players_loop.forEach(p -> {
            log.info("NAME: " + p.name +
                    " ID: " + p.playerid +
                    " CONNECTED: " + p.connected +
                    " INDEX: " + p.playerindex +
                    " PLAYING: " + p.isplaying);
            playersNamesOnLine.add(p.name);
            playersNamesOffLine.remove(p.name);
            if (lmsPlayers.getPlayerByName(p.name) == null) { // если плеера еще нет в сервере, то добавить
                log.info("NEW PLAYER: " + p.name);
                Player player = new Player(p.name, p.playerid);
                if (p.isplaying == 1) {
                    player.playing = true;
                    player.saveLastTime();
                } else {
                    player.playing = false;
                }
                lmsPlayers.players.add(player);
            } else {
                Player player = lmsPlayers.getPlayerByName(p.name);
                player.connected = true;
                if (p.isplaying == 1) {
                    player.playing = true;
                    player.saveLastTime();
                } else {
                    player.playing = false;
                }

                if (serverStatusName.result != null) {
//                    player.title() = serverStatusName.
                }

            }
        });
        write();
        log.info("PLAYERS: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
        log.info("PLAYERS NAMES: " + lmsPlayers.playersNames);
        log.info("PLAYERS NAMES ONLINE: " + lmsPlayers.playersNamesOnLine);
        log.info("PLAYERS NAMES OFFLINE: " + lmsPlayers.playersNamesOffLine);
        log.info("UPDATE PLAYERS FINISH " + Duration.between(time1, LocalTime.now()) + " <<<<<<<<<<");
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
        log.info("");
        log.info("READ LMS PLAYERS FROM lms_players.json");
        lmsPlayers.players = new ArrayList<>();
        LmsPlayers lp = JsonUtils.jsonFileToPojo("lms_players.json", LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            lmsPlayers = lp;
            log.info("LAST PATH: " + lmsPlayers.lastPath);
            log.info("LAST CHANNEL: " + lmsPlayers.lastChannel);
            log.info("BT REMOTE: " + lmsPlayers.btPlayerInQuery);
        }
        log.info("PLAYERS: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public Player getPlayerByName(String name) {
        log.info("BY NAME: " + name);
        Player player = new Player();
        if (name == null) return null;
        if (lmsPlayers.players == null) return null;
        player = lmsPlayers.players.stream()
                .filter(p -> (p.getName() != null))
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (player == null) log.info("PLAYER NOT FOUND " + name);
//        log.info("PLAYER: " + player.name);
        return player;
    }

    public Player getPlayerByRoom(String room) {
        log.info("ROOM: " + room);
        Player player = new Player();
        player = lmsPlayers.players.stream()
                .filter(p -> (p.roomPlayer != null))
                .filter(p -> p.roomPlayer.equals(room))
                .findFirst()
                .orElse(null);
        log.info("PLAYER: " + player.name);
        if (player == null) log.info("PLAYER NOT FOUND WHITH ROOM " + room);
        return player;
    }

    public Player getPlayerByNameInQuery(String name) {
        log.info("NAME: " + name);
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

    public Player getPlayingPldayer(String exceptName) {
        LocalTime time1 = LocalTime.now();
        log.info("SEARCH FOR PLAYING online" + lmsPlayers.playersNamesOnLine);
        log.info("EXCEPT CURRENT PLAYER " + exceptName);
        Player playing = lmsPlayers.players.stream()
//        Player playing = lmsPlayers.playersOnlineNames.stream().map(n -> getPlayerByName(n))
                .filter(p -> !p.separate)
                .filter(p -> p.connected)
                .filter(p -> !p.name.equals(exceptName))
                .filter(p -> {
                    String pp = p.path();
                    if (pp == null) return false;
                    if (pp.equals(silence)) return false;
                    return true;
                })
                .filter(p -> p.mode().equals("play"))
                .findFirst()
                .orElse(null);
        log.info("PLAYING: " + playing);
        LocalTime time2 = LocalTime.now();
        log.info("------------- TIME PLAYING OLD: " + Duration.between(time1, time2));
        return playing;

    }

    public Player getPlayingPlayer(String exceptName) {
        LocalTime time1 = LocalTime.now();
        log.info("");
        log.info("SEARCH FOR PLAYING. EXCEPT " + exceptName + " START >>>>>>>>>>");
        lmsPlayers.update();
        Player playing = lmsPlayers.players.stream()
                .peek(p -> log.info("PLAYER: " + p.name + " SEPARATE: " + p.separate + " ONLINE: " + p.connected + " PLAYING: " + p.playing))
                .filter(p -> !p.separate)
                .filter(p -> p.connected)
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
        log.info("PLAYING: " + playing + " FINISH <<<<<<<<<<");
        return playing;
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String name = parameters.get("name");
        String room = parameters.get("room");
        String delay = parameters.get("delay");
        String schedule = parameters.get("schedule");

        Player player = lmsPlayers.getPlayerByName(name);

        log.info("PLAYER: " + player);
        log.info("name: " + parameters.get("name"));
        log.info("room: " + parameters.get("room"));
        log.info("delay: " + parameters.get("delay"));
        log.info("schedule: " + parameters.get("schedule"));
        log.info(Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":"));


        player.roomPlayer = room;
        player.delay = Integer.valueOf(delay);
        player.schedule = Utils.stringSplitToIntMap(schedule, ",", ":");

        List<String> rooms = SmartHome.devices.stream().map(device -> device.room).collect(Collectors.toList());
        log.info("ROOMS: " + rooms);
//        комната существует, плеер = плеер в комнате - изменить параметры плеера
//        комната существует, плеер != плеер в комнате - поменять id плееров. изменить параметры плеера
//        комната не существует - создать device. изменить параметры плеера


//      комната не существует - создать device и room
        if (!rooms.contains(room)) {
            log.info("ROOM NOT EXISTS. CREATE DEVICE IN ROOM");
            HashMap<String, String> parameters2 = new HashMap<>();
            parameters2.put("room", room);
            Device device = SmartHome.create(parameters2);
            String id = device.id;
            player.deviceId = id;
        }
//      комната существует
        else {
            String id = SmartHome.getDeviceByRoom(room).id;
            player.deviceId = id;
            log.info("ROOM EXISTS");
            Player playerInRoom = lmsPlayers.getPlayerByRoom(room);
            log.info("PLAYER IN ROOM: " + playerInRoom.name + " " + playerInRoom.roomPlayer + " " + playerInRoom.deviceId);
            if (!player.name.equals(playerInRoom.name)) {
                log.info("PLAYER != PLAYER IN ROOM - SWAP PLAYERS ID");
                player.deviceId = playerInRoom.deviceId;
                player.roomPlayer = room;

                playerInRoom.deviceId = null;
                playerInRoom.roomPlayer = null;
                log.info("PLAYER IN ROOM: " + playerInRoom.name + " " + playerInRoom.roomPlayer + " " + playerInRoom.deviceId);
            }
        }
        log.info("PLAYER: " + player.name + " " + player.roomPlayer + " " + player.deviceId);

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

    public void resetPlayers() {
        lmsPlayers.players = new ArrayList<>();
        SmartHome.devices = new LinkedList<>();
        lmsPlayers.update();
        lmsPlayers.write();
    }
}