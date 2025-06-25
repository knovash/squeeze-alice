package squeezealicetest.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Requests;
import org.knovash.squeezealice.lms.PlayerStatus;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;
import static squeezealicetest.utils.MainTest.lmsPlayersTest;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTest {

    public String name;
    public String nameInQuery;
    public String room;
    public String deviceId;
    public String mac;
    public Integer volume_step;
    public Integer volume_low;
    public boolean playing;
    public Integer volume_high;
    public Integer delay;
    public boolean separate = false;
    public boolean connected = false;
    public Map<Integer, Integer> schedule;
    public String title;
    public String currentTrack;
    public String lastPath;
    public String lastPlayTime;
    public int lastChannel = 0; // для переключения канал. сохраняется в next и prev channel
    public static PlayerStatus playerStatus = new PlayerStatus();
    public String mode;
    public boolean sync;
    public String volume;
    public String lastTitle; // чтото непонятное выпилено из таскер
    private List<String> playlist = new ArrayList<>();
    public Integer currentChannel = 0;

    public PlayerTest(String name, String mac) {
        this.name = name;
        this.mac = mac;
        this.title = "непонятно";
        this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 25;
        this.delay = 10;
        this.schedule = new HashMap<>(Map.of(
                0, 10,
                7, 15,
                9, 20,
                20, 15,
                22, 10));
    }

// LMS получение данных ------------------------------------------------------------------------------------------------

    public static String count() {
        log.info("COUNT START");
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.count().toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("COUNT PLAYERS IN LMS: " + response.result._count);
        return response.result._count;
    }

    public static String name(String index) {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.name(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.id(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

    public String mode() {
        this.playing = false;
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "stop";
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public String path() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracks() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

    public String playlistName() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " PLAYLISTNAME: " + response.result._name);
        return response.result._name;
    }

    public String artistName() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " ARTISTNAME: " + response.result._artist);
        return response.result._artist;
    }

    public String volumeGet() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " GET VOLUME: " + response.result._volume);
        return response.result._volume;
    }

    public List<String> favorites() {
//        log.info("FAVORITES START " + this.name);
        String playerName = this.name;
        List<String> playlist = null;
        try {
            Response response = RequestsTest.postToLmsForResponse(RequestParameters.favorites("", 10).toString());
            playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("ERROR " + e);
        }

//        log.info("FAVORITES: " + playlist);
        return playlist;
    }

    public void favoritesAdd(String url, String title) {
        RequestsTest.postToLmsForResponse(RequestParameters.favoritesAdd(this.name, url, title).toString());
    }


// LMS короткие действия --------------------------------------------------------------------------------------------




    public PlayerTest playChannel(Integer channel) {
        this.currentChannel = channel;
        log.info("CHANNEL: " + channel + " PLAYER: " + this.name);
        RequestsTest.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        return this;
    }




}