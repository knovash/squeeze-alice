package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatus;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.ActionsSync;

import java.util.*;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.PagePlayers.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayers {

    public List<Player> players;
    public String lastPathCommon;
    public int lastChannelCommon = 1;
//    public String btPlayerInQuery = "homepod";
    public String btPlayerName = "HomePod";
//    public String tvPlayerInQuery = "homepod1";
//    public String tvPlayerName = "HomePod1";
    public int delayUpdate = 5; // MINUTES
    public int delayExpire = 10; // MINUTES
//    public boolean syncAlt = false;
    public boolean lastThis = true;
    public static ServerStatus serverStatus = new ServerStatus();
    public List<String> autoRemoteUrls = new ArrayList<>();
    public Boolean toggleWake = true; // TODO еще не используется
    public Map<Integer, Integer> scheduleAll = new HashMap<>(Map.of(
            0, 10,
            7, 15,
            9, 20,
            20, 15,
            22, 5));

    public List<String> favorites() {
        log.info("GET FAVORITES LIST");
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites("", "100").toString());
        List<String> favList = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return favList;
    }

    public void fastUpdateServer() {
        String json = Requests.postToLmsForJsonBody(RequestParameters.serverstatusname().toString());
        if (json == null) return;
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
        ServerStatus serverStatus = JsonUtils.jsonToPojo(json, ServerStatus.class);
        if (serverStatus == null) return;
        serverStatus.result.players_loop.forEach(this::fastUpdatePlayer);
    }

    private void fastUpdatePlayer(ServerStatus.PlayersLoop p) {
        Player player = lmsPlayers.playerByName(p.name);
        if (player == null) {
            player = new Player(p.name);
            log.info("ADD NEW PLAYER: " + player.name);
            this.players.add(player);
        }
        player.connected = false;
        player.mode = "stop";
        player.playing = false;
        if (p.isplaying == 1) {
            player.mode = "play";
            player.playing = true;
        }
        if (p.connected == 1) player.connected = true;

        log.info(String.format("" +
                        "PLAYER: %-12s" +
                        "CONNECTED: %-6s " +
                        "MODE: %-6s ",
                player.name,
                player.connected,
                player.mode
        ));
    }

    public void write() {
        log.info("WRITE " + config.fileLmsPlayers);
        JsonUtils.pojoToJsonFile(this, config.fileLmsPlayers);
    }

    public void read() {
        log.debug("READ FILE: " + config.fileLmsPlayers);
        LmsPlayers lp = JsonUtils.jsonFileToPojo(config.fileLmsPlayers, LmsPlayers.class);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
            return;
        }
        // Сохраняем все поля из прочитанного объекта
        this.players = lp.players;
        this.autoRemoteUrls = lp.autoRemoteUrls; // <-- Теперь это поле тоже загружается
        this.toggleWake = lp.toggleWake;
        this.lastPathCommon = lp.lastPathCommon;
        this.lastChannelCommon = lp.lastChannelCommon;
        this.btPlayerName = lp.btPlayerName;
        this.delayUpdate = lp.delayUpdate;
        this.delayExpire = lp.delayExpire;
