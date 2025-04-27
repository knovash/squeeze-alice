package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatusByName;
import org.knovash.squeezealice.provider.ProviderAction;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.PagePlayers.*;

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
    public String autoRemoteRefresh = null;
    public static String saveToFileJson = "data/lms_players.json";

    public List<String> playingPlayersNames;
    public List<String> playingPlayersNamesNotInCurrentGrop;
    public List<String> playersNamesInCurrentGroup;

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
//        log.debug("JSON: " + json);
        if (json == null) return;
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
        serverStatus = JsonUtils.jsonToPojo(json, ServerStatusByName.class);
//        log.info("SEARCH AND ADD NEW PLAYERS FROM LMS");
        if (serverStatus == null) return;
// перед апдейтом сбросить состояние для всех плееров
        lmsPlayers.players.forEach(p -> {
            p.connected = false;
            p.playing = false;
            p.volume = "--";
            p.title = "unknown";
            p.mode = "offline";
        });
// обновить состояние всех плееров полученных в players_loop из LMS
        serverStatus.result.players_loop.forEach(pl -> updatePlayer(pl));
    }

    public void updatePlayer(ServerStatusByName.PlayersLoop playersLoop) {
//        log.info("");
//        log.info("START UPDATE: " + playersLoop.name);
        playersNamesOnLine.add(playersLoop.name);
        Player existsPlayer = lmsPlayers.playerByCorrectName(playersLoop.name);
        if (existsPlayer == null) {
            // если нет то создать новый плеер и добавить
            Player newPlayer = new Player(playersLoop.name, playersLoop.playerid);
            if (playersLoop.isplaying == 1) {
                newPlayer.playing = true;
                newPlayer.mode = "play";
//                newPlayer.saveLastTime();
            } else {
                newPlayer.playing = false;
                newPlayer.mode = "stop";
            }
            if (playersLoop.connected == 1) newPlayer.connected = true;
            newPlayer.status();

            lmsPlayers.players.add(newPlayer);
            log.info("ADD NEW: " +
                            " CONNECTED: " + newPlayer.connected +
                            " SEPARATE: " + newPlayer.separate +
                            " SYNC: " + newPlayer.sync +
                            " PLAYING: " + newPlayer.playing +
                            " PLAYER: " + newPlayer.name
//                    " INDEX: " + playersLoop.playerindex
            );
        } else { // если есть обновить плеер
            if (playersLoop.connected == 1) existsPlayer.connected = true;
            if (playersLoop.isplaying == 1) {
                existsPlayer.playing = true;
                existsPlayer.mode = "play";
            } else {
                existsPlayer.playing = false;
                existsPlayer.mode = "stop";
            }
            existsPlayer.status();
            log.info("UPDATE: " +
                            " CONNECTED: " + existsPlayer.connected +
                            " SEPARATE: " + existsPlayer.separate +
                            " SYNC: " + existsPlayer.sync +
                            " PLAYING: " + existsPlayer.playing +
                            " PLAYER: " + existsPlayer.name
//                    " INDEX: " + playersLoop.playerindex
            );
        }

    }

    public void write() {
        log.info("WRITE FILE: " + LmsPlayers.saveToFileJson);
        JsonUtils.pojoToJsonFile(lmsPlayers, LmsPlayers.saveToFileJson);
    }

    public void read() {
        log.info("READ FILE: " + LmsPlayers.saveToFileJson);
        lmsPlayers.players = new ArrayList<>();
        LmsPlayers lp = JsonUtils.jsonFileToPojo(LmsPlayers.saveToFileJson, LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            lmsPlayers = lp;
        }
        log.info("PLAYERS FROM lms_players.json: " + lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public Player playerByCorrectName(String name) {
//        log.debug("BY NAME: " + name);
        Player player = new Player();
        if (name == null) return null;
        if (lmsPlayers.players == null) return null;
        player = lmsPlayers.players.stream()
                .filter(p -> (p.getName() != null))
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (player == null) log.info("PLAYER NOT FOUND BY NAME: " + name);
//        else log.info("BY NAME: " + name + " GET PLAYER: " + player);
        return player;
    }

    public Player playerByNearestName(String player) {
        log.info("START: " + player);
        if (player == null) return null;
        List<String> players = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
//        String correctPlayerName = Levenstein.getNearestElementInList(player, players);
        String correctPlayerName = Levenstein.getNearestElementInListWord(player, players);
//        String correctPlayerName = Levenstein.search(player, players);
        if (correctPlayerName == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayerName);
        Player correctPlayer = lmsPlayers.playerByCorrectName(correctPlayerName);
        return correctPlayer;
    }

    public Player playerByCorrectRoom(String room) {
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

    public Player playerByNearestRoom(String room) {
        log.info("GET PLAYER BY NEAREST ROOM: " + room);
        if (room == null) return null;
        room = Utils.getCorrectRoomName(room);
        Player player = lmsPlayers.playerByCorrectRoom(room);
        return player;
    }

    public Player playingPlayer(String exceptName, boolean exceptSeparated) {
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated);
        if (playingPlayers != null) return playingPlayers.get(0);
        return null;
    }

    public List<Player> playingPlayers(String exceptName, boolean exceptSeparated) {
        log.info("SEARCH FOR PLAYING. EXCEPT NAME: " + exceptName + ". EXCEPT SEPARATED: " + exceptSeparated);
        lmsPlayers.updateLmsPlayers();
        List<Player> playingPlayers = null;

        playingPlayers = lmsPlayers.players.stream()
                .filter(p -> !exceptSeparated || !p.separate)
                .filter(p -> p.playing)
                .filter(p -> !exceptName.equals(p.name))
                .filter(p -> {
                    String path = p.path();
                    return path != null && !path.equals(config.silence);
                })
                .collect(Collectors.toList());

        if (playingPlayers == null || playingPlayers.size() == 0) {
            log.info("NO PLAYING PLAYERS");
            return null;
        }
        log.info("PLAYING PLAYERS: " + playingPlayers);
        return playingPlayers;
    }

    public List<String> playingPlayersNames(String exceptName, Boolean exceptSeparated) {
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated);
        if (playingPlayers == null) return new ArrayList<>();
        return playingPlayers.stream().map(p -> p.name).collect(Collectors.toList());
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String playerName = parameters.getOrDefault(player_name_value, "null");
        String roomName = parameters.getOrDefault(player_room_value, "null");
        String delay = parameters.getOrDefault(player_delay_value, "null");
        String schedule = parameters.getOrDefault(player_schedule_value, "null");
        log.info("name: " + playerName);
        log.info("room: " + roomName);
        log.info("delay: " + delay);
        log.info("schedule: " + schedule);
        if (playerName.equals("null") || roomName.equals("null") || delay.equals("null") || schedule.equals("null")) {
            log.info("ERROR PARAMETER NULL");
            return "NULL";
        }
        Player player = lmsPlayers.playerByCorrectName(playerName);
        player.delay = Integer.valueOf(delay);
        player.schedule = Utils.stringSplitToIntMap2(schedule, ",", ":");
        SwitchVoiceCommand.room = roomName;
        Player playerNew = SwitchVoiceCommand.selectPlayerInRoom(playerName, roomName, false);
        log.info("SELECT PLAYER NEW: " + playerNew);
        write();
        log.info("FINISH PLAYER SAVE");
        return "OK";
    }

    public String playerRemove(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        String name = parameters.get("name");
        Player player = lmsPlayers.playerByCorrectName(name);
        log.info("PLAYER DEVICE ID: " + player.deviceId);
        int id = Integer.parseInt(player.deviceId);
        log.info("PLAYER REMOVE: " + player);
        lmsPlayers.players.remove(player);
        Device device = SmartHome.getDeviceById(id);
        SmartHome.devices.remove(device);
        write();
        return "OK";
    }

    public List<String> separatedPlayers(Player excludePlayer) {
        log.info("TRY GET SEPARATED PLAYERS");
        lmsPlayers.updateLmsPlayers();


        List<String> separatePlayers =
                lmsPlayers.players.stream()
                        .peek(p -> log.debug(p.name + " separate " + p.separate))
                        .filter(p -> p.separate)
                        .filter(p -> !p.name.equals(excludePlayer.name))
//                        .peek(p -> log.info(p.name + " filter separate " + p.separate))
                        .map(p -> {
                            if (p.playing) return p.name;
                            return " не играет " + p.name;
                        })
                        .collect(Collectors.toList());
        log.info("SEPARATED PLAYERS: " + separatePlayers);
        return separatePlayers;
    }

    public Player playerByDeviceId(String id) {
        if (id == null) {
            log.info("ERROR NO PLAYER BY ID: " + id);
            return null;
        }
        Player player = lmsPlayers.players.stream()
                .peek(p -> log.info("PLAYER ID: " + p.deviceId + " ROOM: " + p.room))
                .filter(p -> p.room != null)
                .filter(p -> p.deviceId != null)
                .filter(p -> p.deviceId.equals(id))
                .findFirst().orElse(null);
        log.info("ID: " + id + " PLAYER BY ID: " + player);
        if (player != null) log.info("ID: " + id + " PLAYER: " + player.name);
        return player;
    }

    public String playerNameByDeviceId(String id) {
        Player player = playerByDeviceId(id);
        if (player == null) return null;
        return player.name;
    }

    public void delayExpireSave(HashMap<String, String> parameters) {
        String tmp = parameters.get(delay_expire_value);
        if (tmp == null) return;
        delayExpire = Integer.parseInt(tmp);
        write();
    }

    public void autoremoteSave(HashMap<String, String> parameters) {
        String tmp = parameters.get(autoremote_value);
        if (tmp == null) return;
        autoRemoteRefresh = tmp;
        write();
    }

    public void altSyncSave(HashMap<String, String> parameters) {
        String tmp = parameters.get(alt_sync_value);
        if (tmp == null) return;
        syncAlt = Boolean.parseBoolean(tmp);
        log.info("ALT SYNC SAVE syncAlt: " + syncAlt);
        write();
    }

    public void lastThisSave(HashMap<String, String> parameters) {
        String tmp = parameters.get(last_this_value);
        if (tmp == null) return;
        lastThis = Boolean.parseBoolean(tmp);
        write();
    }

    public void lmsSave(HashMap<String, String> parameters) {
        String tmp1 = parameters.get(lms_ip_value);
        String tmp2 = parameters.get(lms_port_value);
        if (tmp1 == null || tmp2 == null) return;
        config.lmsIp = tmp1;
        config.lmsPort = tmp2;
        config.write();
        lmsPlayers.searchForLmsIp();
        lmsPlayers.updateLmsPlayers();
    }

    //  если пришла команда Алисы включи музыку везде
    public void turnOnMusicAll(List<Device> devicesForTurnOn) {
        log.info("START TURN ON ALL");
// если плееров ЛМС нет то выход
        if (lmsPlayers.players == null || lmsPlayers.players.size() == 0) {
            log.info("NO PLAYERS");
            return;
        }
        //       взять плееры только подключенные в умный дом с Алисой
        List<Player> playersForTurnOn;

        if (devicesForTurnOn != null)
            playersForTurnOn = devicesForTurnOn.stream().map(d -> lmsPlayers.playerByDeviceId(d.id)).collect(Collectors.toList());
        else
            playersForTurnOn = SmartHome.devices.stream().map(d -> lmsPlayers.playerByDeviceId(d.id)).collect(Collectors.toList());

// если в запросе провайдера указан канал то неискать играющий - включить канал из провайдера

        if (ProviderAction.channelValueByProvider != null) {
            log.info("CHNNEL BY PROVIDER: " + ProviderAction.channelValueByProvider);
        }

//        найти играющий плеер до выполнения пробуждения всех
        Player playingPlayer = lmsPlayers.playingPlayer("", true);
        log.info("PLAYING PLAYER: " + playingPlayer);
//        если нет играющего взять первый плеер, если и его нет то выход
        if (playingPlayer == null) {
            playingPlayer = playersForTurnOn.get(0);
            log.info("NO PLAYING. GET FIRST PLAYER " + playingPlayer.name);
        }

//        разбудить все плееры одновременно в паралельных потоках
        List<CompletableFuture<Void>> futures = playersForTurnOn.stream()
                .map(p -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() ->
                            p.ifExpiredAndNotPlayingUnsyncWakeSet()
                    ).exceptionally(ex -> { // Обработка исключений
                        log.error("Task failed: " + ex.getMessage());
                        return null;
                    });
//                    log.info("WAITOK");
                    return future;
                })
                .collect(Collectors.toList());
