package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatusByName;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.web.PageIndex;

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
    public String autoRemoteRefresh = "https://autoremotejoaomgcd.appspot.com/sendmessage?key=fovfKw-pC3A:APA91bFz1IHu4FIo9BpJaxwW0HgOulJtoXHF-khXptkSmn6QjhBIywkgi0-w9f4DvMK5y-hoOOTWsXDrv7ASE4S4BADhV8SQz6Y0XOJ5XWbF0pmprdOdmA7aEZ5hfQAWZ2Cd9RW_rShf&message=re";
public static String saveToFileJson = "data/lms_players.json";

    public List<String> favorites() {
        log.info("START");
        String playerName = lmsPlayers.players.get(0).name;
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites(playerName, 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return playlist;
    }

    public void updateLmsPlayers() {
        log.info("UPDATE PLAYERS FROM LMS");
        if (!lmsServerOnline) {
            log.info("LMS OFFLINE");
            return;
        }
        String countPlayers = Player.count();
        if (countPlayers.equals("0")) {
            log.info("LMS NO PLAYERS");
            lmsPlayers.players = new ArrayList<>();
            return;
        }

        playersNames = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        String json = Requests.postToLmsForJsonBody(RequestParameters.serverstatusname().toString());
        log.debug("JSON: " + json);
        if (json == null) return;
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
        serverStatus = JsonUtils.jsonToPojo(json, ServerStatusByName.class);
//        log.info("SEARCH AND ADD NEW PLAYERS FROM LMS");
        if (serverStatus == null) return;
        lmsPlayers.players.forEach(p -> {
            p.connected = false;
            p.playing = false;
            p.mode = "stop";
        });
        serverStatus.result.players_loop.forEach(pl -> updatePlayer(pl));

        if (lmsPlayers.players != null && lmsPlayers.players.size() > 0)
            PageIndex.msgLmsPlayers = "LMS подключено " + lmsPlayers.players.size() + " плееров "
                    + lmsPlayers.players.stream().map(player -> player.name)
                    .collect(Collectors.toList());
        else
            PageIndex.msgLmsPlayers = "LMS нет плееров. Подключите плееры http://192.168.1.110:9000";

//        log.info("PLAYERS ONLINE: " + lmsPlayers.playersNamesOnLine + " " + Duration.between(time1, LocalTime.now(zoneId)));
    }

    public void updatePlayer(ServerStatusByName.PlayersLoop playersLoop) {
        playersNamesOnLine.add(playersLoop.name);
//        playersNamesOffLine.remove(playersLoop.name);
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
//        JsonUtils.pojoToJsonFile(lmsPlayers, "data/lms_players.json");
        JsonUtils.pojoToJsonFile(lmsPlayers, LmsPlayers.saveToFileJson);
    }

    public void readPlayersSettings() {
        log.debug("READ LMS PLAYERS FROM lms_players.json");
        lmsPlayers.players = new ArrayList<>();
//        LmsPlayers lp = JsonUtils.jsonFileToPojo("data/lms_players.json", LmsPlayers.class);
        LmsPlayers lp = JsonUtils.jsonFileToPojo(LmsPlayers.saveToFileJson, LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            lmsPlayers = lp;
//            log.info("LAST PATH: " + lmsPlayers.lastPath);
//            log.info("LAST CHANNEL: " + lmsPlayers.lastChannel);
//            log.info("BT REMOTE: " + lmsPlayers.btPlayerInQuery);
        }
        log.info("PLAYERS FROM lms_players.json: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public Player getPlayerByCorrectName(String name) {
        log.debug("BY NAME: " + name);
        Player player = new Player();
        if (name == null) return null;
        if (lmsPlayers.players == null) return null;
        player = lmsPlayers.players.stream()
                .filter(p -> (p.getName() != null))
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (player == null) log.info("PLAYER NOT FOUND BY NAME: " + name);
        log.info("BY NAME: " + name +" GET PLAYER: " + player);
        return player;
    }

    public Player getPlayerByNearestName(String player) {
        log.info("START: " + player);
        if (player == null) return null;
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
//        String correctPlayerName = Levenstein.getNearestElementInList(player, players);
        String correctPlayerName = Levenstein.getNearestElementInListWord(player, players);
//        String correctPlayerName = Levenstein.search(player, players);
        if (correctPlayerName == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayerName);
        Player correctPlayer = lmsPlayers.getPlayerByCorrectName(correctPlayerName);
        return correctPlayer;
    }

    public Player getPlayerByCorrectRoom(String room) {
//        log.info("ROOM: " + room);
        Player player = new Player();
        Optional<Player> optionalPlayer = lmsPlayers.players.stream()
                .filter(p -> (p.room != null))
//                .peek(p -> log.info("0: " + p.name + " " + p.room + " = " + room + " " + p.room.equals(room)))
                .filter(p -> p.room.equals(room))
                .filter(Objects::nonNull)
                .findFirst();
        player = (Player) optionalPlayer.orElse(null);
        if (player != null) log.info("ROOM: " + room + " PLAYER: " + player.name);
        if (player == null) log.debug("PLAYER NOT FOUND WHITH ROOM " + room);
        return player;
    }

    public Player getPlayerByCorrectRoom2(String room) {
        log.debug("ROOM: " + room);
        Optional<Player> optionalPlayer = lmsPlayers.players.stream()
                .filter(p -> (p.room != null))
                .filter(p -> p.room.equals(room))
                .findFirst();
        Player player = optionalPlayer.orElse(null);
        if (player != null) {
            log.info("ROOM: " + room + " PLAYER: " + player.name);
        } else {
            log.debug("PLAYER NOT FOUND WITH ROOM " + room);
        }
        return player;
    }

    public Player getPlayerByNearestRoom(String room) {
        log.info("GET PLAYER BY NEAREST ROOM: " + room);
        if (room == null) return null;
        room = Utils.getCorrectRoomName(room);
        Player player = lmsPlayers.getPlayerByCorrectRoom(room);
        return player;
    }

    public Player getPlayingPlayer(String exceptName) {
        List<Player> playingPlayers = getPlayingPlayers(exceptName);
        if (playingPlayers != null) return playingPlayers.get(0);
        return null;
    }

    public List<Player> getPlayingPlayers(String exceptName) {
        log.info("SEARCH FOR PLAYING. EXCEPT " + exceptName);
        lmsPlayers.updateLmsPlayers();
        List<Player> playingPlayers = null;
        playingPlayers =
                lmsPlayers.players.stream()
//                .peek(p -> log.info("PLAYER: " + p.name + " SEPARATE: " + p.separate + " ONLINE: " + p.connected + " PLAYING: " + p.playing))
                        .filter(p -> !p.separate)
                        .filter(p -> p.playing)
                        .filter(p -> !p.name.equals(exceptName))
                        .filter(p -> p.path() != null)
                        .filter(p -> !p.path().equals(config.silence))
//                        .filter(p -> {
//                            String pp = p.path();
//                            if (pp == null) return false;
//                            if (pp.equals(silence)) return false;
//                            return true;
//                        })
                        .collect(Collectors.toList());
//        log.info("AFTER FILTER: " + playingPlayers);
        if (playingPlayers == null || playingPlayers.size() == 0) {
            log.info("NO PLAYING PLAYERS");
            return null;
        }
        log.info("PLAYING PLAYERS: " + playingPlayers);
        return playingPlayers;
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String playerName = parameters.get("player_name");
        String roomName = parameters.get("room");
        String delay = parameters.get("delay");
        String schedule = parameters.get("schedule");

        log.info("name: " + playerName);
        log.info("room: " + roomName);
        log.info("delay: " + delay);
        log.info("schedule: " + schedule);


        Player player = lmsPlayers.getPlayerByCorrectName(playerName);
        log.info("PLAYER: " + player);

        player.delay = Integer.valueOf(delay);
        player.schedule = Utils.stringSplitToIntMap2(schedule, ",", ":");
        log.info("PLAYER SHEDULE: " + schedule);

//        log.info(Utils.stringSplitToIntMap(parameters.get("schedule"), ",", ":"));
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
        log.info("PLAYER DEVICE ID: " + player.deviceId);
        int id = Integer.parseInt(player.deviceId);
        log.info("PLAYER REMOVE: " + player);
        lmsPlayers.players.remove(player);


        Device device = SmartHome.getDeviceById(id);
        SmartHome.devices.remove(device);

        write();
        return "OK";
    }

    public List<String> getSeparatePlayers(Player excludePlayer) {
        log.info("TRY GET SEPARATED PLAYERS");
        lmsPlayers.updateLmsPlayers();
        List<String> separatePlayers =
                lmsPlayers.players.stream()
                        .peek(p -> log.debug(p.name + " separate " + p.separate))
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
        if (player != null) log.info("ID: " + id + " PLAYER: " + player.name);
        return player;
    }

    public String getPlayerNameByDeviceId(String id) {
        Player player = getPlayerByDeviceId(id);
        if (player == null) return "";
        return player.name;
    }

    public void delayExpireSave(HashMap<String, String> parameters) {
        delayExpire = Integer.parseInt(parameters.get("delay_expire_value"));
//        JsonUtils.pojoToJsonFile(lmsPlayers, "data/lms_players.json");
        JsonUtils.pojoToJsonFile(lmsPlayers,LmsPlayers.saveToFileJson);
        lmsPlayers.write(); // ПОЧЕМУ ВЫЗЫВАЕТ СУПЕР РЕФРЕШ???
    }

    public void autoremoteSave(HashMap<String, String> parameters) {
        autoRemoteRefresh = parameters.get("autoremote_value");
        lmsPlayers.write();
    }

    public void altSyncSave(HashMap<String, String> parameters) {
        syncAlt = Boolean.parseBoolean(parameters.get("alt_sync_value"));
        lmsPlayers.write();
    }

    public void lmsSave(HashMap<String, String> parameters) {
        config.lmsIp = parameters.get("lms_ip_value");
        config.lmsPort = parameters.get("lms_port_value");
        config.writeConfig();

        lmsPlayers.searchForLmsIp();
        lmsPlayers.updateLmsPlayers();

//        Utils.checkIpIsLms(config.lmsIp);
//        lmsPlayers.write();
    }


    public void lastThisSave(HashMap<String, String> parameters) {
        log.info(parameters);
        lastThis = Boolean.parseBoolean(parameters.get("last_this_value"));
        lmsPlayers.write();
        log.info(lmsPlayers.lastThis);
    }

    public String getSuperRefresh() {
        log.info("SUPER REFRESH START >>>");
        lmsPlayers.updateLmsPlayers(); //TODO совместить со статусами плееров
        lmsPlayers.syncgroups();
        List<String> roomNames = rooms;
        List<String> playersNames = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> roomsAndPlayersNames = new ArrayList<>();
        roomsAndPlayersNames.addAll(roomNames);
        roomsAndPlayersNames.addAll(playersNames);
        List<String> roomsAndPlayersModes = new ArrayList<>();
        List<String> roomsAndPlayersSyncs = new ArrayList<>();
        List<String> roomsAndPlayersTitles = new ArrayList<>();
        List<String> playersVolModTit = new ArrayList<>();
        log.info("roomNames: " + roomNames);
        log.info("playersNames: " + playersNames);
        roomNames.stream()
                .map(r -> lmsPlayers.getPlayerByCorrectRoom(r))
                .peek(p -> {
                    if (p != null) {
                        p.status();
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
                        roomsAndPlayersModes.add(p.mode);
                        roomsAndPlayersSyncs.add(String.valueOf(p.sync));
                        roomsAndPlayersTitles.add(p.title);
                        playersVolModTit.add(p.name + "-" + p.volume + "-" + p.mode + "-" + p.title);
                    } else {
                        roomsAndPlayersModes.add("null");
                        roomsAndPlayersSyncs.add(String.valueOf(false));
                        roomsAndPlayersTitles.add("null");
                    }
                })
                .collect(Collectors.toList());

        log.debug("ROOMS&PLAYERS NAMES: " + roomsAndPlayersNames);
        log.debug("ROOMS&PLAYERS MODES: " + roomsAndPlayersModes);
        log.debug("ROOMS&PLAYERS SYNCS: " + roomsAndPlayersSyncs);
        log.debug("ROOMS&PLAYERS TITLES: " + roomsAndPlayersTitles);
        String joinedRoomsAndPlayersNames = String.join(",", roomsAndPlayersNames);
        String joinedRoomsAndPlayersModes = String.join(",", roomsAndPlayersModes);
        String joinedRoomsAndPlayersSyncs = String.join(",", roomsAndPlayersSyncs);
        String joinedRoomsAndPlayersTitles = String.join(",", roomsAndPlayersTitles);
        String joinedPlayersVolModTit = String.join(",", playersVolModTit);

        String response = joinedRoomsAndPlayersNames + ":"
                + joinedRoomsAndPlayersModes + ":"
                + joinedRoomsAndPlayersSyncs + ":"
                + joinedRoomsAndPlayersTitles + ":"
                + joinedPlayersVolModTit;
        log.info("SUPER REFRESH FINISH <<<");
        return response;
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
        this.players.forEach(p -> p.sync = false);
        if (response == null) return null;
        if (response.result.syncgroups_loop == null) return null;
        log.info("SYNCGROUPS: " + response.result.syncgroups_loop);
        List<List<String>> syncMemberNames = response.result.syncgroups_loop.stream()
                .map(syncgroupsLoop -> syncgroupsLoop.sync_member_names)
                .map(s -> List.of(s.split(",")))
                .collect(Collectors.toList());
        List<Object> result = new ArrayList<>();
        syncMemberNames.forEach(result::addAll);
        this.players.stream()
                .filter(p -> result.contains(p.name))
                .forEach(p -> p.sync = true);
        log.info("syncMemberNames: " + result);
        return syncMemberNames;
    }

    public void searchForLmsIp() {
        log.info("SEARCH FOR LMS IP");

        String lmsIp = LmsSearchForIp.findServerIp();

        if (lmsIp != null) {
            log.info("LMS HAS BEEN FOUND: " + lmsIp);
            lmsServerOnline = true;
            config.lmsIp = lmsIp;
            config.writeConfig();
        } else {
            log.info("LMS WAS NOT FOUND");
            lmsServerOnline = false;
        }

    }
}


//        else {
//            Integer start = 1;
//            while (lmsIp == null && start < 150) {
//                lmsIp = IntStream
//                        .range(start, start + 50)
//                        .boxed()
//                        .map(index -> CompletableFuture.supplyAsync(() -> Utils.checkIp(myip, Integer.valueOf(index))))
//                        .collect(Collectors.collectingAndThen(Collectors.toList(), cfs -> cfs.stream().map(CompletableFuture::join)))
//                        .filter(Objects::nonNull)
//                        .findFirst().orElse(null);
//                start = start + 50;
//            }
//        }