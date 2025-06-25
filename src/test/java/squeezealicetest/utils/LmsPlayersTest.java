package squeezealicetest.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatusByName;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static squeezealicetest.utils.MainTest.lmsPlayersTest;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsPlayersTest {

    public List<PlayerTest> players;
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

    public static String widgetsNames;
    public static String widgetsModes;
    public static String widgetsSyncs;
    public static String widgetsSeparates;
//    public static String widgetPlayersTitles;
//    public static String joinedPlayersVolModTit;

    public static String widgetPlayersPlay;
    public static String widgetPlayersStop;

    public List<String> favorites() {
        log.info("START");
        String playerName = lmsPlayersTest.players.get(0).name;
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.favorites("", 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        return playlist;
    }

    public void write() {
        log.info("WRITE FILE: " + LmsPlayersTest.saveToFileJson);
        JsonUtils.pojoToJsonFile(lmsPlayersTest, LmsPlayersTest.saveToFileJson);
    }

    public void read() {
        log.info("READ FILE: " + LmsPlayersTest.saveToFileJson);
        lmsPlayersTest.players = new ArrayList<>();
//        LmsPlayersTest lp = JsonUtils.jsonFileToPojo("/data/lms_players.json", LmsPlayersTest.class);
//        LmsPlayersTest lp = JsonUtils.jsonFileToPojo("/data/lms_players.json", LmsPlayersTest.class);
        LmsPlayersTest lp = JsonUtils.jsonFileToPojoClass("/data/lms_players.json", LmsPlayersTest.class);
        log.info("PLAYERS: " + lp);
        if (lp == null) {
            log.info("NO PLAYERS lms_players.json");
        } else {
            lmsPlayersTest = lp;
        }
        log.info("PLAYERS FROM lms_players.json: " + lmsPlayersTest.players.stream().map(p -> p.name).collect(Collectors.toList()));
    }

    public PlayerTest playerByCorrectName(String name) {
//        log.debug("BY NAME: " + name);
        PlayerTest player = new PlayerTest();
        if (name == null) return null;
        if (lmsPlayersTest.players == null) return null;
        player = lmsPlayersTest.players.stream()
                .filter(p -> (p.getName() != null))
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
        if (player == null) log.info("PLAYER NOT FOUND BY NAME: " + name);
//        else log.info("BY NAME: " + name + " GET PLAYER: " + player);
        return player;
    }

    public PlayerTest playerByNearestName(String player) {
        log.info("START: " + player);
        if (player == null) return null;
        List<String> players = lmsPlayersTest.players.stream().map(p -> p.name).collect(Collectors.toList());
        player = Utils.convertCyrilic(player);
//        String correctPlayerName = Levenstein.getNearestElementInList(player, players);
        String correctPlayerName = Levenstein.getNearestElementInListWord(player, players);
//        String correctPlayerName = Levenstein.search(player, players);
        if (correctPlayerName == null) log.info("ERROR PLAYER NOT EXISTS IN LMS ");
        log.info("CORRECT PLAYER: " + player + " -> " + correctPlayerName);
        PlayerTest correctPlayer = lmsPlayersTest.playerByCorrectName(correctPlayerName);
        return correctPlayer;
    }

    public PlayerTest playerByCorrectRoom(String room) {
//        log.info("ROOM: " + room);
        PlayerTest player = new PlayerTest();
        Optional<PlayerTest> optionalPlayer = lmsPlayersTest.players.stream()
                .filter(p -> (p.room != null))
//                .peek(p -> log.info("0: " + p.name + " " + p.room + " = " + room + " " + p.room.equals(room)))
                .filter(p -> p.room.equals(room))
                .filter(Objects::nonNull)
                .findFirst();
        player = (PlayerTest) optionalPlayer.orElse(null);
        if (player != null) log.info("ROOM: " + room + " PLAYER: " + player.name);
        if (player == null) log.debug("PLAYER NOT FOUND WHITH ROOM " + room);
        return player;
    }

    public PlayerTest playerByNearestRoom(String room) {
        log.info("GET PLAYER BY NEAREST ROOM: " + room);
        if (room == null) return null;
        room = Utils.getCorrectRoomName(room);
        PlayerTest player = lmsPlayersTest.playerByCorrectRoom(room);
        return player;
    }


    public PlayerTest playerByDeviceId(String id) {
        if (id == null) {
            log.info("ERROR NO PLAYER BY ID: " + id);
            return null;
        }
        PlayerTest player = lmsPlayersTest.players.stream()
//                .peek(p -> log.info("PLAYER ID: " + p.deviceId + " ROOM: " + p.room))
                .filter(p -> p.room != null)
                .filter(p -> p.deviceId != null)
                .filter(p -> p.deviceId.equals(id))
                .findFirst().orElse(null);
//        log.info("ID: " + id + " PLAYER BY ID: " + player);
        if (player != null) log.info("BY ID: " + id + " PLAYER: " + player.name);
        else log.info("ERROR PLAYER NULL");
        return player;
    }

    public String playerNameByDeviceId(String id) {
        PlayerTest player = playerByDeviceId(id);
        if (player == null) return null;
        return player.name;
    }

    public List<List<String>> syncgroups() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.syncgroups().toString());
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