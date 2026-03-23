package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatus;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.ActionsSync;
import org.knovash.squeezealice.volumio.VolumioPlayer;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.PagePlayers.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayers {

    public List<Player> players = new ArrayList<>();
    public String btPlayerName = "HomePod";
    public int delayUpdate = 5; // MINUTES
    public int delayExpire = 10; // MINUTES
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
    private String lastUpdateTime;

    public void checkUpdated() {
        if (lastUpdateTime == null) log.info("--- UPDATE EXPIRED ---- ERROR -----------");
        LocalTime lastTime = LocalTime.parse(this.lastUpdateTime).truncatedTo(SECONDS);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(SECONDS);
        long diff = lastTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        if (expired) log.info("--- UPDATE EXPIRED ---- ERROR -----------");
        else log.info("OK");

    }

    public void saveUpdsteTime() {// сохранить последнее время обновления
        this.lastUpdateTime = LocalTime.now(zoneId).truncatedTo(SECONDS).toString();
        log.info("LAST UPDATE TIME: " + this.lastUpdateTime);
        lmsPlayers.write();
    }


    public void updatePlayers() {
        log.info("UPDATE PLAYERS FROM LMS");
        if (lmsPlayers.players == null) lmsPlayers.players = new ArrayList<>();
        String json = Requests.postToLmsForJsonBody(RequestParameters.serverstatusname().toString());
        if (json == null) return;
        json = JsonUtils.replaceSpace(json);
        json = json.replaceAll("\"newversion.*</a>\\.\"", "\"newversion\": \"--\"");
        ServerStatus serverStatus = JsonUtils.jsonToPojo(json, ServerStatus.class);
        if (serverStatus == null) return;

        if (players != null) this.players.stream().forEach(p -> p.cleanPlayer()); // очистить все плеееры


        serverStatus.result.players_loop.stream()
                .filter(pl -> lmsPlayers.playerByName(pl.name) == null)
                .forEach(pl -> {
                    log.info("ADD NEW PLAYER " + pl.name);
                    lmsPlayers.players.add(new Player(pl.name));
                });

        serverStatus.result.players_loop.stream()
                .filter(pl -> lmsPlayers.playerByName(pl.name) != null)
                .forEach(pl -> lmsPlayers.playerByName(pl.name).update(pl)); // так сделано потому что для volumio свой update


        lmsPlayers.players.stream()
                .sorted(Comparator.comparing(p -> !p.connected))
                .forEach(p ->
                        log.info(String.format("" +
                                        "UPDATED PLAYER: %-14s" +
                                        "ROOM: %-10s " +
                                        "CONNECTED: %-6s " +
                                        "SEPARATED: %-6s " +
                                        "MODE: %-6s ",
                                p.name,
                                p.room,
                                p.connected,
                                p.separate,
                                p.mode
                        )));
        this.saveUpdsteTime();
    }

    public void write() {
        log.info("WRITE: " + config.fileLmsPlayers);
        JsonUtils.pojoToJsonFile(this, config.fileLmsPlayers);
    }

    public void read() {
        log.debug("READ: " + config.fileLmsPlayers);
        LmsPlayers lp = JsonUtils.jsonFileToPojo(config.fileLmsPlayers, LmsPlayers.class);
        if (lp != null) {
            this.players = lp.players;
            this.autoRemoteUrls = lp.autoRemoteUrls;
            this.toggleWake = lp.toggleWake;
            this.btPlayerName = lp.btPlayerName;
            this.delayUpdate = lp.delayUpdate;
            this.delayExpire = lp.delayExpire;
            this.lastThis = lp.lastThis;
        }
        log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(player -> player.name).collect(Collectors.toList()));
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
        List<Player> playingPlayers = playingPlayers(exceptName, exceptSeparated);
        if (playingPlayers == null || playingPlayers.isEmpty()) {
            return null;
        }
        return playingPlayers.stream()
                .sorted(Comparator.comparing(Player::getLastPlayTimePlayer,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .findFirst()
                .orElse(null);
    }

    public Player lastPlayedPlayer(List<Player> players) {
        Player ssss = null;
        if (players == null || players.isEmpty()) {
            ssss = lmsPlayers.players.stream()
                    .filter(player -> !player.connected)
                    .sorted(Comparator.comparing(Player::getLastPlayTimePlayer,
                            Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .findFirst()
                    .orElse(null);
        } else {
            ssss = players.stream()
                    .filter(player -> !player.connected)
                    .sorted(Comparator.comparing(Player::getLastPlayTimePlayer,
                            Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .findFirst()
                    .orElse(null);
        }
        if (ssss != null) log.info("LAST PLAYED " + ssss.name + " " + ssss.lastPlayTimePlayer);
        else log.info("LAST PLAYED NULL ERROR");
        return ssss;

    }

    public List<Player> playingPlayers(String exceptName, boolean exceptSeparated) {
        log.info(" ------- ПРОВЕРЯТЬ ЧТО СОСТОЯНИЕ ПЛЕЕРОВ ОБНОВЛЕНО !!! -----");

        this.checkUpdated();

//        lmsPlayers.fastUpdateServer(); // тут надо потому что иногда вызывается после unsync all
        List<Player> playingPlayers = this.players.stream()
                .filter(p -> !exceptSeparated || !p.separate) // исключить отдельные
                .filter(p -> p.playing) // выбрать играющие
                .filter(p -> !exceptName.equals(p.name)) // кроме этого
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

        String id = null;

        log.info("PLAYER REMOVE: " + player);
        this.players.remove(player);
        if (id != null) smartHome.devices.remove(smartHome.deviceByExternalId(id));
//        Device device = SmartHome.getDeviceById(id);
        write();
        return "OK";
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
        this.updatePlayers(); // после сохранения ip lms обновить плееры
    }

    public void volumioSave(HashMap<String, String> parameters) {
        log.info("PARAMETERS: " + parameters);
        String tmp1 = parameters.get(volumio_ip_value);
        log.info(tmp1);
        if (tmp1 == null) return;
        config.volumioIp = tmp1;
        log.info(config);
        config.write();
    }

    public void turnOffMusicAll() { // для Таскер только
        log.info("\nSTOP ALL");

        lmsPlayers.checkUpdated(); // TODO DEBUG
        this.players.parallelStream()
                .filter(player -> player.connected)
                .peek(player -> log.info(player.name))
                .forEach(player -> player.turnOffMusic());
    }

//    public void autoremoteRequest() {
//        log.info("REQUEST TASKER AUTOREMOTE REFRESH");
//        log.info("URLS SIZE: {}", this.autoRemoteUrls.size());
//        this.autoRemoteUrls.forEach(url -> {
//            log.info("POST TO AUTOREMOTE: {}", url);
//            try {
//                HttpResponse response = Request.Post(url)
//                        .connectTimeout(5000)
//                        .socketTimeout(5000)
//                        .execute()
//                        .returnResponse();
//                int statusCode = response.getStatusLine().getStatusCode();
//
//                log.error("POST. Status: {}, URL: {}", statusCode, url);
//
//            } catch (Exception e) {
//                log.error("POST ERROR. URL: " + url, e);
//            }
//        });
//    }

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

    public void afterAll() {
        log.info("AFTER ALL");
// сохранить состояние плееров - время и путь
        lmsPlayers.checkUpdated(); // TODO DEBUG
        this.players.stream().filter(player -> player.connected).forEach(player -> player.saveLastTimePath());
// запрос на обновление виджетов таскера
//        this.autoremoteRequest();
// обновить отображение в Яндекс
//        this.fastUpdateServer();
        Tasker.ready = "yes";
        log.info("TASKER READY: " + Tasker.ready);
//        Yandex.sendAllStates();

    }

    public void logPlayersNames() {
        log.info("LMS PLAYERS: " + lmsPlayers.players.stream().filter(Objects::nonNull).map(player -> player.name).collect(Collectors.toList()));
    }

}