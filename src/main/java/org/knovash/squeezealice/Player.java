package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.ResponseFromLms;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import static org.knovash.squeezealice.Main.SILENCE;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    public String name;
    public String id;
    public Integer volumeStep;
    public Integer volumeAlicePrevious;
    public Integer presetLow;
    public Integer presetHigh;
    public Integer wakeDelay;
    public boolean black;
    public Integer volumeAliceLow;
    public Integer volumeAliceHigh;
    public Map<Integer, Integer> timeVolume;

    public static String lastPath;
    public static Integer lastChannel;

    public Player(String name, String id) {
        this.name = name;
        this.id = id;
        this.volumeAlicePrevious = 1;
        this.volumeStep = 5;
        this.presetLow = 5;
        this.presetHigh = 20;
        this.wakeDelay = 10000;
        this.volumeAliceLow = 1;
        this.volumeAliceHigh = 9;
        this.black = false;
        this.timeVolume = new HashMap<>(Map.of(20, 5, 7, 5, 0, 2, 22, 5, 16, 7));
    }

    public static String name(String index) {
        Response response = Fluent.post(Requests.name(index).toString());
        Content content;
        try {
            content = response.returnContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("NAME: " + responseFromLms.result._name);
        return responseFromLms.result._name;
    }

    public static String id(String index) {
        Response response = Fluent.post(Requests.id(index).toString());
        Content content;
        try {
            content = response.returnContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("ID: " + responseFromLms.result._id);
        return responseFromLms.result._id;
    }

    public String mode() {
        Response response = Fluent.post(Requests.mode(this.name).toString());
        Content content;
        try {
            content = response.returnContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("PLAYER: " + this.name + " MODE: " + responseFromLms.result._mode);
        return responseFromLms.result._mode;
    }

    public String path() {
        Response response = Fluent.post(Requests.path(this.name).toString());
        Content content;
        try {
            content = response.returnContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("PLAYER: " + this.name + " PATH: " + responseFromLms.result._path);
        return responseFromLms.result._path;
    }

    public Player volume(String value) {
        log.info("PLAYER: " + this.name + " VOLUME: " + value);
        Response response = Fluent.post(Requests.volume(this.name, value).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            log.info("ERROR");
            throw new RuntimeException(e);
        }
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player play(Integer channel) {
        log.info("PLAYER: " + this.name + " PLAY CHANNEL: " + channel);
        Response response = Fluent.post(Requests.play(this.name, channel - 1).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Player.lastChannel = channel;
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player play(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        Response response = Fluent.post(Requests.play(this.name, path).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Player.lastPath = path;
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILINCE: " + SILENCE);
        Response response = Fluent.post(Requests.play(this.name, SILENCE).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        Response response = Fluent.post(Requests.pause(this.name).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player sync(String toPlayer) {
        log.info("PLAYER: " + this.name + " SYNC TO: " + toPlayer);
        Response response = Fluent.post(Requests.sync(this.name, toPlayer).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player unsync() {
        log.info("PLAYER: " + this.name + " UNSYNC");
        Response response = Fluent.post(Requests.unsync(this.name).toString());
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("SATUS: " + httpResponse.getStatusLine());
        if (httpResponse.getStatusLine().getStatusCode() == 200) return this;
        return null;
    }

    public Player wakeAndSet() {
        log.info("PLAYER: " + this.name + " WAKE WAIT: " + this.wakeDelay);
        this.playSilence()
                .volume("-1")
                .setVolumeByTime()
                .waitForWake()
                .volume("-1")
                .setVolumeByTime()
                .pause();
        return this;
    }

    public Player setVolumeByTime() {
        log.info("VOLUME BY TIME");
        log.info(this.timeVolume);
        LocalTime time = LocalTime.now();
        log.info("TIME: " + time);
        int volume =
                timeVolume.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(),0).isBefore(time))
                        .peek(entry -> log.info("FILTER " + entry))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get()
                        .getValue();
        log.info("VOLUME: " + volume);
        this.volume(String.valueOf(volume));
        return this;
    }

    public Player waitForWake() {
        log.info("WAIT " + wakeDelay);
        try {
            Thread.sleep(this.wakeDelay); // wait
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
