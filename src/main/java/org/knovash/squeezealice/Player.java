package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.PlayerStatus;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.utils.JsonUtils;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.silence;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    public String name;
    public String nameInQuery;
    public String roomPlayer = null; // in Yandex
    public String deviceId; // in Yandex
    public String mac; // mac in LMS
    public Integer volume_step;
    public Integer volume_low;
    public boolean playing;
    public String mode;
    public Integer volume_high;
    public Integer delay;
    public boolean black = false;
    public boolean separate = false;
    public boolean connected = false;
    public Map<Integer, Integer> schedule;
    public String title = "херпоймичё";

    public String lastPath;
    public String lastPlayTime;
    public int lastChannel = 0;
    public static PlayerStatus playerStatus = new PlayerStatus();

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
        this.black = false;
        this.schedule = new HashMap<>(Map.of(
                0, 5,
                7, 5,
                9, 10,
                20, 10,
                22, 5));
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

    public List<Response.SyncgroupsLoop> syncgroups() {
        Response response = Requests.postToLmsForResponse(RequestParameters.syncgroups().toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("SYNCGROUPS: " + response.result.syncgroups_loop);
        return response.result.syncgroups_loop;
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
        log.info("PLAYER: " + this.name + " SET VOLUME: " + value);
        String status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name);
            return this;
        }
        this.status();
        if (playerStatus.result.mixer_volume < 1) this.volumeSet("1");
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

    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        Requests.postToLmsForStatus(RequestParameters.play(this.name).toString());
        return this;
    }

    public Player pause() {
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
        this.ifNotPlayUnsyncWakeSet();
        String status = Requests.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name);
            return this;
        }
        this
                .saveLastTime()
                .saveLastChannel(channel)
                .saveLastPath(); // path