// дождаться пробуждения всех плееров
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("FINISH WAIT FOR WAKE ALL");
//       включить один плеер
        playingPlayer.playLast();
        String name = playingPlayer.name;
        log.info("NAME PLAYING: " + name);
//         подключить все остальные к играющему
        List<CompletableFuture<Void>> futures2 = playersForTurnOn.stream()
                .filter(p -> !p.name.equals(name))
                .map(p -> {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> p.syncTo(name).saveLastTimePath())
                            .thenRunAsync(() ->
                                    log.info("SYNC OK")
                            ).exceptionally(ex -> { // Обработка исключений
                                log.error("Task failed: " + ex.getMessage());
                                return null;
                            });
//                    log.info("OK");
                    return future;
                })
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures2.toArray(new CompletableFuture[0]))
                .thenRunAsync(() -> autoremoteRequest());
        log.info("FINISH TURN ON ALL"); // Теперь выполнится даже при ошибках в задачах
    }

    public String turnOffMusicAll() {
        log.info("STOP ALL");
        lmsPlayers.updateLmsPlayers();
        List<CompletableFuture<Void>> futures = lmsPlayers.players.stream()
                .filter(player -> player.connected)
                .filter(player -> player.playing)
                .map(p -> CompletableFuture.runAsync(() -> p.turnOffMusic().saveLastTimePath()))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRunAsync(() -> autoremoteRequest());
        return "OK";
    }

    public void autoremoteRequest() {
//        записать в файл lmsPlayers.json
        lmsPlayers.write(); // save last
//        запрос обновления виджетов
        log.info("REQUEST AUTOREMOTE REFRESH");
        Requests.autoRemoteRefresh();
    }

    public String forTaskerPlayersIconsJson(Player player, String value) {
        log.info("WIDGETS JSON");
        Widgets widgets = new Widgets();
//        get_widgets
        widgets.widgetsData = new ArrayList<>();
//        get_playlist
        widgets.playerslistWidgetText = player.forTaskerPlaylist(value);
//        get_players
        widgets.playlistWidgetText = lmsPlayers.forTaskerPlayersList();

        lmsPlayers.players.stream().forEach(p -> {
            if (p.playing) {
                Widgets.Play play = new Widgets.Play();
                play.player = p.name;
                play.volume = p.volume;
                play.mode = p.mode;
                play.title = p.title;
                widgets.playList.add(play);
            }
        });
        log.info("WIDGETS OBJECT: " + widgets);
        String widgetsJson = JsonUtils.pojoToJson(widgets);
        log.info("WIDGETS JSON: " + widgetsJson);
        return widgetsJson;
    }

    public String forTaskerWidgetsIcons() {
        log.info("SUPER REFRESH START >>>");
        lmsPlayers.updateLmsPlayers();
        lmsPlayers.syncgroups();
        List<String> roomNames = rooms;
        List<String> playersNames = lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList());
        List<String> roomsAndPlayersNames = new ArrayList<>();
        roomsAndPlayersNames.addAll(roomNames);
        roomsAndPlayersNames.addAll(playersNames);
        List<String> roomsAndPlayersModes = new ArrayList<>();
        List<String> roomsAndPlayersSyncs = new ArrayList<>();
        List<String> roomsAndPlayersSeparates = new ArrayList<>();
        List<String> roomsAndPlayersTitles = new ArrayList<>();
        List<String> playersVolModTit = new ArrayList<>();
