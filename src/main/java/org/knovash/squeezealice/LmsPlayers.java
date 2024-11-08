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
    public String tvPlayerInQuery = "homepod1";
    public String tvPlayerName = "HomePod1";
    public int delayUpdate = 5; // MINUTES
    public int delayExpire = 10; // MINUTES
    public boolean syncAlt = false;
    public boolean lastThis = true;
    public static ServerStatusByName serverStatus = new ServerStatusByName();
    public String lastTitle;

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
        serverStatus = JsonUtils.jsonToPojo(json, ServerStatusByName.class);
        log.info("SEARCH AND ADD NEW PLAYERS FROM LMS");
        if (serverStatus == null) return;
        lmsPlayers.players.forEach(p -> {
            p.connected = false;
            p.playing = false;
        });
        serverStatus.result.players_loop.forEach(pl -> updatePlayer(pl));
//        log.info("PLAYERS ONLINE: " + lmsPlayers.playersNamesOnLine + " " + Duration.between(time1, LocalTime.now(zoneId)));
    }

    public void updatePlayer(ServerStatusByName.PlayersLoop playersLoop) {
        playersNamesOnLine.add(playersLoop.name);
        playersNamesOffLine.remove(playersLoop.name);
        Player existsPlayer = lmsPlayers.getPlayerByCorrectName(playersLoop.name);
        if (existsPlayer == null) { // если нет то создать новый плеер и добавить
            log.info("ADD NEW PLAYER: " + playersLoop.name);
            Player newPlayer = new Player(playersLoop.name, playersLoop.playerid);
            if (playersLoop.isplaying == 1) {
                newPlayer.playing = true;
                newPlayer.mode = "play";
                newPlayer.saveLastTime();
            } else {
                newPlayer.playing = false;
                newPlayer.mode = "stop";
            }
            lmsPlayers.players.add(newPlayer);
        } else { // если есть обновить плеер
            existsPlayer.connected = true;
            if (playersLoop.isplaying == 1) {
                existsPlayer.playing = true;
                existsPlayer.mode = "play";
                existsPlayer.saveLastTime();
            } else {
                existsPlayer.playing = false;
                existsPlayer.mode = "stop";
            }
        }
        log.info("UPDATE PLAYER: " + playersLoop.name +
                " CONNECTED: " + playersLoop.connected +
                " INDEX: " + playersLoop.playerindex +
                " PLAYING: " + playersLoop.isplaying);
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
//        log.info("ROOM: " + room);
        Player player = new Player();
        Optional optionalPlayer = lmsPlayers.players.stream()
                .filter(p -> (p.room != null))
//                .peek(p -> log.info("0: " + p.name + " " + p.room + " = " + room + " " + p.room.equals(room)))
                .filter(p -> p.room.equals(room))
                .filter(Objects::nonNull).findFirst();
        player = (Player) optionalPlayer.orElse(null);
        if (player != null) log.info("ROOM: " + room + " PLAYER: " + player.name);
        if (player == null) log.info("PLAYER NOT FOUND WHITH ROOM " + room);
        return player;
    }

    public Player getPlayerByNearestRoom(String room) {
        log.info("GET PLAYER BY NEAREST ROOM: " + room);
        room = Utils.getCorrectRoomName(room);
        Player player = lmsPlayers.getPlayerByCorrectRoom(room);
        return player;
    }

    public Player getPlayerByNameInQuery(String name) {
//        log.info("NAME: " + name);
        if (name == null) return null;
        if (name.equals("btremote")) {
            log.info("BT PLAYER: " + btPlayerInQuery);
            return lmsPlayers.getPlayerByNameInQuery(btPlayerInQuery);
        }
//        if (name.equals("tvremote")) {
//            log.info("TV REMOTE PLAYER: " + tvPlayerInQuery);
//            return lmsPlayers.getPlayerByNameInQuery(tvPlayerInQuery);
//        }
        if (lmsPlayers.players == null) return null;
        Player player = lmsPlayers.players.stream()
                .filter(p -> p.getNameInQuery().toLowerCase().equals(name.toLowerCase()))
                .findFirst()
                .orElse(null);
        log.info("NAME: " + name + " PLAYER: " + player);
        return player;
    }

    public Player getPlayingPlayer(String exceptName) {
        List<Player> playingPlayers = getPlayingPlayers(exceptName);
        if (playingPlayers != null) return playingPlayers.get(0);
        return null;
    }

    public List<Player> getPlayingPlayers(String exceptName) {
        log.info("SEARCH FOR PLAYING. EXCEPT " + exceptName);
        lmsPlayers.updateServerStatus();
        List<Player> playingPlayers = null;
        playingPlayers =
                lmsPlayers.players.stream()
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
                        .collect(Collectors.toList());
        log.info("AFTER FILTER: " + playingPlayers);
        if (playingPlayers == null || playingPlayers.size() == 0) {
            log.info("NO PLAYING PLAYERS");
            return null;
        }
        log.info("PLAYING PLAYERS: " + playingPlayers);
        return playingPlayers;
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String playerName = parameters.get("name");
        String roomName = parameters.get("room");
        String delay = parameters.get("delay");
        String schedule = parameters.get("schedule");
        Player player = lmsPlayers.getPlayerByCorrectName(playerName);
        player.delay = Integer.valueOf(delay);
        player.schedule = Utils.stringSplitToIntMap(schedule, ",", ":");
        log.info("PLAYER: " + player);
        log.info("name: " + parameters.get("name"));
        log.info("room: " + parameters.get("room"));
        log.info("delay: " + parameters.get("delay"));
        log.info("schedule: " + parameters.get("schedule"));
        log.info(Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":"));

        SwitchVoiceCommand.room = roomName;
        Player playerNew = SwitchVoiceCommand.selectPlayerInRoom(playerName, roomName, false);
        log.info("SELECT PLAYER NEW: " + playerNew);

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

    public void delayExpireSave(HashMap<String, String> parameters) {
        delayExpire = Integer.parseInt(parameters.get("delay_expire_value"));
        lmsPlayers.write();
    }

    public void altSyncSave(HashMap<String, String> parameters) {
        syncAlt = Boolean.parseBoolean(parameters.get("alt_sync_value"));
        lmsPlayers.write();
    }

    public void lastThisSave(HashMap<String, String> parameters) {
        log.info(parameters);
        lastThis = Boolean.parseBoolean(parameters.get("last_this_value"));
        lmsPlayers.write();
        log.info(lmsPlayers.lastThis);
    }

    public String roomsAndPlayersAllWidgets() {
        log.info("ROOMS&PLAYERS NAMES START");
        lmsPlayers.updateServerStatus();
        lmsPlayers.syncgroups();
        List<String> roomNames = rooms;
        List<String> playersNames = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> roomsAndPlayersNames = new ArrayList<>();
        roomsAndPlayersNames.addAll(roomNames);
        roomsAndPlayersNames.addAll(playersNames);
        List<String> roomsAndPlayersModes = new ArrayList<>();
        List<String> roomsAndPlayersSyncs = new ArrayList<>();
        List<String> roomsAndPlayersTitles = new ArrayList<>();
        log.info("roomNames: " + roomNames);
        log.info("playersNames: " + playersNames);
        roomNames.stream()
                .map(r -> lmsPlayers.getPlayerByCorrectRoom(r))
                .peek(p -> {
                    if (p != null) {
//                        log.info("/// " + p.name + " " + p.mode + " " + p.sync + " " + p.title);
                        p.status();
                        p.title();
                        roomsAndPlayersModes.add(p.mode);
                        roomsAndPlayersSyncs.add(String.valueOf(p.sync));
                        roomsAndPlayersTitles.add(p.title);
                    } else {
                        roomsAndPlayersModes.add("null");
                        roomsAndPlayersSyncs.add(String.valueOf(false));
                        roomsAndPlayersTitles.add("null");
                    }
                })
                .collect(Collectors.toList());
        playersNames.stream()
                .map(r -> lmsPlayers.getPlayerByCorrectName(r))
                .peek(p -> {
                    if (p != null) {
//                        log.info("/// " + p.name + " " + p.mode + " " + p.sync + " " + p.title);
                        p.status();
                        p.title();
                        roomsAndPlayersModes.add(p.mode);
                        roomsAndPlayersSyncs.add(String.valueOf(p.sync));
                        roomsAndPlayersTitles.add(p.title);
                    } else {
                        roomsAndPlayersModes.add("null");
                        roomsAndPlayersSyncs.add(String.valueOf(false));
                        roomsAndPlayersTitles.add("null");
                    }
                })
                .collect(Collectors.toList());
        log.info("ROOMS&PLAYERS NAMES: " + roomsAndPlayersNames);
        log.info("ROOMS&PLAYERS MODES: " + roomsAndPlayersModes);
        log.info("ROOMS&PLAYERS SYNCS: " + roomsAndPlayersSyncs);
        log.info("ROOMS&PLAYERS TITLES: " + roomsAndPlayersTitles);
        String joinedRoomsAndPlayersNames = String.join(",", roomsAndPlayersNames);
        String joinedRoomsAndPlayersModes = String.join(",", roomsAndPlayersModes);
        String joinedRoomsAndPlayersSyncs = String.join(",", roomsAndPlayersSyncs);
        String joinedRoomsAndPlayersTitles = String.join(",", roomsAndPlayersTitles);
        String response = joinedRoomsAndPlayersNames + ":"
                + joinedRoomsAndPlayersModes + ":"
                + joinedRoomsAndPlayersSyncs + ":"
                + joinedRoomsAndPlayersTitles;
        return response;
    }

    public String playerVolumeModeTitle() {
        log.info("PLAYER-VOLUME-MODE-TITLE START");
        lmsPlayers.updateServerStatus();
        Player.syncgroups();
        List<String> list = lmsPlayers.players.stream()
                .peek(p -> p.status())
                .peek(p -> p.title())
                .map(p -> p.name + "-" + p.volumeGet() + "-" + p.mode + "-" + p.title)
                .collect(Collectors.toList());
        String joinList = String.join(",", list);
        log.info("PLAYER-VOLUME-MODE-TITLE: " + joinList);
        return joinList;
    }

    public String getLastTitle(Player player) {
        String selectLast = null;
        String result = null;
        if (player == null) return "";
        if (lmsPlayers.lastThis) {
            selectLast = player.lastTitle;
            result = "play last this: " + selectLast + " other: " + lmsPlayers.lastTitle;
        } else {
            selectLast = lmsPlayers.lastTitle;
            result = "play last other: " + selectLast + " other: " + lmsPlayers.lastTitle;
        }
        log.info("PLAYER: " + player.name + " LAST TITLE: " + selectLast);
        return result;
    }

    public String getPlayerNameByWidgetName(String value) {
        log.info("GET PLAYER BY WIDGET: " + value);
        Player player1 = null;
        String playerName = "ERROR";
        String roomName;
        roomName = Utils.getCorrectRoomName(value);
//        log.info("ITS ROOM: " + roomName);
        if (roomName != null) player1 = lmsPlayers.getPlayerByCorrectRoom(roomName);
//        log.info("PLAYER BY ROOM: " + player1);
        if (player1 != null) playerName = player1.name;
//        log.info("PLAYER NAME BY ROOM: " + playerName);
        if (player1 == null) {
//            log.info("ITS PLAYER !!!");
            playerName = Utils.getCorrectPlayerName(value);
//            log.info("PLAYER: " + player1);
            if (playerName != null) roomName = lmsPlayers.getPlayerByCorrectName(playerName).room;
//            log.info("ROOM BY PLAYER: " + player1);
        }
        log.info("ROOM: " + roomName + " PLAYER: " + playerName);
        String result = roomName + "," + playerName;
        return result;
    }

    public List<List<String>> syncgroups() {
        Response response = Requests.postToLmsForResponse(RequestParameters.syncgroups().toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        if (response.result.syncgroups_loop == null) {
            log.info("REQUEST syncgroups_loop NULL");
            return null;
        }
        log.info("SYNCGROUPS: " + response.result.syncgroups_loop);
//        List<String> lll = List.of("kj giu oio".split(" "));
        List<List<String>> syncMemberNames = response.result.syncgroups_loop.stream()
                .map(syncgroupsLoop -> syncgroupsLoop.sync_member_names)
                .map(s -> List.of(s.split(",")))
                .collect(Collectors.toList());
        List<Object> result = new ArrayList<>();
        syncMemberNames.forEach(result::addAll);
        this.players.stream().forEach(p -> {
            if (result.contains(p.name)) p.sync = true;
            else p.sync = false;
            log.info("PLAYER: " + p);
        });
        log.info("syncMemberNames: " + result);
        return syncMemberNames;
    }
}