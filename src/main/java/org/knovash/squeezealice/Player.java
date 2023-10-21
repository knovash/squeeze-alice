package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.pojo.lms.ResponseFromLms;

import java.time.LocalTime;
import java.util.*;

import static org.knovash.squeezealice.Main.silence;
import static org.knovash.squeezealice.Main.server;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    public String name;
    public String id;
    public Integer volume_step;
    public Integer volume_alice_previous;
    public Integer volume_low;
    public Integer volume_high;
    public Integer wake_delay;
    public boolean black;
    public Integer volume_alice_low;
    public Integer volume_alice_high;
    public Map<Integer, Integer> timeVolume;
    public String lastPath;
    public LocalTime lastPlayTime;

    public static String lastStrPath;
    public static String lastPathGlobal;

    public Player(String name, String id) {
        this.name = name;
        this.id = id;
        this.volume_alice_previous = 1;
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 25;
        this.wake_delay = 10000;
        this.volume_alice_low = 1;
        this.volume_alice_high = 9;
        this.black = false;
        this.timeVolume = new HashMap<>(Map.of(
                0, 5,
                7, 10,
                8, 15,
                20, 10,
                22, 5));
    }

    public static String name(String index) {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.name(index).toString());
        if (responseFromLms == null) return "";
        log.info("NAME: " + responseFromLms.result._name);
        return responseFromLms.result._name;
    }

    public static String id(String index) {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.id(index).toString());
        if (responseFromLms == null) return "";
        log.info("ID: " + responseFromLms.result._id);
        return responseFromLms.result._id;
    }

    public String mode() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.mode(this.name).toString());
        if (responseFromLms == null) return "";
        log.info("PLAYER: " + this.name + " MODE: " + responseFromLms.result._mode);
        return responseFromLms.result._mode;
    }

    public String path() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.path(this.name).toString());
        if (responseFromLms == null) return "";
        log.info("PLAYER: " + this.name + " PATH: " + responseFromLms.result._path);
        return responseFromLms.result._path;
    }

    public String playlistname() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.playlistname(this.name).toString());
        if (responseFromLms == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + responseFromLms.result._name);
        return responseFromLms.result._name;
    }

    public String albumname() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.albumname(this.name).toString());
        if (responseFromLms == null) return "";
        log.info("PLAYER: " + this.name + " ALBUM: " + responseFromLms.result._album);
        return responseFromLms.result._album;
    }

    public String trackname() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.trackname(this.name).toString());
        if (responseFromLms == null) return "";
        log.info("PLAYER: " + this.name + " TRACK: " + responseFromLms.result._title);
        return responseFromLms.result._title;
    }

    public String artistname() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.artistname(this.name).toString());
        if (responseFromLms == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + responseFromLms.result._artist);
        return responseFromLms.result._artist;
    }

    public String volume() {
        ResponseFromLms responseFromLms = Fluent.postGetContent(Requests.volume(this.name).toString());
        if (responseFromLms == null) return null;
        log.info("PLAYER: " + this.name + " GET VOLUME: " + responseFromLms.result._volume);
        return responseFromLms.result._volume;
    }

    public Player volume(String value) {
        log.info("PLAYER: " + this.name + " SET VOLUME: " + value);
        String status = Fluent.postGetStatus(Requests.volume(this.name, value).toString());
        log.info("SATUS: " + status);
        return this;
    }

    public Player play(Integer channel) {
        log.info("PLAYER: " + this.name + " PLAY CHANNEL: " + channel);
        String status = Fluent.postGetStatus(Requests.play(this.name, channel - 1).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        return this;
    }

    public Player play(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        String status = Fluent.postGetStatus(Requests.play(this.name, path).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        String status = Fluent.postGetStatus(Requests.play(this.name, silence).toString());
        log.info("SATUS: " + status);
        return this;
    }

    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        String status = Fluent.postGetStatus(Requests.play(this.name).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        return this;
    }

    public Player pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        String status = Fluent.postGetStatus(Requests.pause(this.name).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        this.saveLastTimeIfPlay();
        return this;
    }

    public Player play_pause() {
        log.info("PLAYER: " + this.name + " PLAY/PAUSE");
        String status = Fluent.postGetStatus(Requests.play_pause(this.name).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        return this;
    }

    public Player prevtrack() {
        log.info("PLAYER: " + this.name + " NEXT");
        String status = Fluent.postGetStatus(Requests.prevtrack(this.name).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        return this;
    }

    public Player nexttrack() {
        log.info("PLAYER: " + this.name + " NEXT");
        String status = Fluent.postGetStatus(Requests.nexttrack(this.name).toString());
        log.info("SATUS: " + status);
        this.saveLastPath();
        return this;
    }

    public Player shuffleon() {
        log.info("PLAYER: " + this.name + " SHUFFLE ON");
        String status = Fluent.postGetStatus(Requests.shuffleon(this.name).toString());
        log.info("SATUS: " + status);
        return this;
    }

    public Player shuffleoff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        String status = Fluent.postGetStatus(Requests.shuffleoff(this.name).toString());
        log.info("SATUS: " + status);
        return this;
    }

    public Player sync(String toPlayer) {
        log.info("PLAYER: " + this.name + " SYNC TO: " + toPlayer);
        String status = Fluent.postGetStatus(Requests.sync(this.name, toPlayer).toString());
        log.info("SATUS: " + status);
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name);
        String status = Fluent.postGetStatus(Requests.unsync(this.name).toString());
        log.info("SATUS: " + status);
        return this;
    }

    public Player playLast() {
        log.info("PLAY LAST");
        log.info("TRY PATH: " + this.path());
        log.info("TRY LAST PATH: " + this.lastPath);
        log.info("TRY GLOBAL LAST PATH: " + Player.lastPathGlobal);

        if (this.path() == null ||
                this.path().equals(silence) ||
                this.path().contains("tishini") ||
                this.path().contains("silence") ||
                this.path().contains("korot") ||
                this.path().contains("spotify")
        ) {
            log.info("SKIP PATH: " + this.path());
        } else {
            log.info("PRESS BUTTON PLAY: " + this.path());
            this.play();
            return this;
        }
        if (this.lastPath == null ||
                this.lastPath.equals(silence) ||
                this.lastPath.contains("tishini") ||
                this.lastPath.contains("silence") ||
                this.lastPath.contains("korot") ||
                this.lastPath.contains("spotify")
        ) {
            log.info("SKIP LAST PATH: " + this.lastPath);
        } else {
            log.info("PLAY PLAYER LAST PATH: " + this.lastPath);
            this.play(this.lastPath);
            return this;
        }
        if (Player.lastPathGlobal == null ||
                Player.lastPathGlobal.equals(silence) ||
                Player.lastPathGlobal.contains("tishini") ||
                Player.lastPathGlobal.contains("korot") ||
                Player.lastPathGlobal.contains("silence") ||
                Player.lastPathGlobal.contains("spotify")
        ) {
            log.info("SKIP GLOBAL PATH: " + Player.lastPathGlobal);
        } else {
            log.info("PLAY GLOBAL LAST PATH: " + Player.lastPathGlobal);
            this.play(Player.lastPathGlobal);
            return this;
        }
        log.info("PLAY FIRST FAVORITE");
        this.play(1);
        return this;
    }

    public Player wakeAndSet() {
        log.info("PLAYER: " + this.name + " WAKE WAIT: " + this.wake_delay);
        this
                .saveLastPath()
                .playSilence()
                .volume("-1")
                .setVolumeByTime()
                .waitForWake()
                .volume("-1")
                .setVolumeByTime()
                .pause();
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
        this.volume(String.valueOf(e.getValue()));
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

    public Player saveLastPath() {
        String path = this.path();
        if (path != null && !path.equals(silence)) {
            this.lastPath = path;
            lastPathGlobal = this.lastPath;
        }
        return this;
    }

    public void remove() {
        server.players.remove(this);
    }

    public Player saveLastTime() {
        this.lastPlayTime = LocalTime.now();
        return this;
    }

    public Player saveLastTimeIfPlay() {
        if (this.mode().equals("play")) this.lastPlayTime = LocalTime.now();
        return this;
    }

    @Override
    public String toString() {
        return "\nPlayer{" +
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