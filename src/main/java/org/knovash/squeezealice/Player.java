package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.PlayerStatus;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.JsonUtils;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

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
    public String lastPath;
    public String lastPlayTime;
    public int lastChannel = 0;
    public static PlayerStatus playerStatus = new PlayerStatus();
    public String mode;
    public boolean sync;
    public String volume;
    public String lastTitle;

    public Player(String name, String mac) {
        this.name = name;
        this.mac = mac;
        this.title = "херпоймичё";
        this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 25;
        this.delay = 10000;
        this.schedule = new HashMap<>(Map.of(
                0, 10,
                7, 15,
                9, 20,
                20, 15,
                22, 10));
    }

    public static String name(String index) {
        Response response = Requests.postToLmsForResponse(RequestParameters.name(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response = Requests.postToLmsForResponse(RequestParameters.id(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

    public String mode() {
        this.playing = false;
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "stop";
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracks() {
        Response response = Requests.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

    public String playlistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " PLAYLISTNAME: " + response.result._name);
        return response.result._name;
    }

    public String playlistUrl() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlisturl(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info(response.result.toString());
        log.info("PLAYER: " + this.name + " PLAYLISTURL: " + response.result._url);
        return response.result._url;
    }

    public String albumName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.albumname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " ALBUMNAME: " + response.result._album);
        return response.result._album;
    }

    public String trackname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.trackname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
        log.info("PLAYER: " + this.name + " TRACKNAME: " + response.result._title);
        return response.result._title;
    }

    public String artistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " ARTISTNAME: " + response.result._artist);
        return response.result._artist;
    }

    public String volumeGet() {
        Response response = Requests.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " GET VOLUME: " + response.result._volume);
        return response.result._volume;
    }

    public Player volumeSet(String value) {
        String status = null;
        log.info("PLAYER: " + this.name + " SET VOLUME: " + value);
        status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name);
            return this;
        }

        this.status(null);
        if (playerStatus.result.mixer_volume < 1) {
            status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, "1").toString());
            if (status == null || !status.contains("200")) {
                log.info("REQUEST ERROR " + this.name);
                return this;
            }
        }

        if (!playerStatus.result.mode.equals("play")) {
            if (!value.contains("+") && !value.contains("-")) {
                this.lastPlayTime = null;
                return this;
            }
            if (value.contains("+")) value = value.replace("+", "-");
            else value = value.replace("-", "+");
            Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        }
        return this;
    }

    public String volumeRelativeOrAbsolute(String value, Boolean relative) {
        log.info("PLAYER: " + this.name + " VOLUME: " + value + " RELATIVE: " + relative);
//        if (Spotify.ifPlaying()) {
//            Spotify.volumeGeneral(value, relative);
//            return;
//        }
        if (relative != null && relative.equals(true)) {
            if (value.contains("-")) {
                this.volumeSet(value);
            } else {
                this.volumeSet("+" + value);
            }
        }
        if (relative != null && relative.equals(false)) {
            this.volumeSet(value);
        }
        log.info("VOLUME " + this.volume);
        return this.name + "-" + this.volume;
    }

    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        Spotify.active = false;
        Requests.postToLmsForStatus(RequestParameters.play(this.name).toString());
        return this;
    }

    public Player turnOnMusic() {
        Spotify.active = false;

        log.info("TURN ON MUSIC PLAYER: " + this.name);
//        CompletableFuture.runAsync(() -> {
        this.ifExpiredOrNotPlayUnsyncWakeSet();
        if (this.separate) {
            log.info("PLAYER IS SEPARATED - PLAY LAST");
            this.playLast();
            lmsPlayers.write();
            return this;
        }
        Player playing = lmsPlayers.getPlayingPlayer(this.name);
        if (playing == null) {
            log.info("NO PLAYING. PLAY LAST");
            this.playLast();
//                player.syncAllOtherPlayingToThis();
        } else {
            log.info("SYNC TO PLAYING " + playing);
            this.syncTo(playing.name); //.saveLastPath().saveLastTime();
        }
        lmsPlayers.write();
//        });
        Requests.autoRemoteRefresh();
        return this;
    }

    public Player turnOffMusic() {
        Spotify.active = false;
        Actions.turnOffMusic(this);
        return this;
    }

    public Player stop() {
        log.info("PLAYER: " + this.name + " STOP");
        Spotify.active = false;
        this
                .unsync()
                .pause()
                .saveLastTime()
                .saveLastPath();
        return this;
    }

    public Player pause() {
        Spotify.active = false;
        log.info("PLAYER: " + this.name + " PAUSE");
        Requests.postToLmsForStatus(RequestParameters.pause(this.name).toString());
        return this;
    }

    public Player togglePlayPause() {
        log.info("PLAYER: " + this.name + " PLAY/PAUSE");
        Requests.postToLmsForStatus(RequestParameters.togglePlayPause(this.name).toString());
        return this;
    }

    public Player playChannel(Integer channel) {
        log.info("CHANNEL: " + channel + " PLAYER: " + this.name);
        Spotify.active = false;
        String status = Requests.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name);
            return this;
        }
        this.saveLastTime().saveLastChannel(channel).saveLastPath();
        lmsPlayers.write();
        return this;
    }

    public String playChannelRelativeOrAbsolute(String value, Boolean relative) {
//        log.info("CANNEL: " + value + " RELATIVE: " + relative);
        int channel;
        if (relative != null && relative.equals(true)) {
            if (this.lastChannel != 0) channel = this.lastChannel + 1;
            else channel = lmsPlayers.lastChannel + 1;
        } else {
            channel = Integer.parseInt(value);
        }
        log.info("PLAYER: " + this.name + " CHANNEL: " + channel + " RELATIVE: " + relative);
        this.ifExpiredOrNotPlayUnsyncWakeSet();
        Player playing = lmsPlayers.getPlayingPlayer(this.name);
        if (playing != null) this.syncTo(playing.name);
        this.playChannel(Integer.valueOf(channel));
        this.waitFor(2000);
        lmsPlayers.lastChannel = channel;
        this.syncAllOtherPlayingToThis();

        Requests.autoRemoteRefresh();
        return "PLAY CHANNEL " + channel;
    }


    public Player playPath(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        Spotify.active = false;
        Requests.postToLmsForStatus(RequestParameters.play(this.name, path).toString());
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        Requests.postToLmsForStatus(RequestParameters.play(this.name, config.silence).toString());
        return this;
    }

    public Player prevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        if (!this.tracks().equals("1"))
            Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public Player nextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        if (!this.tracks().equals("1"))
            Requests.postToLmsForStatus(RequestParameters.nexttrack(this.name).toString());
        return this;
    }

    public Player playTrackNumber(String track) {
        log.info("PLAYER: " + this.name + " TRACK NUMBER " + track);
        Requests.postToLmsForStatus(RequestParameters.track(this.name, track).toString());
        return this;
    }

    public Player prevChannel() {
        int channel = 1;
        List<String> favList = this.favorites();
        int favSize = favList.size();
        String playlistName = this.playlistName();
        log.info("CURRENT PLAYLIST NAME " + playlistName + " CONTAINS IN FAVORIRES " + favList.contains(playlistName));
        Integer currentIndex = 0;
        Integer nextIndex = 0;
        if (favList.contains(playlistName)) {
            currentIndex = favList.indexOf(playlistName) + 1;
            nextIndex = currentIndex;
            log.info("CURRENT INDEX IN FAVORITES " + currentIndex + " PLAY NEXT " + nextIndex);
            this.lastChannel = currentIndex;
        }

        if (this.lastChannel != 0) channel = this.lastChannel - 1;
        else channel = lmsPlayers.lastChannel - 1;
        if (channel < 1) channel = favSize;
        log.info("PLAY PREV CHANNEL: " + channel + " LAST CHANNEL: " + lmsPlayers.lastChannel);
        this.ifExpiredOrNotPlayUnsyncWakeSet();
        this.playChannel(channel);

        Requests.autoRemoteRefresh();
        return this;
    }

    public Player nextChannel() {
        log.info("LAST CHANNEL THIS: " + this.lastChannel + " COMMON: " + lmsPlayers.lastChannel);
        int channel = 1;
        List<String> favList = this.favorites();
        int favSize = favList.size();
        String playlistName = this.playlistName();
        log.info("CURRENT PLAYLIST NAME " + playlistName + " CONTAINS IN FAVORIRES " + favList.contains(playlistName));
        Integer currentIndex = 0;
        Integer nextIndex = 0;
        if (favList.contains(playlistName)) {
            currentIndex = favList.indexOf(playlistName) + 1;
            nextIndex = currentIndex + 2;
            log.info("CURRENT INDEX IN FAVORITES " + currentIndex + " PLAY NEXT " + nextIndex);
            this.lastChannel = currentIndex;
        }

        if (this.lastChannel != 0) {
            channel = this.lastChannel + 1;
            log.info("CHANNEL THIS+1: " + channel);
        } else {
            channel = lmsPlayers.lastChannel + 1;
            log.info("CHANNEL COMMON+1: " + channel);
        }
        if (channel > favSize) channel = 1;
        log.info("PLAY CHANNEL: " + channel);
        this.ifExpiredOrNotPlayUnsyncWakeSet();
        this.playChannel(channel);

        Requests.autoRemoteRefresh();
        return this;
    }

    public Player next() {
        log.info("PLAYER: " + this.name + " NEXT");
        int tracks = Integer.parseInt(this.tracks());
        log.info("TRACKS IN PLAYLIST: " + tracks);
        if (tracks < 2) this.nextChannel();
        else this.nextTrack();
        return this;
    }

    public Player prev() {
        log.info("PLAYER: " + this.name + " PREV");
        int tracks = Integer.parseInt(this.tracks());
        log.info("TRACKS IN PLAYLIST: " + tracks);
        if (tracks < 2) this.prevChannel();
        else this.prevTrack();
        return this;
    }

    public List<String> favorites() {
        String playerName = this.name;
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites(playerName, 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        log.info("FAVORITES: " + playlist);
        return playlist;
    }

    public String favoritesAdd() {
        log.info("START ADD ON " + this.name);
        this.status();
        String playerName = this.name;
        String url = this.path();
        String title = this.title;
        log.info("NOW PATH: " + url);
        if (url.contains("spotify")) {
            url = Spotify.lastPath;
            title = Spotify.lastTitle;
            log.info("SPOTIFY LAST: " + url);
        }
        int size = this.favorites().size() + 1;
        log.info("FAVORITES BEFORE SIZE: " + size);
        title = size + " " + title;
        Requests.postToLmsForResponse(RequestParameters.favoritesAdd(playerName, url, title).toString());
        log.info("FAVORITES ADD");
        return title;
    }

    public Player shuffleOn() {
        log.info("PLAYER: " + this.name + " SHUFFLE ON");
        Requests.postToLmsForStatus(RequestParameters.shuffleon(this.name).toString());
        return this;
    }

    public Player shuffleOff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        Requests.postToLmsForStatus(RequestParameters.shuffleoff(this.name).toString());
        return this;
    }

    public Player syncTo(String toPlayerName) {
        log.info("START SYNC " + this.name + " TO " + toPlayerName);
        if (lmsPlayers.syncAlt) {
            String path = lmsPlayers.getPlayerByCorrectName(toPlayerName).path();
            if (path.contains("di.fm") || path.contains("audioaddict")) {
                log.info("SYNC DI.FM " + this.name + " PLAY PATH FROM " + toPlayerName);
                this.playPath(path);
                return this;
            } else {
                log.info("SYNC " + this.name + " TO " + toPlayerName);
                Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
                this.saveLastPath().saveLastTime();
            }
        } else {
            log.info("SYNC " + this.name + " TO " + toPlayerName);
            Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
            this.saveLastPath().saveLastTime();
        }
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name);
        Requests.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        return this;
    }

    public Boolean ifExpiredOrNotPlayUnsyncWakeSet() {
        log.info("CHECK PLAYER IF NOT PLAY OR PLAY TOO LONG");
        if (!this.status(0)) {
            log.info("ERROR STATUS NULL !!!");
            return false;
        }
        if (!this.playing && this.ifTimeExpired(null)) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET");
            this.unsync().wakeAndSet();
            return true;
        }
        if (this.playing && this.ifTimeExpired(60)) {
            log.info("PLAYER " + this.name + " PLAY TOO LONG - UNSYNC, WAKE, SET");
            this.unsync().wakeAndSet();
            return true;
        }
        log.info("SKIP WAKE");
        return true;
    }

    public Player wakeAndSet() {
        log.info("");
        log.info("WAKE START >>>");
        log.info("PLAYER: " + this.name + " WAIT: " + this.delay);
        Yandex.runScenario("музыка подождите");
        this
                .playSilence()
                .volumeSet("+1")
                .setVolumeByTime()
                .waitForWake()
                .volumeSet("-1")
                .setVolumeByTime()
                .pause();
        log.info("WAKE FINISH <<<");
        log.info("");
        return this;
    }

    public Player stopAllOther() {
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name))
                .forEach(player -> player.unsync().pause());
        return this;
    }

    public Player playLast() {
        log.info("PLAY LAST");
        String thisPath = this.path();
        String thisLastPath = this.lastPath;
        String commonLastPath = lmsPlayers.lastPath;
        String lastPath;
//        log.info("THIS PATH: " + thisPath);
//        log.info("THIS LAST PATH: " + thisLastPath);
//        log.info("TRY GLOBAL LAST PATH: " + commonLastPath);
//        log.info("SILENCE PATH: " + silence);
//        log.info("LAST THIS STATE: " + lmsPlayers.lastThis);
        if (lmsPlayers.lastThis) lastPath = thisLastPath;
        else lastPath = commonLastPath;

        if (thisPath != null && !thisPath.equals(config.silence) && !thisPath.equals("")) {
            log.info("PUSH PLAY BUTTON: " + thisPath);
            this.play().saveLastPath().saveLastTime();
            return this;
        }
        if (commonLastPath != null && !commonLastPath.equals(config.silence) && !commonLastPath.equals("")) {
            log.info("PLAY COMMON LAST PATH: " + commonLastPath);
            this.playPath(lastPath).saveLastPath().saveLastTime();
            return this;
        }
        log.info("PLAY CHANNEL 1");
        this.playChannel(1);
        return this;
    }

    public Player setVolumeByTime() {
        LocalTime timeNow = LocalTime.now(zoneId);
//        log.info("VOLUME BY TIME: " + timeNow + " OF: " + this.schedule);
        Map.Entry<Integer, Integer> e =
                schedule.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("TIME: " + timeNow.truncatedTo(MINUTES) + "VOLUME: " + e.getValue() + " BY TIME: " + e.getKey() + " OF: " + this.schedule);
        this.volumeSet(String.valueOf(e.getValue()));
        return this;
    }

    public Player waitForWake() {
        log.info("WAIT " + delay + " . . . . .");
        try {
            Thread.sleep(this.delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Player waitFor(int msec) {
        log.info("WAIT " + msec + " START");
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("WAIT " + msec + " FINISH");
        return this;
    }

    public Player saveLastTime() {
        this.lastPlayTime = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
        log.debug("SAVE LAST TIME: " + this.lastPlayTime);
        return this;
    }

    public Player saveLastPath() {
        String path = this.path();
        if (path != null && !path.equals(config.silence)) {
            this.lastPath = path;
            lmsPlayers.lastPath = this.lastPath;
            log.debug("SAVE LAST PATH: " + this.lastPath);
        }
        this.status();
        this.lastTitle = this.title;
        lmsPlayers.lastTitle = this.title;
        return this;
    }

    public Player saveLastChannel(int channel) {
        log.debug("SAVE LAST CHANNEL: " + channel);
        lmsPlayers.lastChannel = channel;
        this.lastChannel = channel;
        this.status();
        this.lastTitle = this.title;
        lmsPlayers.lastTitle = this.title;
        return this;
    }

    public void remove() {
        lmsPlayers.players.remove(this);
    }

    public Player separateFlagTrue() {
        this.separate = true;
        log.info("SEPARTE FLAG: " + this.name + " " + this.separate);
        return this;
    }

    public Player separateFlagFalse() {
        this.separate = false;
        log.info("SEPARTE FLAG: " + this.name + " " + this.separate);
        return this;
    }


    public Player separateOn() {
        log.info("SEPARATE ON");
        this.separateFlagTrue().unsync().turnOnMusic();
        lmsPlayers.write();
        return this;
    }

    public Player separateOff() {
        log.info("SEPARATE OFF");
        if (this.separate) {
            log.info("SEPARATE FLAG OFF AND TURN ON THIS " + this.name);
            this.separateFlagFalse().turnOnMusic().syncAllOtherPlayingToThis();
        } else {
            log.info("SEPARATE FLAG OFF AND TURN ON ALL OTHER");
            lmsPlayers.updateServerStatus();
            lmsPlayers.players
                    .stream().map(p -> p.separateFlagFalse())
                    .filter(p -> !p.name.equals(this.name))
                    .filter(p -> p.playing)
                    .forEach(p -> CompletableFuture.runAsync(() -> p.turnOnMusic()));
//                    .forEach(p -> {
//                p.separateFlagFalse();
//                log.info(p.name + " MODE: " + p.playing + " SEPARATE: " + p.separate);
//                if (p.playing && !p.name.equals(this.name)) CompletableFuture.runAsync(() -> p.turnOnMusic());
//            });
        }
        log.info("FINISH");
        lmsPlayers.write();
        return this;
    }

    public Boolean status() {
        return status(1);
    }

    public Boolean status(Integer tracks) {
//        log.info("STATUS PLAYER " + this.name);
        if (tracks == null) tracks = 0;
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(this.name, tracks).toString());
        if (json == null) {
            log.info("REQUEST ERROR request null");
            return null;
        }
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null) {
            log.info("REQUEST ERROR json invalid");
            return false;
        }
        this.title();
        this.volume = String.valueOf(playerStatus.result.mixer_volume);
        if (playerStatus.result.mode.equals("play")) {
            this.playing = true;
            this.mode = "play";
        } else {
            this.playing = false;
            this.mode = "stop";
        }
        log.info("STATUS PLAYER: " + this.name +
                " MODE: " + this.mode +
                " " + this.playing +
                " VOLUME: " + this.volume +
                " SYNC: " + this.sync +
                " CONNECTED: " + this.connected +
                " TITLE: " + this.title);
        return true;
    }

    public void title() {
//  запрашивать "playlist", "name" для Soma и Di. для Spoty нету. запросить "artist"
//        log.info("PLAYER: " + this.name + " TITLE: " + this.title);
        String title = this.playerStatus.result.current_title;
//        log.info("CURRENT TITLE: " + this.title);
        if ((title != null) && (title != "")) {
            if (title.contains(": ")) title = title.replaceAll(":.*", "");
            if (title.contains(" - ")) title = title.replaceAll(" - .*", "");
        }
        if ((title == null) || (title == "")) title = this.artistName();
        if (title == null) title = "херпоймичё";
        if (title.length() > 20) title = title.substring(1, 20);
        title = title.replaceAll(",", " ");
        title = title.replaceAll(":", " ");
        title = title.replaceAll("  ", " ");
        this.title = title;
//        log.info("TITLE: " + this.title);
    }

    public Player syncAllOtherPlayingToThis() {
        if (!lmsPlayers.syncAlt) {
            log.info("ALT SYNC OFF");
            return this;
        }
        if (this.separate) {
            log.info("PLAYER IS SEPARETE. STOP ALT SYNC");
            return this;
        }
        log.info("ALT SYNC ALL PLAYING TO " + this.name + " ALT SYNC: " + lmsPlayers.syncAlt);
        log.info("SEARCH PLAYING PLAYERS...");
        List<Player> playingPlayers = lmsPlayers.getPlayingPlayers(this.name);
        log.info("PLAYING PLAYERS: " + playingPlayers);
        if (playingPlayers == null) return this;
        log.info("UNSYNC ALL PLAYING PLAYERS...");
        playingPlayers.forEach(p -> p.unsync());
        log.info("SYNC ALL NOW PLAYING PLAYERS");
        playingPlayers.forEach(p -> p.syncTo(this.name));
        log.info("FINISH <<<");
        return this;
    }

    public boolean ifTimeExpired(Integer delayMinutes) {
        long delayExpire = lmsPlayers.delayExpire;
        if (delayMinutes != null) delayExpire = delayMinutes;
        if (this.lastPlayTime == null) return true;
        LocalTime playerTime = LocalTime.parse(this.lastPlayTime).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(MINUTES);
        long diff = playerTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        log.info("EXPIRED: " + expired + " DIFF:" + diff + " DELAY:" + delayExpire);
        return expired;
    }

    public Player switchToHere() {
        log.info("TRY TRANSFER IF SPOTIFY PLAY");
        if (Spotify.transfer(this)) return this;
        if (this.mode().equals("play")) {
            log.info("PLAYER PLAYING. STOP ALL OTHER");
            this.stopAllOther();
            return this;
        }
        log.info("PLAYER NOT PLAY. SYNC TO PLAYING OR PLAY LAST. STOP ALL OTHER");
        this.turnOnMusic().stopAllOther();
        return this;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", room='" + room + '\'' +
                ", mode='" + mode + '\'' +
                ", sync='" + sync + '\'' +
                ", deviceid='" + deviceId + '\'' +
                ", title='" + title + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(getName(), player.getName()) && Objects.equals(getMac(), player.getMac());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMac());
    }
}