//        log.info("roomNames: " + roomNames);
//        log.info("playersNames: " + playersNames);
        roomNames.stream()
                .map(r -> lmsPlayers.playerByCorrectRoom(r))
                .peek(p -> {
                    if (p != null) {
                        p.status();
                        roomsAndPlayersModes.add(p.mode);
                        roomsAndPlayersSyncs.add(String.valueOf(p.sync));
                        roomsAndPlayersSeparates.add(String.valueOf(p.separate));
                        roomsAndPlayersTitles.add(p.title);
                    } else {
                        roomsAndPlayersModes.add("null");
                        roomsAndPlayersSyncs.add(String.valueOf(false));
                        roomsAndPlayersSeparates.add("null");
                        roomsAndPlayersTitles.add("null");
                    }
                })
                .collect(Collectors.toList());

        playersNames.stream()
                .map(r -> lmsPlayers.playerByCorrectName(r))
                .peek(p -> {
                    if (p != null) {
//                        log.info("PLAYER SYNC: " + p.sync);
                        roomsAndPlayersModes.add(p.mode);
                        roomsAndPlayersSyncs.add(String.valueOf(p.sync));
                        roomsAndPlayersSeparates.add(String.valueOf(p.separate));
                        roomsAndPlayersTitles.add(p.title);
                        playersVolModTit.add(p.name + "-" + p.volume + "-" + p.mode + "-" + p.title);
                    } else {
//                        log.info("PLAYER NULL");
                        roomsAndPlayersModes.add("null");
                        roomsAndPlayersSyncs.add(String.valueOf(false));
                        roomsAndPlayersSeparates.add("null");
                        roomsAndPlayersTitles.add("null");
                    }
                })
                .collect(Collectors.toList());

        log.info("NAMES: " + roomsAndPlayersNames);
        log.info("MODES: " + roomsAndPlayersModes);
        log.info("SYNCS: " + roomsAndPlayersSyncs);
        log.info("TITLES: " + roomsAndPlayersTitles);
        log.info("SEPARATES: " + roomsAndPlayersSeparates);
        String joinedRoomsAndPlayersNames = String.join(",", roomsAndPlayersNames);
        String joinedRoomsAndPlayersModes = String.join(",", roomsAndPlayersModes);
        String joinedRoomsAndPlayersSyncs = String.join(",", roomsAndPlayersSyncs);
        String joinedRoomsAndPlayersSeparates = String.join(",", roomsAndPlayersSeparates);
        String joinedRoomsAndPlayersTitles = String.join(",", roomsAndPlayersTitles);
        String joinedPlayersVolModTit = String.join(",", playersVolModTit);

        String response = joinedRoomsAndPlayersNames + ":"
                + joinedRoomsAndPlayersModes + ":"
                + joinedRoomsAndPlayersSyncs + ":"
                + joinedRoomsAndPlayersTitles + ":"
                + joinedPlayersVolModTit + ":"
                + joinedRoomsAndPlayersSeparates;
        log.info("SUPER REFRESH FINISH <<<");
        return response;
    }

