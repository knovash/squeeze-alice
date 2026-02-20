package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatusByName;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
import org.knovash.squeezealice.yandex.Yandex;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.PagePlayers.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayers {

    public List<Player> players;
    //    public List<String> playersNames = new ArrayList<>();
//    public List<String> playersNamesOnLine = new ArrayList<>();
//    public List<String> playersNamesOffLine = new ArrayList<>();
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
    public List<String> autoRemoteUrls = new ArrayList<>();
    public static String saveToFileJson = "data/lms_players.json";

    public List<String> playingPlayersNames;
    public List<String> playingPlayersNamesNotInCurrentGrop;
    public List<String> playersNamesInCurrentGroup;

    public static String widgetsNames;
    public static String widgetsModes;
    public static String widgetsSyncs;
    public static String widgetsSeparates;
    public static String nowPlaying;
    public static String nowPlayingTv;
//    public static String widgetPlayersTitles;
//    public static String joinedPlayersVolModTit;

    public static String widgetPlayersPlay;
    public static String widgetPlayersStop;
    public Boolean toggleWake = false;

    public Map<Integer, Integer> scheduleAll = new HashMap<>(Map.of(
            0, 10,
            7, 15,
            9, 20,
            20, 15,
            22, 5));

    public List<String> favorites() {
        log.info("GET FAVORITES LIST");
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites("", 100).toString());
        List<String> favList = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return favList;
    }

    public void updateLmsPlayers() {
        log.info("UPDATE PLAYERS FROM LMS");
        if (!lmsServerOnline) return;
        if (this.players == null) this.players = new ArrayList<>();
        String json = Requests.postToLmsForJsonBody(RequestParameters.serverstatusname().toString());
//        log.info("UPDATE RESPONSE:\n" + json);
        if (json == null) return;
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
// получить лист плееров из LMS
        serverStatus = JsonUtils.jsonToPojo(json, ServerStatusByName.class);
        if (serverStatus == null) return;
// обновить состояние всех плееров полученных в players_loop из LMS
//        playersNamesOnLine = new ArrayList<>();

// очистить состояние локальных плееров, которых могло не оказаться в LMS
//        this.players.forEach(player ->player.clean());

        List<String> lmsPlayersList = serverStatus.result.players_loop.stream().map(playersLoop -> playersLoop.name).collect(Collectors.toList());
        List<String> localPlayersList = this.players.stream().map(player -> player.name).collect(Collectors.toList());
        Set<String> fullPlayersSet = new HashSet<>(); // Создаём HashSet из первого списка
        fullPlayersSet.addAll(lmsPlayersList); // Добавляем элементы второго списк
        fullPlayersSet.addAll(localPlayersList); // Добавляем элементы второго списк

        fullPlayersSet.forEach(player -> updatePlayer(player));
    }

    public void updatePlayer(String playerName) {
//        playersNamesOnLine.add(playerName);
// попытка найти этот плеер в подключенных по имени
        Player player = this.playerByCorrectName(playerName);
        if (player == null) {
// если плеера еще нет то создать новый плеер обновить статус и добавить
            player = new Player(playerName);
            log.info("ADD NEW PLAYER: " + player.name);
// обновить статус и добавить новый плеер в подключенные
            this.players.add(player);
        }
// если плеер уже есть то обновить статус плеера
        player.status(); // updatePlayer
    }

    public void write() {
        log.info("\nWRITE FILE: " + LmsPlayers.saveToFileJson);
        JsonUtils.pojoToJsonFile(this, LmsPlayers.saveToFileJson);
    }

    public void read() {
        log.info("READ FILE: " + LmsPlayers.saveToFileJson);
        LmsPlayers lp = JsonUtils.jsonFileToPojo(LmsPlayers.saveToFileJson, LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
            return;
        }

        // Сохраняем все поля из прочитанного объекта
        this.players = lp.players;
        this.autoRemoteUrls = lp.autoRemoteUrls; // <-- Теперь это поле тоже загружается

        this.toggleWake = lp.toggleWake;


        this.lastPath = lp.lastPath;
        this.lastChannel = lp.lastChannel;
//        this.btPlayerInQuery = lp.btPlayerInQuery;
        this.btPlayerName = lp.btPlayerName;
//        this.tvPlayerInQuery = lp.tvPlayerInQuery;
//        this.tvPlayerName = lp.tvPlayerName;
        this.delayUpdate = lp.delayUpdate;
        this.delayExpire = lp.delayExpire;
        this.syncAlt = lp.syncAlt;
        this.lastThis = lp.lastThis;
//        this.playingPlayersNames = lp.playingPlayersNames;
//        this.playingPlayersNamesNotInCurrentGrop = lp.playingPlayersNamesNotInCurrentGrop;
//        this.playersNamesInCurrentGroup = lp.playersNamesInCurrentGroup;

        log.info("PLAYERS FROM lms_players.json: " + this.players.stream().map(p -> p.name).collect(Collectors.toList()));
        log.info("autoRemoteUrls LOADED: " + this.autoRemoteUrls);
    }


    public void read2() {
        log.info("READ FILE: " + LmsPlayers.saveToFileJson);
        this.players = new ArrayList<>();
        LmsPlayers lp = JsonUtils.jsonFileToPojo(LmsPlayers.saveToFileJson, LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            this.players = lp.players;
        }
        log.info("PLAYERS FROM lms_players.json: " + this.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public Player playerByCorrectName(String name) {
        Player player;
        if (name == null || this == null || this.players == null) return null;
        player = this.players.stream()
                .filter(p -> name.equals(p.name))
                .findFirst()
                .orElse(null);
        if (player == null) log.debug("PLAYER NOT FOUND BY NAME: " + name);
        return player;
    }

    public Player playerByNearestName(String player) {
//        log.info("START: " + player);
        if (player == null) return null;
        List<String> players = this.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
        String correctPlayerName = Levenstein.getNearestElementInListWord(player, players);
        if (correctPlayerName == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayerName);
        Player correctPlayer = this.playerByCorrectName(correctPlayerName);
        return correctPlayer;
    }

    public Player playerByCorrectRoom(String room) {
//        log.info("ROOM: " + room);
        Player player = new Player();
        Optional<Player> optionalPlayer = this.players.stream()
                .filter(p -> (p.room != null))
//                .peek(p -> log.info("0: " + p.name + " " + p.room + " = " + room + " " + p.room.equals(room)))
                .filter(p -> p.room.equals(room))
                .filter(Objects::nonNull)
                .findFirst();
        player = (Player) optionalPlayer.orElse(null);
//        if (player != null) log.info("ROOM: " + room + " PLAYER: " + player.name);
//        if (player == null) log.info("PLAYER NOT FOUND WHITH ROOM " + room);
        return player;
    }

    public Player playerByNearestRoom(String room) {
//        log.info("GET PLAYER BY NEAREST ROOM: " + room);
        if (room == null) return null;
        room = Utils.getCorrectRoomName(room);
        Player player = this.playerByCorrectRoom(room);
        return player;
    }

    public Player playingPlayer(String exceptName, boolean exceptSeparated) {
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated); //playingPlayer
        if (playingPlayers != null) return playingPlayers.get(0);
        return null;
    }

    public List<Player> playingPlayers(String exceptName, boolean exceptSeparated) {
        List<Player> playingPlayers = null;
        playingPlayers = this.players.stream()
                .filter(p -> !exceptSeparated || !p.separate)
                .filter(p -> p.playing) // playingPlayers
                .filter(p -> !exceptName.equals(p.name))
                // TODO вернуть если ошибки с плеерами которые играют тишину
//                .filter(p -> {
//                    String path = p.path();
//                    return path != null && !path.equals(config.silence);
//                })
                .collect(Collectors.toList());
        if (playingPlayers == null || playingPlayers.size() == 0) {
            log.info("SEARCH FOR PLAYING. EXCEPT NAME: " + exceptName + ". EXCEPT SEPARATED: " + exceptSeparated + " NO PLAYING PLAYERS");
            return null;
        }
        log.info("SEARCH FOR PLAYING. EXCEPT NAME: " + exceptName + ". EXCEPT SEPARATED: " + exceptSeparated + " PLAYING PLAYERS: " + playingPlayers.stream().map(player -> player.name).collect(Collectors.toList()));
        return playingPlayers;
    }

    public List<String> playingPlayersNames(String exceptName, Boolean exceptSeparated) {
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated); // playingPlayersNames
        if (playingPlayers == null) return new ArrayList<>();
        return playingPlayers.stream().map(p -> p.name).collect(Collectors.toList());
    }

    public String playerSave(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String playerName = parameters.getOrDefault(player_name_value, "null");
        String roomName = parameters.getOrDefault(player_room_value, "null");
        String delay = parameters.getOrDefault(player_delay_value, "null");
        String volumeMax = parameters.getOrDefault(player_volume_max_value, "null");
        String schedule = parameters.getOrDefault(player_schedule_value, "null");
        log.info("name: " + playerName);
        log.info("room: " + roomName);
        log.info("delay: " + delay);
        log.info("volumeMax: " + volumeMax);
        log.info("schedule: " + schedule);

        links.addLinkPlayer(roomName, playerName);
        links.write();
        log.info(links);

        if (playerName.equals("null") || roomName.equals("null") || delay.equals("null") || schedule.equals("null")) {
            log.info("ERROR PARAMETER NULL");
            return "NULL";
        }
        Player player = this.playerByCorrectName(playerName);
        player.delay = Integer.valueOf(delay);
        player.volume_high = Integer.valueOf(volumeMax);
        player.schedule = Utils.stringSplitToIntMap2(schedule, ",", ":");
        SwitchVoiceCommand.room = roomName;
        Player playerNew = SwitchVoiceCommand.selectPlayerInRoom(playerName, roomName, false); // playerSave
        log.info("SELECT PLAYER NEW: " + playerNew);
        write();
        log.info("FINISH PLAYER SAVE");
        return "OK";
    }

    public String playerRemove(HashMap<String, String> parameters) {
        log.info("PLAYER SAVE PARAMETERS: " + parameters);
        String playerName = parameters.getOrDefault(player_name_value, "null");
        String roomName = parameters.getOrDefault(player_room_value, "null");
        log.info("name: " + playerName);
        log.info("room: " + roomName);
//        linkss.addLinkPlayer(roomName,playerName);
//        log.info(linkss);

        Player player = this.playerByCorrectName(playerName);
        log.info("PLAYER DEVICE ID: " + player.deviceId);
        String id = null;
        if (player.deviceId != null) id = player.deviceId;
        log.info("PLAYER REMOVE: " + player);
        this.players.remove(player);
        if (id != null) smartHome.devices.remove(smartHome.getDeviceById(id));
//        Device device = SmartHome.getDeviceById(id);
        write();
        return "OK";
    }

    public Player playerByDeviceId(String extIdPlayerName) {
        if (extIdPlayerName == null) {
            log.info("ERROR NULL ID: " + extIdPlayerName);
            return null;
        }

//        Player player = this.players.stream()
////                .peek(p -> log.info("PLAYER ID: " + p.deviceId + " ROOM: " + p.room))
//                .filter(p -> p.room != null)
//                .filter(p -> p.name != null)
//                .filter(p -> p.name.equals(extIdPlayerName))
//                .findFirst().orElse(null);

        Player player = this.players.stream()
//                .peek(p -> log.info("PLAYER ID: " + p.deviceId + " ROOM: " + p.room))
                .filter(p -> p.room != null)
                .filter(p -> p.deviceId != null)
                .filter(p -> p.deviceId.equals(extIdPlayerName))
                .findFirst().orElse(null);

//        log.info("ID: " + id + " PLAYER BY ID: " + player);
        if (player != null) log.debug("BY ID: " + extIdPlayerName + " PLAYER: " + player.name);
        else log.info("ERROR PLAYER NULL BY ID: " + extIdPlayerName);
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

    public void toggleWakeSave(HashMap<String, String> parameters) {
        String tmp = parameters.get(toggle_wake_value);
        log.info("TMP: =========== " + tmp);
        if (tmp == null) return;
        toggleWake = Boolean.valueOf(tmp);
        write();
    }

    public void autoremoteSave(HashMap<String, String> parameters) {
        log.info("START " + parameters);
        String tmp = parameters.get(autoremote_value);
        log.info("TMP " + tmp);
        if (tmp == null) return;
        if (autoRemoteUrls == null) autoRemoteUrls = new ArrayList<>();
        autoRemoteUrls.add(tmp);
        write();
    }

    public void autoremoteRemove(HashMap<String, String> parameters) {
        log.info("START " + parameters);
        String tmp = parameters.get(autoremote_value);
        log.info("TMP " + tmp);
        if (tmp == null) return;
        if (autoRemoteUrls == null) autoRemoteUrls = new ArrayList<>();
        autoRemoteUrls.remove(tmp);
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
        this.searchForLmsIp();

        log.info("\nUPDATE LMS PLAYERS");
        this.updateLmsPlayers(); // lmsSave
    }

    public void turnOffMusicAll() { // для Таскер только
        log.info("\nSTOP ALL");
        this.players.parallelStream()
                .filter(player -> player.connected)
                .peek(player -> log.info(player.name))
                .forEach(player -> player.turnOffMusic());
    }


    public void autoremoteRequest() {
        log.info("REQUEST TASKER AUTOREMOTE REFRESH");
        log.info("URLS SIZE: {}", this.autoRemoteUrls.size());

        this.autoRemoteUrls.forEach(url -> {

            log.info("POST TO AUTOREMOTE: {}", url);
            try {
                HttpResponse response = Request.Post(url)
                        .connectTimeout(5000)
                        .socketTimeout(5000)
                        .execute()
                        .returnResponse();

                int statusCode = response.getStatusLine().getStatusCode();

                log.error("POST. Status: {}, URL: {}", statusCode, url);

            } catch (Exception e) {
                log.error("POST ERROR. URL: " + url, e);
            }
        });
    }


    public void autoremoteпRequest() {
        log.info("\nREQUEST TASKER AUTOREMOTE REFRESH");
        log.info("URLS SIZE: " + this.autoRemoteUrls.size());
        this.autoRemoteUrls.stream().forEach(url -> {
            log.info("POST TO AUTOREMOTE: " + url);
            if (url == null) return;
            try {
                Request.Post(url).execute();
            } catch (IOException e) {
                log.info("POST ERROR " + e);
            }
        });
    }

    public String forTaskerWidgetsRefreshJson(Player player, String lines) {
        log.info("\nSTART TASKER WIDGETS REFRESH JSON");
        log.info("\nTASKER WIDGET PLAYERS LIST");
        this.forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        log.info("\nTASKER WIDGET ICONS");
        this.forTaskerWidgetsIcons(); // для виджетов иконок плееров
        String responsePlaylist = "[]";
        LmsPlayers.nowPlaying = "offline";
        if (player != null) {
            log.info("\nTASKER WIDGET PLAY LIST FOR " + player.name);
            responsePlaylist = player.forTaskerPlaylist(lines); // для виджета плейлиста
            if (responsePlaylist == null) {
//                log.info("--- responsePlaylist = player.playlistNameShort;");
//                log.info("--- PLAYLIST NAME SHORT: " + player.playlistNameShort);
                responsePlaylist = player.playlistNameShort;
            }
            log.info("\nTASKER WIDGET ONE ICON NOW PLAYING");
//            log.info("--- LmsPlayers.nowPlaying = player.playlistNameShort");
//            log.info("PLAYLIST NAME SHORT: " + player.playlistNameShort);
//            log.info("title: " + player.title);
//            log.info("playlistName: " + player.playlistName);
//            log.info("playlistNameShort: " + player.playlistNameShort);
            String title = player.playlistNameShort;
            if(player.playlistNameShort == null) title = player.requestTitle();
            LmsPlayers.nowPlaying = title; // для виджета одной иконкой для телефона где неработает плагин
            LmsPlayers.nowPlayingTv = player.name + " - " + player.volume + " - " + player.mode + " - " + title; // для виджета одной иконкой для телефона где неработает плагин
        }

//        log.info("\nTASKER WIDGET PLAYERS LIST");
//        this.forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        String responseJson = "{\n" +
                "  \"PLAYLIST\": \"" + responsePlaylist + "\",\n" +
                "  \"PLAYERS_PLAY\": \"" + widgetPlayersPlay + "\",\n" +
                "  \"PLAYERS_STOP\": \"" + widgetPlayersStop + "\",\n" +
                "  \"ROOMSPLAYERS\": \"" + widgetsNames + "\",\n" +
                "  \"MODES\": \"" + widgetsModes + "\",\n" +
                "  \"SYNCS\": \"" + widgetsSyncs + "\",\n" +
                "  \"SEPARATES\": \"" + widgetsSeparates + "\",\n" +
                "  \"NOWPLAYING\": \"" + nowPlaying + "\",\n" +
                "  \"NOWPLAYINGTV\": \"" + nowPlayingTv + "\"\n" +
                "}";
        log.info(responseJson);
        return responseJson;
    }

    public void forTaskerWidgetsIcons() {
        this.syncgroups();
        List<String> iconsNames = new ArrayList<>(rooms);
        iconsNames.addAll(this.players.stream().map(p -> p.name).collect(Collectors.toList()));
        List<String> modes = new ArrayList<>();
        List<String> syncs = new ArrayList<>();
        List<String> separates = new ArrayList<>();
        iconsNames.forEach(name -> {
            Player player = this.playerByCorrectRoom(name);
            if (player == null) player = this.playerByCorrectName(name);
            modes.add(player != null ? player.mode : "null"); // forTaskerWidgetsIcons
            syncs.add(player != null ? String.valueOf(player.sync) : "false");
            separates.add(player != null ? String.valueOf(player.separate) : "null");
        });
        log.info("NAMES: " + iconsNames);
        log.info("MODES: " + modes);
        log.info("SYNCS:" + syncs);
        log.info("SEPARATES: " + separates);
        widgetsNames = String.join(",", iconsNames);
        widgetsModes = String.join(",", modes);
        widgetsSyncs = String.join(",", syncs);
        widgetsSeparates = String.join(",", separates);
    }


    public String playerNameByWidgetName(String value) {
        log.info("GET PLAYER BY WIDGET: " + value);
        Player player1 = null;
        String playerName = "ERROR";
        String roomName;
        roomName = Utils.getCorrectRoomName(value);
        if (roomName != null) player1 = this.playerByCorrectRoom(roomName);
        if (player1 != null) playerName = player1.name;
        if (player1 == null) {
            playerName = Utils.getCorrectPlayerName(value);
            if (playerName != null) roomName = this.playerByCorrectName(playerName).room;
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
//        log.info("SYNCGROUPS LOOP: " + response.result.syncgroups_loop);
        List<List<String>> syncMemberNames = response.result.syncgroups_loop.stream()
                .map(syncgroupsLoop -> syncgroupsLoop.sync_member_names)
                .map(s -> List.of(s.split(",")))
                .collect(Collectors.toList());
        List<Object> result = new ArrayList<>();
        syncMemberNames.forEach(result::addAll);
        log.info("SYNCGROUPS: " + syncMemberNames);
        return syncMemberNames;
    }

    public void searchForLmsIp() {
        log.info("SEARCH FOR LMS IP");
        String lmsIp = LmsSearchForIp.findServerIp();
        if (lmsIp != null) {
            log.info("LMS IP: " + lmsIp);
            lmsServerOnline = true;
            config.lmsIp = lmsIp;
            config.write();
        } else {
            log.info("ERROR LMS NOT FOUND");
            lmsServerOnline = false;
        }
    }

    public String forTaskerPlayersList() {
        log.info("TASKER PLAYERS LIST");
        List<String> favList = this.players.get(0).favorites(); // forTaskerPlayersList

        Function<Player, String> playerFormatter = p -> {
            if (!p.connected) p.playlistNameShort = "unknown";
            if (!p.connected) p.playlistNameShort = "offline";
            String playlistName = p.requestPlaylistName(); // forTaskerPlayersList -------------------------------------
            String favoritesIndex = "";
            if (playlistName != null && favList.contains(playlistName))
                favoritesIndex = (favList.indexOf(playlistName) + 1) + ".";
            if (playlistName == null) playlistName = p.requestTitle(); // --------------------------------------
            else playlistName = p.playlistNameShort; // forTaskerPlayersList
            String roomNamePlayerName = p.room + " - " + p.name;
            if (p.room == null) roomNamePlayerName = p.name;
//            log.info(roomNamePlayerName + " - " + p.volume + " - " + favoritesIndex + playlistName);
            return roomNamePlayerName + " - " + p.volume + " - " + favoritesIndex + playlistName;
        };

        log.info("\nPLAYING PLAYERS LIST:");
        widgetPlayersPlay = this.players.stream()
                .filter(p -> p.playing)
                .sorted(Comparator.comparing(
                        p -> p.playlistNameShort,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(playerFormatter)
                .collect(Collectors.joining(","));
        if (widgetPlayersPlay.isEmpty()) widgetPlayersPlay = "all players stop";
        log.info("\nNOT PLAYING PLAYERS LIST:");
        widgetPlayersStop = this.players.stream()
                .filter(p -> !p.playing)
                .sorted(Comparator.comparing(
                        p -> p.playlistNameShort,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(playerFormatter)
                .collect(Collectors.joining(","));
        if (widgetPlayersStop.isEmpty()) widgetPlayersStop = "all players play";
        log.info("RESULT PLAYING PLAYERS LIST: " + widgetPlayersPlay);
        log.info("RESULT NOT PLAYING PLAYERS LIST: " + widgetPlayersStop);
        String result = widgetPlayersPlay + ";" + widgetPlayersStop;
        return result;
    }

    public void checkRooms() {
        log.info("START CHECK ROOMS");
        this.players.stream()
                .filter(player -> player.room != null)
                .filter(player -> player.deviceId != null)
                .forEach(p -> {
//                    log.info("PLAYER: " + p.name + " ROOM: " + p.room + " ID: " + p.deviceId);
                    Device dev = SmartHome.devices.stream()
                            .filter(device -> device.room.equals(p.room))
                            .findFirst().orElse(null);
                    if (dev != null) {
//                        log.info("DEVICE ROOM:" + dev.room + " ID: " + dev.id);
                        if (!p.deviceId.equals(dev.id)) {
                            p.deviceId = dev.id;
                            log.info("FIX PLAYER: " + p.name + " ROOM: " + p.room + " ID: " + p.deviceId);
                        }
                    }
                });
    }


    public void afterAll() {
        log.info("\nAFTER ALL - SAVE LAST TIME");
        // сохранить состояние плееров - время и путь
        this.players.stream().filter(player -> player.connected).forEach(player -> player.saveLastTimePath());
        this.write();
// запрос на обновление виджетов таскера

        log.info("\nAFTER ALL - AUTOREMOTE REFRESH");
        this.autoremoteRequest();
//        обновить отображение в Яндекс
        this.updateLmsPlayers();
        Yandex.sendAllStates();

// для автотеста - все действия завершены

        log.info("\nAFTER ALL - HIVE TEST");
        hive.publish("test", "test");
    }
}