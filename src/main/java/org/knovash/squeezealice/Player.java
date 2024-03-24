package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;

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
    public String mac;
    public Integer volume_step;
    public Integer volume_low;
    public Integer volume_high;
    public Integer wake_delay;
    public boolean black = false;
    public boolean separate = false;
    public boolean online = false;
    public Map<Integer, Integer> timeVolume;

    public String lastPath;
    public String lastPlayTime;
    public int lastChannel = 1;


    public Player(String name, String mac) {
        this.name = name;
        this.mac = mac;
        this.nameInQuery = this.getQueryNameString();
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
        Response response = Requests.postToLmsForContent(RequestParameters.name(index).toString());
        if (response == null) return "";
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response = Requests.postToLmsForContent(RequestParameters.id(index).toString());
        if (response == null) return "";
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

    public String mode() {
        Response response = Requests.postToLmsForContent(RequestParameters.mode(this.name).toString());
        if (response == null) return "stop";
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public List<Response.SyncgroupsLoop> syncgroups() {
        Response response = Requests.postToLmsForContent(RequestParameters.syncgroups().toString());
        if (response == null) return null;
        log.info("SYNCGROUPS: " + response.result.syncgroups_loop);
        return response.result.syncgroups_loop;
    }

    public String path() {
        Response response = Requests.postToLmsForContent(RequestParameters.path(this.name).toString());
        if (response == null) return "";
        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracks() {
        Response response = Requests.postToLmsForContent(RequestParameters.tracks(this.name).toString());
        if (response == null) return "0";
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

    public String playlistname() {
        Response response = Requests.postToLmsForContent(RequestParameters.playlistname(this.name).toString());
        if (response == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._name);
        return response.result._name;
    }

    public String playlistUrl() {
        Response response = Requests.postToLmsForContent(RequestParameters.playlisturl(this.name).toString());
        if (response == null) return null;
        log.info(response.result.toString());
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._url);
        return response.result._url;
    }

    public String albumname() {
        Response response = Requests.postToLmsForContent(RequestParameters.albumname(this.name).toString());
        if (response == null) return "";
        log.info("PLAYER: " + this.name + " ALBUM: " + response.result._album);
        return response.result._album;
    }

    public String trackname() {
        Response response = Requests.postToLmsForContent(RequestParameters.trackname(this.name).toString());
        if (response == null) return "";
        log.info("PLAYER: " + this.name + " TRACK: " + response.result._title);
        return response.result._title;
    }

    public String artistname() {
        Response response = Requests.postToLmsForContent(RequestParameters.artistname(this.name).toString());
        if (response == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._artist);
        return response.result._artist;
    }

    public String volumeGet() {
        Response response = Requests.postToLmsForContent(RequestParameters.volume(this.name).toString());
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
        log.info("LAST CHANNEL: " + SmartHome.lastChannel);
        int favSize = this.favorites().size();
        this.path();
        int channel = SmartHome.lastChannel - 1;
        if (channel < 1) channel = favSize;
        log.info("PLAY CHANNEL: " + channel);
        Actions.playChannel(this, channel);
        return this;
    }

    public Player nextChannel() {
        log.info("LAST CHANNEL: " + SmartHome.lastChannel);
        int favSize = this.favorites().size();
        int channel = SmartHome.lastChannel + 1;
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
        Response response = Requests.postToLmsForContent(RequestParameters.favorites(playerName, 10).toString());
        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        log.info("FAVORITES: " + playlist);
        return playlist;
    }

    public void favoritesAdd() {
        String playerName = this.name;
        String url ="";
//        url = this.path()
        String title ="";
        Response response = Requests.postToLmsForContent(RequestParameters.favoritesAdd(playerName, url, title ).toString());
        log.info("FAVORITES ADD");

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

    public Player sync(String toPlayerName) {
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
        if (thisLastPath != null && !thisPath.equals(silence)) {
            log.info("PLAY THIS LAST PATH");
            this.playPath(thisLastPath);
            return this;
        }
        if (commonLastPath != null && !thisPath.equals(silence)) {
            log.info("PLAY COMMON LAST PATH");
            this.playPath(commonLastPath);
            return this;
        }
        log.info("PLAY CHANNEL 1");
        this.playChannel(1);
        return this;
    }

    public Player wakeAndSet() {
        log.info("WAKE START -------------------");
//        if (true) {
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
            log.info("WAKE FINISH ------------------");
        } else {
            log.info("WAKE SKIP ------------------");
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
        SmartHome.lastChannel = channel;
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
        log.info("SEPARATE ALONE OFF");
        lmsPlayers.players.forEach(p -> p.separate = false);
        lmsPlayers.write();
        Actions.turnOnMusic(this);
        return this;
    }

    public Player saveLastTime() {
        this.lastPlayTime = LocalTime.now().truncatedTo(MINUTES).toString();
        log.info("SAVE LAST TIME: " + this.lastPlayTime);
        return this;
    }

    public Player syncAllOtherPlayingToThis() {
        log.info("SYNC ALL OTHER PLAYING to " + this.name + " SEPARATE: " + this.separate);
        if (this.separate) {
            log.info("PLAYER SEPARETE OR ALONE - RETURN");
            return this;
        }
        lmsPlayers.update();
        List<String> listNamesOnline = lmsPlayers.playersOnlineNames;
        log.info("PLAYERS ONLINE: " + listNamesOnline);
        listNamesOnline.remove(this.name);
        List<Response.SyncgroupsLoop> groupe = this.syncgroups();
        if (groupe != null) {
            String names = groupe.get(0).sync_member_names;
            List<String> listNamesInGroupe = List.of(names.split(","));
            listNamesOnline.removeAll(listNamesInGroupe);
        }
        log.info("FOREACH SYNC TO : " + this.name);
        listNamesOnline.stream()
                .map(n -> lmsPlayers.getPlayerByName(n))
                .filter(p -> p.mode().equals("play"))
                .peek(p -> log.info(p.name + " separate: " + p.separate))
                .filter(p -> !p.separate)
                .forEach(p -> p.sync(this.name));
        log.info("SYNC ALL OTHER PLAYING FINISH");
        return this;
    }

    public String getQueryNameString() {
        return this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
    }

    @Override
    public String toString() {
        return "\nPlayer{" +
                "name='" + name + '\'' +
                ", id='" + mac + '\'' +
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