//                .syncAllOtherPlayingToThis(); // получить mode каждого
        lmsPlayers.write();
        return this;
    }

    public Player playPath(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        Requests.postToLmsForStatus(RequestParameters.play(this.name, path).toString());
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        Requests.postToLmsForStatus(RequestParameters.play(this.name, silence).toString());
        return this;
    }

    public Player prevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public Player nextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
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
        int favSize = this.favorites().size();
        if (this.lastChannel != 0) channel = this.lastChannel - 1;
        else channel = lmsPlayers.lastChannel - 1;
        if (channel < 1) channel = favSize;
        log.info("PLAY PREV CHANNEL: " + channel + " LAST CHANNEL: " + lmsPlayers.lastChannel);
        this.playChannel(channel);
        return this;
    }

    public Player nextChannel() {
        log.info("LAST CHANNEL THIS: " + this.lastChannel + " COMMON: " + lmsPlayers.lastChannel);
        int channel = 1;
        int favSize = this.favorites().size();
        if (this.lastChannel != 0) {
            channel = this.lastChannel + 1;
            log.info("CHANNEL THIS+1: " + channel);
        } else {
            channel = lmsPlayers.lastChannel + 1;
            log.info("CHANNEL COMMON+1: " + channel);
        }
        if (channel > favSize) channel = 1;
        log.info("PLAY CHANNEL: " + channel);
//        Actions.actionPlayChannel(this, channel);
        this.playChannel(channel);
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
        String playerName = this.name;
        String url = this.lastPath;
        int size = this.favorites().size() + 1;
        String title = "New" + size;
        Response response = Requests.postToLmsForResponse(RequestParameters.favoritesAdd(playerName, url, title).toString());
        log.info("FAVORITES ADD");
        return title;
    }

    public Player shuffleon() {
        log.info("PLAYER: " + this.name + " SHUFFLE ON");
        Requests.postToLmsForStatus(RequestParameters.shuffleon(this.name).toString());
        return this;
    }

    public Player shuffleoff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        Requests.postToLmsForStatus(RequestParameters.shuffleoff(this.name).toString());
        return this;
    }

    public Player syncTo(String toPlayerName) {
        log.info("PLAYER: " + this.name + " SYNC TO: " + toPlayerName);
        Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        this.saveLastPath().saveLastTime();
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name);
        Requests.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        return this;
    }

    public Boolean ifNotPlayUnsyncWakeSet() {
        log.info("CHECK PLAYER IF PLAY");
        if (!this.status()) return null;
        if (!this.playing) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET");
            this.unsync().wakeAndSet();
        } else log.info("PLAYER " + this.name + " PLAY - SKIP WAKE");
        return true;
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
        log.info("THIS PATH: " + thisPath);
        log.info("LAST PATH: " + thisLastPath);
        log.info("TRY GLOBAL LAST PATH: " + commonLastPath);
        log.info("SILENCE PATH: " + silence);

        if (thisPath != null && !thisPath.equals(silence) && !thisPath.equals("")) {
            log.info("PLAY THIS PATH");
            this.play().saveLastPath().saveLastTime();
            return this;
        }

        if (commonLastPath != null && !commonLastPath.equals(silence) && !commonLastPath.equals("")) {
            log.info("PLAY COMMON LAST PATH");
            this.playPath(commonLastPath).saveLastPath().saveLastTime();
            return this;
        }

        log.info("PLAY CHANNEL 1");
        this.playChannel(1);
        return this;
    }

    public Player wakeAndSet() {
        log.info("WAKE START >>>");
        if (this.ifTimeExpired()) {
            log.info("PLAYER: " + this.name + " WAKE WAIT: " + this.delay);
            this
                    .playSilence()
                    .volumeSet("+1")
                    .setVolumeByTime()
                    .waitForWake()
                    .volumeSet("-1")
                    .setVolumeByTime()
                    .pause();
            log.info("WAKE FINISH <<<");
        } else {
            log.info("WAKE SKIP <<<");
        }
        return this;
    }

    public Player setVolumeByTime() {
        LocalTime timeNow = LocalTime.now();
        log.info("VOLUME BY TIME: " + timeNow + " OF: " + this.schedule);
        Map.Entry<Integer, Integer> e =
                schedule.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("VOLUME: " + e.getValue() + " BY TIME: " + e.getKey());
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
        this.lastPlayTime = LocalTime.now().truncatedTo(MINUTES).toString();
        log.info("SAVE LAST TIME: " + this.lastPlayTime);
        return this;
    }

    public Player saveLastPath() {
        String path = this.path();
        if (path != null && !path.equals(silence)) {
            this.lastPath = path;
            lmsPlayers.lastPath = this.lastPath;
            log.info("SAVE LAST PATH: " + this.lastPath);
        }
        return this;
    }

    public Player saveLastChannel(int channel) {
        log.info("SAVE LAST CHANNEL: " + channel);
        lmsPlayers.lastChannel = channel;
        this.lastChannel = channel;
        return this;
    }

    public void remove() {
        lmsPlayers.players.remove(this);
    }

    public Player separate_on() { // отдельно от других
        log.info("SEPARATE ON");
        this.separate = true;
        lmsPlayers.write();
        this.unsync();
        Actions.turnOnMusic(this);
        return this;
    }

    public Player alone_on() {  // только этот плеер
        log.info("ALONE ON");
        this.separate = true;
        lmsPlayers.write();
        this.unsync();
        Actions.turnOnMusic(this);
        this.stopAllOther();
        return this;
    }

    public Player separate_alone_off() {
        log.info("SEPARATE ALONE OFF START >>>>>>>>>>");

        if (this.mode().equals("play")) {
            log.info("ALL SEPARATE SET false");
            lmsPlayers.players.forEach(p -> p.separate = false);
            lmsPlayers.write();
            log.info("SYNC ALL PLAYING TO THIS: " + this.name);
            this.syncAllOtherPlayingToThis();
            log.info("SEPARATE ALONE OFF FINISH <<<<<<<<<<");
            return this;
        }

        Player playing = lmsPlayers.getPlayingPlayer(this.name);
        log.info("PLAYING: " + playing);
        log.info("ALL SEPARATE SET false");
        lmsPlayers.players.forEach(p -> p.separate = false);
        lmsPlayers.write();

        if (playing == null) {
            log.info("NO PLAYING. START PLAY THIS: " + this);
            Actions.turnOnMusic(this);
            log.info("SEPARATE ALONE OFF FINISH <<<<<<<<<<");
            return this;
        }

        log.info("SYNC ALL PLAYING TO THIS: " + playing.name);
        playing.syncAllOtherPlayingToThis();
        log.info("SEPARATE ALONE OFF FINISH <<<<<<<<<<");
        return this;
    }

    public Boolean status() {
        log.info("PLAYER STATUS " + this.name);
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(this.name).toString());
        if (json == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("MODE: " + playerStatus.result.mode + " VOLUME: " + playerStatus.result.mixer_volume);
        this.playing = false;
        if (playerStatus.result.mode.equals("play")) this.playing = true;
        return true;
    }

    public Boolean ifPlaying() {
        log.info("CHECK IF PLAYING " + this.name);
//        this.status();
        if (this.playerStatus.result.mode.equals("play")) {
            log.info(this.name + " IS PLAYING: true");
            return true;
        }
        log.info("IS PLAYING: false");
        return false;
    }

    public void title() {
//  запрашивать "playlist", "name" для Soma и Di. для Spoty нету. запросить "artist"
//        log.info("PLAYER: " + this.name + " TITLE: " + this.title);
        String title = this.playerStatus.result.current_title;
        if ((title != null) && (title != "")) {
            if (title.contains(": ")) title = title.replaceAll(":.*", "");
            if (title.contains(" - ")) title = title.replaceAll(" - .*", "");
        }
        if ((title == null) || (title == "")) {
            title = this.artistName();
        }

        if (title == null) title = "херпоймичё";
        this.title = title;
        log.info("TITLE: " + this.title);
    }

    public Player syncAllOtherPlayingToThis() {
        log.info("SYNC ALL PLAYING TO " + this.name);
        log.info("CHECK IF SEPARATE");
        if (this.separate) {
            log.info("PLAYER SEPARETE");
            return this;
        }
        log.info("GET SYNC GROPE");
        List<Response.SyncgroupsLoop> groupe = this.syncgroups();
        List<String> listNamesInGroupe;
        String firstNameinGroupe = null;
        if (groupe != null) {
            String names = groupe.get(0).sync_member_names;
            listNamesInGroupe = List.of(names.split(","));
            firstNameinGroupe = listNamesInGroupe.get(0);
            log.info("PLAYERS IN SYNC GROUPE: " + listNamesInGroupe);
        } else {
            log.info("NO SYNC GROPE");
            listNamesInGroupe = new ArrayList<>();
        }
        lmsPlayers.updateServerStatus();
        log.info("STREAM PLAYERS, FILTER, SYNC TO THIS");
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name))
//                .peek(p -> log.info("PLAYER: " + p.name +
//                        " PLAYING: " + p.playing +
//                        " SEPARATE: " + p.separate +
//                        " SYNC: " + listNamesInGroupe.contains(p.name)))
                .filter(p -> p.playing)
                .filter(p -> !p.name.equals(this.name))
                .filter(p -> !listNamesInGroupe.contains(p.name))
                .filter(p -> !p.separate)
//                .peek(p -> log.info("PLAYER: " + p.name + " SYNC TO: " + this.name))
                .forEach(p -> p.syncTo(this.name));
        Player playerInGroupe = lmsPlayers.getPlayerByName(firstNameinGroupe);
        log.info("IF SYNC GROPE - SYNC " + playerInGroupe + " FIRST TO THIS");
        if (playerInGroupe != null && playerInGroupe.playing) playerInGroupe.syncTo(this.name);
        log.info("FINISH <<<");
        return this;
    }

    public boolean ifTimeExpired() {
        long delay = lmsPlayers.delayExpire;
        if (this.lastPlayTime == null) return true;
        LocalTime playerTime = LocalTime.parse(this.lastPlayTime).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now().truncatedTo(MINUTES);
        log.info("PLAYER LAST TIME: " + playerTime + " NOW TIME: " + nowTime + " DIFF: " + playerTime.until(nowTime, MINUTES) + " DELAY: " + delay);
        long diff = playerTime.until(nowTime, MINUTES);
        if (diff < 0) return true;
        log.info("EXPIRED: " + (diff > delay));
        return (diff > delay);
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", room='" + roomPlayer + '\'' +
                ", id='" + deviceId + '\'' +
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