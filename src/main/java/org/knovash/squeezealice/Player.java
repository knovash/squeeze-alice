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
    public String id;
    public Integer volume_step;
    public Integer volume_low;
    public boolean playing;
    public Integer volume_high;
    public Integer wake_delay;
    public boolean black = false;
    public boolean separate = false;
    public boolean connected = false;
    public Map<Integer, Integer> timeVolume;
    public String title = "херпоймичё";

    public String lastPath;
    public String lastPlayTime;
    public int lastChannel = 1;
    public PlayerStatus status = new PlayerStatus();


    public Player(String name, String id) {
        this.name = name;
        this.id = id;
        this.title = "херпоймичё";
        this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 25;
        this.wake_delay = 10000;
        this.black = false;
        this.timeVolume = new HashMap<>(Map.of(
                0, 5,
                7, 5,
                9, 10,
                20, 10,
                22, 5));
    }

    public static String name(String index) {
        Response response = Requests.postToLmsForResponse(RequestParameters.name(index).toString());
        if (response == null) return "";
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response = Requests.postToLmsForResponse(RequestParameters.id(index).toString());
        if (response == null) return "";
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

    public String mode() {
        this.playing = false;
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) return "stop";
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public List<Response.SyncgroupsLoop> syncgroups() {
        Response response = Requests.postToLmsForResponse(RequestParameters.syncgroups().toString());
        if (response == null) return null;
        log.info("SYNCGROUPS: " + response.result.syncgroups_loop);
        return response.result.syncgroups_loop;
    }

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) return "";
        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracks() {
        Response response = Requests.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
        if (response == null) return "0";
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

    public String playlistname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._name);
        return response.result._name;
    }

    public String playlistUrl() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlisturl(this.name).toString());
        if (response == null) return null;
        log.info(response.result.toString());
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._url);
        return response.result._url;
    }

    public String albumname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.albumname(this.name).toString());
        if (response == null) return "";
        log.info("PLAYER: " + this.name + " ALBUM: " + response.result._album);
        return response.result._album;
    }

    public String trackname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.trackname(this.name).toString());
        if (response == null) return "";
        log.info("PLAYER: " + this.name + " TRACK: " + response.result._title);
        return response.result._title;
    }

    public String artistname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._artist);
        return response.result._artist;
    }

    public String volumeGet() {
        Response response = Requests.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response == null) return "0";
        log.info("PLAYER: " + this.name + " GET VOLUME: " + response.result._volume);
        return response.result._volume;
    }

    public Player volumeSet(String value) {
        log.info("PLAYER: " + this.name + " SET VOLUME: " + value);
        String status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (Integer.parseInt(this.volumeGet()) < 1) this.volumeSet("1");
        log.info("STATUS: " + status);
        return this;
    }

    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        String status = Requests.postToLmsForStatus(RequestParameters.play(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        String status = Requests.postToLmsForStatus(RequestParameters.pause(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player play_pause() {
        log.info("PLAYER: " + this.name + " PLAY/PAUSE");
        String status = Requests.postToLmsForStatus(RequestParameters.play_pause(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player playChannel(Integer channel) {
        log.info("PLAYER: " + this.name + " PLAY CHANNEL: " + channel);
        String status = Requests.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player playPath(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        String status = Requests.postToLmsForStatus(RequestParameters.play(this.name, path).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        String status = Requests.postToLmsForStatus(RequestParameters.play(this.name, silence).toString());
        log.info("STATUS: " + status);
        return this;
    }


    public Player prevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        String status = Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player nextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        String status = Requests.postToLmsForStatus(RequestParameters.nexttrack(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player prevChannel() {
        log.info("LAST CHANNEL: " + lmsPlayers.lastChannel);
        int channel = 1;
        int favSize = this.favorites().size();
        if (this.lastChannel != 0) channel = this.lastChannel - 1;
        else channel = lmsPlayers.lastChannel - 1;
        if (channel < 1) channel = favSize;
        log.info("PLAY CHANNEL: " + channel);
        Actions.playChannel(this, channel);
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
        Actions.playChannel(this, channel);
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
        String status = Requests.postToLmsForStatus(RequestParameters.shuffleon(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player shuffleoff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        String status = Requests.postToLmsForStatus(RequestParameters.shuffleoff(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player syncTo(String toPlayerName) {
        log.info("PLAYER: " + this.name + " SYNC TO: " + toPlayerName);
//  пока сломанана синхронизация di.fm в LMS
//  https://forums.slimdevices.com/forum/user-forums/logitech-media-server/1673928-logitech-media-server-8-4-0-released?p=1675699#post1675699
//  https://github.com/Logitech/slimserver/issues/993
        String path = lmsPlayers.getPlayerByName(toPlayerName).path();
        if (path.contains("di.fm") || path.contains("audioaddict")) {
            log.info("SYNC DI.FM");
            this.playPath(path);
            log.info("STATUS: sync to audioaddict finish");
            return this;
        } else {
            log.info("SYNC NORMAL");
            String status = Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
            log.info("STATUS: " + status);
        }
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name);
        String status = Requests.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        log.info("STATUS: " + status);
        return this;
    }

    public Player ifNotPlayUnsyncWakeSet() {
        log.info("CHECK IF PLAY");
        if (!this.mode().equals("play")) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET");
            this.unsync().wakeAndSet();
        } else log.info("PLAYER " + this.name + " PLAY - SKIP WAKE");
        return this;
    }

    public Player stopAllOther() {
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name))
                .forEach(player -> player.unsync());
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

        if (thisPath != null && !thisPath.equals(silence)) {
            log.info("PLAY THIS PATH");
            this.play();
            return this;
        }

        if (commonLastPath != null && !commonLastPath.equals(silence)) {
            log.info("PLAY COMMON LAST PATH");
            this.playPath(commonLastPath);
            return this;
        }

        log.info("PLAY CHANNEL 1");
        this.playChannel(1).saveLastChannel(1);
        return this;
    }

    public Player wakeAndSet() {
        log.info("WAKE START >>>>>>>>>>");
        if (Actions.timeExpired(this)) {
            log.info("PLAYER: " + this.name + " WAKE WAIT: " + this.wake_delay);
            this
                    .playSilence()
                    .volumeSet("+1")
                    .setVolumeByTime()
                    .waitForWake()
                    .volumeSet("-1")
                    .setVolumeByTime()
                    .pause();
            log.info("WAKE FINISH <<<<<<<<<<");
        } else {
            log.info("WAKE SKIP <<<<<<<<<<");
        }
        return this;
    }

    public Player setVolumeByTime() {
        LocalTime timeNow = LocalTime.now();
        log.info("VOLUME BY TIME: " + timeNow + " OF: " + this.timeVolume);
        Map.Entry<Integer, Integer> e =
                timeVolume.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("VOLUME: " + e.getValue() + " BY TIME: " + e.getKey());
        this.volumeSet(String.valueOf(e.getValue()));
        return this;
    }

    public Player waitForWake() {
        log.info("WAIT " + wake_delay + " . . . . .");
        try {
            Thread.sleep(this.wake_delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Player waitFor(int delay) {
        log.info("WAIT " + delay + " START");
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("WAIT " + delay + " FINISH");
        return this;
    }

    public Player saveLastPath() {
        log.info("SAVE LAST PATH");
        String path = this.path();
        if (path != null && !path.equals(silence)) {
            this.lastPath = path;
            lmsPlayers.lastPath = this.lastPath;
        }
        log.info("SAVE LAST PATH: " + this.lastPath);
        return this;
    }

    public Player saveLastChannel(int channel) {
        log.info("SAVE LAST CHANNEL: " + channel);
        lmsPlayers.lastChannel = channel;
        this.lastChannel = channel;
        return this;
    }

    public Player saveLastPathLink(String path) {
        log.info("SAVE LAST PATH LINK START");
        this.lastPath = path;
        lmsPlayers.lastPath = this.lastPath;
        log.info("SAVED LAST PATH: " + this.lastPath);
//        log.info("SAVED LAST PATH G : " + lastPathGlobal);
        log.info("SAVE LAST PATH LINK STOP");
        return this;
    }

    public void remove() {
        lmsPlayers.players.remove(this);
    }

    public Player separate_on() { // отдельно от других
        log.info("SEPARATE ON");
        this.separate = true;
        lmsPlayers.write();
        this
                .unsync()
                .play();
        return this;
    }

    public Player alone_on() {  // только этот плеер
        log.info("ALONE ON");
        this.separate = true;
        lmsPlayers.write();
        this
                .unsync()
                .play()
                .stopAllOther();
        return this;
    }

    public Player separate_alone_off() {
        log.info("SEPARATE ALONE OFF - SYNC ALL PLAYING TO PLAYING");
        Player playing = lmsPlayers.getPlayingPlayer("none");
        log.info("PLAYING: " + playing);
        if (playing == null) {
            playing = this;
            if (!playing.mode().equals("play")) playing.play();
        }
        log.info("ALL SEPARATE SET false");
        lmsPlayers.players.forEach(p -> p.separate = false);
        lmsPlayers.write();
        log.info("SYNC ALL PLAYING TO: " + playing);
        playing.syncAllOtherPlayingToThis();
        return playing;
    }

    public Player saveLastTime() {
        this.lastPlayTime = LocalTime.now().truncatedTo(MINUTES).toString();
        log.info("SAVE LAST TIME: " + this.lastPlayTime);
        return this;
    }

    public Player status() {
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(this.name).toString());
        log.info("STATUS JSON: " + json);
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        PlayerStatus playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        log.info("STATUS MODE: " + playerStatus.result.mode);
        log.info("STATUS VOLUME: " + playerStatus.result.mixer_volume);
        log.info("STATUS TITLE: " + playerStatus.result.current_title);
        log.info("STATUS INDEX: " + playerStatus.result.playlist_cur_index);
        this.status = playerStatus;
        return this;
    }

    public Player title() {
//  запрашивать "playlist", "name" для Soma и Di. для Spoty нету. запросить "artist"
        log.info("START PLAYER: " + this.name + " TITLE: " + this.title);
        String title = this.status.result.current_title;
        if ((title != null) || (title != "")) {
            if (title.contains(":")) title = title.replaceAll(":.*", "");
            if (title.contains("- DI.FM")) title = title.replaceAll(" - DI.FM.*", "");
            log.info("TITLE playlistname: _" + title + "_");
        }
        if ((title == null) || (title == "")) title = this.artistname();
        if (title == null) title = "херпоймичё";
        this.title = title;
        log.info("TITLE: " + this.title);
        return this;
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
        log.info("UPDATE");
        lmsPlayers.updateNew();
        log.info("STREAM PLAYERS, FILTER, SYNC TO THIS");
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name))
                .peek(p -> log.info("PLAYER: " + p.name +
                        " PLAYING: " + p.playing +
                        " SEPARATE: " + p.separate +
                        " SYNC: " + listNamesInGroupe.contains(p.name)))
                .filter(p -> p.playing)
                .filter(p -> !p.name.equals(this.name))
                .filter(p -> !listNamesInGroupe.contains(p.name))
                .filter(p -> !p.separate)
                .peek(p -> log.info("PLAYER: " + p.name + " SYNC TO: " + this.name))
                .forEach(p -> p.syncTo(this.name));
        Player playerInGroupe = lmsPlayers.getPlayerByName(firstNameinGroupe);
        log.info("IF SYNC GROPE - SYNC " + playerInGroupe + "FIRST TO THIS");
        if (playerInGroupe != null && playerInGroupe.playing) playerInGroupe.syncTo(this.name);
        log.info("SYNC ALL OTHER PLAYING FINISH");
        return this;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(getName(), player.getName()) && Objects.equals(getId(), player.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getId());
    }
}