//    public String lastTitle(Player player) {
//        String selectLast = null;
//        String result = null;
//        if (player == null) return "";
//        if (lmsPlayers.lastThis) {
//            selectLast = player.lastTitle;
//            result = "play last this: " + selectLast + " other: " + lmsPlayers.lastTitle;
//        } else {
//            selectLast = lmsPlayers.lastTitle;
//            result = "play last other: " + selectLast + " other: " + lmsPlayers.lastTitle;
//        }
//        log.info("PLAYER: " + player.name + " LAST TITLE: " + selectLast);
//        return result;
//    }

    public String playerNameByWidgetName(String value) {
        log.info("GET PLAYER BY WIDGET: " + value);
        Player player1 = null;
        String playerName = "ERROR";
        String roomName;
        roomName = Utils.getCorrectRoomName(value);
//        log.info("ITS ROOM: " + roomName);
        if (roomName != null) player1 = lmsPlayers.playerByCorrectRoom(roomName);
//        log.info("PLAYER BY ROOM: " + player1);
        if (player1 != null) playerName = player1.name;
//        log.info("PLAYER NAME BY ROOM: " + playerName);
        if (player1 == null) {
//            log.info("ITS PLAYER !!!");
            playerName = Utils.getCorrectPlayerName(value);
//            log.info("PLAYER: " + player1);
            if (playerName != null) roomName = lmsPlayers.playerByCorrectName(playerName).room;
//            log.info("ROOM BY PLAYER: " + player1);
        }
        log.info("ROOM: " + roomName + " PLAYER: " + playerName);
        String result = roomName + "," + playerName;
        return result;
    }

    public List<List<String>> syncgroups() {
        Response response = Requests.postToLmsForResponse(RequestParameters.syncgroups().toString());
//        this.players.forEach(p -> p.sync = false);
        if (response == null) return null;
        if (response.result.syncgroups_loop == null) return null;
        log.info("SYNCGROUPS LOOP: " + response.result.syncgroups_loop);
        List<List<String>> syncMemberNames = response.result.syncgroups_loop.stream()
                .map(syncgroupsLoop -> syncgroupsLoop.sync_member_names)
                .map(s -> List.of(s.split(",")))
                .collect(Collectors.toList());
        List<Object> result = new ArrayList<>();
        syncMemberNames.forEach(result::addAll);
//        this.players.stream()
//                .filter(p -> result.contains(p.name))
//                .forEach(p -> p.sync = true);
//        log.info("syncMemberNames: " + result);
        log.info("SYNCGROUPS: " + syncMemberNames);
        return syncMemberNames;
    }

    public void searchForLmsIp() {
        log.info("SEARCH FOR LMS IP");
        String lmsIp = LmsSearchForIp.findServerIp();
        if (lmsIp != null) {
            log.info("LMS HAS BEEN FOUND: " + lmsIp);
            lmsServerOnline = true;
            config.lmsIp = lmsIp;
            config.write();
        } else {
            log.info("LMS WAS NOT FOUND");
            lmsServerOnline = false;
        }
    }

    public String forTaskerPlayersList() {
        lmsPlayers.updateLmsPlayers();

//        получить лист избранного из первого плеера для отпледеления номера канала
        List<String> favList = this.players.get(0).favorites();

        List<String> listPlayingPlayers = lmsPlayers.players.stream()
                .filter(p -> p.playing)
                .map(p -> {
                    String currentChannel = "";
                    String currentTitle = p.playlistName();
                    if (favList.contains(currentTitle))
                        currentChannel = String.valueOf(favList.indexOf(currentTitle) + 1) + ".";
                    if (p.room == null)
                        return p.name + " - " + p.volume + " - " + p.mode + " - " + currentChannel + p.title;
                    else
                        return p.room + " - " + p.name + " - " + p.volume + " - " + p.mode + " - " + currentChannel + p.title;
                })
                .collect(Collectors.toList());
        String playingPlayers = String.join(",", listPlayingPlayers);
        if (listPlayingPlayers.size() == 0) playingPlayers = "all players stop";

        log.info("PLAYING: " + playingPlayers);

        List<String> listNotPlayingPlayers = lmsPlayers.players.stream()
                .filter(p -> !p.playing)
                .map(p -> {
                    if (p.room == null)
                        return p.name + " - " + p.volume + " - " + p.mode + " - " + p.title;
                    else
                        return p.room + " - " + p.name + " - " + p.volume + " - " + p.mode + " - " + p.title;
                })
                .collect(Collectors.toList());
        String notPlayingPlayers = String.join(",", listNotPlayingPlayers);
        log.info("NOT PLAYING: " + notPlayingPlayers);

        String result = playingPlayers + ";" + notPlayingPlayers;
        return result;
    }

    public void checkRooms() {
        log.info("CHECK ROOMS START");
        this.players.stream()
                .filter(player -> player.room != null)
                .filter(player -> player.deviceId != null)
                .forEach(p -> {
                    log.info("PLAYER: " + p.name + " ROOM: " + p.room + " ID: " + p.deviceId);
                    Device dev = SmartHome.devices.stream()
                            .filter(device -> device.room.equals(p.room))
                            .findFirst().orElse(null);
                    if (dev != null) {
                        log.info("DEVICE ROOM:" + dev.room + " ID: " + dev.id);
                        if (!p.deviceId.equals(dev.id)) {
                            p.deviceId = dev.id;
                            log.info("FIX PLAYER: " + p.name + " ROOM: " + p.room + " ID: " + p.deviceId);
                        }
                    }
                });
    }
}