//        this.syncAlt = lp.syncAlt;
        this.lastThis = lp.lastThis;
        log.info("PLAYERS SETTINGS lms_players.json: " + this.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public Player playerByName(String name) {
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
        Player correctPlayer = this.playerByName(correctPlayerName);
        return correctPlayer;
    }

    public Player playerByRoom(String room) {
        if (room == null) return null;
        return this.players.stream()
                .filter(Objects::nonNull)
                .filter(p -> room.equals(p.room))
                .findFirst()
                .orElse(null);
    }

    public Player playerByNearestRoom(String room) {
        if (room == null) return null;
        room = Utils.getCorrectRoomName(room);
        return this.playerByRoom(room);
    }

    public Player playingPlayer(String exceptName, boolean exceptSeparated) {
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated); //playingPlayer
        if (playingPlayers != null) return playingPlayers.get(0);
        return null;
    }

    public List<Player> playingPlayers(String exceptName, boolean exceptSeparated) {
        lmsPlayers.fastUpdateServer(); // TODO удалить
        List<Player> playingPlayers = this.players.stream()
                .filter(p -> !exceptSeparated || !p.separate)
                .filter(p -> p.playing)
                .filter(p -> !exceptName.equals(p.name))
//                .filter(p -> { // TODO вернуть если ошибки с плеерами которые играют тишину
//                    log.info("check path not silence");
//                    String path = p.path();
//                    return path != null && !path.equals(config.silence);
//                })
                .collect(Collectors.toList());
        if (playingPlayers == null || playingPlayers.isEmpty()) {
            log.info("NO PLAYING PLAYERS. EXCEPT NAME: " + exceptName + ". EXCEPT SEPARATED: " + exceptSeparated);
            return null;
        }
        log.info("SEARCH FOR PLAYING. EXCEPT NAME: " + exceptName + ". EXCEPT SEPARATED: " + exceptSeparated + " PLAYING PLAYERS: " + playingPlayers.stream().map(player -> player.name).collect(Collectors.toList()));
        return playingPlayers;
    }

    public List<String> playingPlayersNames(String exceptName, Boolean exceptSeparated) {
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated);
        if (playingPlayers == null || playingPlayers.isEmpty()) return Collections.emptyList();
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
        Player player = this.playerByName(playerName);
        player.delay = Integer.valueOf(delay);
        player.volume_high = Integer.valueOf(volumeMax);
        player.schedule = Utils.stringSplitToIntMap2(schedule, ",", ":");
//        SwitchVoiceCommand.room = roomName;
        Player playerNew = ActionsSync.selectPlayerInRoom(playerName, roomName, false); // playerSave
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

        Player player = this.playerByName(playerName);
        log.info("PLAYER DEVICE ID: " + player.deviceId);
        String id = null;
        if (player.deviceId != null) id = player.deviceId;
        log.info("PLAYER REMOVE: " + player);
        this.players.remove(player);
        if (id != null) smartHome.devices.remove(smartHome.deviceById(id));
//        Device device = SmartHome.getDeviceById(id);
        write();
        return "OK";
    }

    public Player playerByDeviceId(String extIdPlayerName) {
        if (extIdPlayerName == null) {
            log.info("ERROR NULL ID: " + extIdPlayerName);
            return null;
        }
        Player player = this.players.stream()
                .filter(p -> p.room != null)
                .filter(p -> p.deviceId != null)
                .filter(p -> p.deviceId.equals(extIdPlayerName))
                .findFirst().orElse(null);
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

//    public void altSyncSave(HashMap<String, String> parameters) {
//        String tmp = parameters.get(alt_sync_value);
//        if (tmp == null) return;
//        syncAlt = Boolean.parseBoolean(tmp);
//        log.info("ALT SYNC SAVE syncAlt: " + syncAlt);
//        write();
//    }

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
        this.fastUpdateServer(); // после сохранения ip lms обновить плееры
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
        syncMemberNames.forEach(collection -> result.addAll(collection));
        log.info("SYNCGROUPS: " + syncMemberNames);
        return syncMemberNames;
    }

    public void searchForLmsIp() {
        if (Utils.checkIpIsLms(config.lmsIp)) {
            lmsServerOnline = true;
            return;
        }
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


    public void checkRooms() {
        log.info("CHECK ROOMS");
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
// сохранить состояние плееров - время и путь
        this.players.stream().filter(player -> player.connected).forEach(player -> player.saveLastTimePath());
// запрос на обновление виджетов таскера
//        this.autoremoteRequest();
// обновить отображение в Яндекс
//        this.fastUpdateServer();
//        Yandex.sendAllStates();

    }

}