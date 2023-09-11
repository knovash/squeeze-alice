package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
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

    public static String lastStrPath;
    public static String lastPathGlobal;

    public Player(String name, String id) {
        this.name = name;
        this.id = id;
        this.volume_alice_previous = 1;
        this.volume_step = 5;
        this.volume_low = 5;
        this.volume_high = 20;
        this.wake_delay = 10000;
        this.volume_alice_low = 1;
        this.volume_alice_high = 9;
        this.black = false;
        this.timeVolume = new HashMap<>(Map.of(
                0, 3,
                7, 5,
                8, 10,
                9, 15,
                20, 10,
                22, 5));
    }

    public static String name(String index) {
        Response response = Fluent.post(Requests.name(index).toString());
        if (response == null) {return "";}
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
        if (response == null) {return "";}
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
        if (response == null) {return "";}
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
        if (response == null) {return "";}
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
        if (response == null) {return this;}
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.saveLastPath();
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public String volume() {
        Response response = Fluent.post(Requests.volume(this.name).toString());
        if (response == null) {return "";}
        Content content;
        try {
            content = response.returnContent();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("PLAYER: " + this.name + " _VOLUME: " + responseFromLms.result._volume);
        return responseFromLms.result._volume;
    }

    public Player play(Integer channel) {
        log.info("PLAYER: " + this.name + " PLAY CHANNEL: " + channel);
        Response response = Fluent.post(Requests.play(this.name, channel - 1).toString());
        if (response == null) {return this;}
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.saveLastPath();
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player play(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        Response response = Fluent.post(Requests.play(this.name, path).toString());
        if (response == null) {return this;}
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.saveLastPath();
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILINCE: " + SILENCE);
        Response response = Fluent.post(Requests.play(this.name, SILENCE).toString());
        if (response == null) {return this;}
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        Response response = Fluent.post(Requests.play(this.name).toString());
        if (response == null) {return this;}
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.saveLastPath();
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player playLast() {
        if (this.path() != null && !this.path().equals(SILENCE)) {
            log.info("PLAY: " + this.path());
            this.play();
            return this;
        }
        if (this.lastPath != null && !this.lastPath.equals(SILENCE)) {
            log.info("PLAY PLAYER LAST PATH: " + this.lastPath);
            this.play(this.lastPath);
            return this;
        }
        if (Player.lastPathGlobal != null && !Player.lastPathGlobal.equals(SILENCE)) {
            log.info("PLAY PLAYER LAST PATH GLOBAL: " + Player.lastPathGlobal);
            this.play(Player.lastPathGlobal);
            return this;
        }
        this.play(1); // играть первое избранное
        return this;
    }


    public Player pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        Response response = Fluent.post(Requests.pause(this.name).toString());
        if (response == null) {return this;}
        HttpResponse httpResponse;
        try {
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.lastPath = this.path();
        lastPathGlobal = this.lastPath;
        log.info("SATUS: " + httpResponse.getStatusLine());
        return this;
    }

    public Player sync(String toPlayer) {
        log.info("PLAYER: " + this.name + " SYNC TO: " + toPlayer);
        Response response = Fluent.post(Requests.sync(this.name, toPlayer).toString());
        if (response == null) {return this;}
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
        if (response == null) {return this;}
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
        LocalTime time = LocalTime.now();
        log.info("VOLUME BY TIME: " + time + " OF: " + this.timeVolume);
        int volume =
                timeVolume.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(time))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get()
                        .getValue();
        log.info("VOLUME: " + volume);
        this.volume(String.valueOf(volume));
        return this;
    }

    public Player waitForWake() {
        log.info("WAIT " + wake_delay);
        log.info(". . . . . .");
        try {
            Thread.sleep(this.wake_delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Player saveLastPath() {
        String path = this.path();
        if (path != null && !path.equals(SILENCE)) {
            this.lastPath = path;
            lastPathGlobal = this.lastPath;
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
