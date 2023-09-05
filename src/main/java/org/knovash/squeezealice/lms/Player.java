package org.knovash.squeezealice.lms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Content;
import org.knovash.squeezealice.Fluent;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.Response;
import org.knovash.squeezealice.enums.Mode;

import java.util.Objects;

import static java.rmi.server.LogStream.SILENT;
import static org.knovash.squeezealice.Main.serverLMS;

@Log4j2
@Data
//@NoArgsConstructor
@AllArgsConstructor
public class Player {

    public String name;
    public String id;
    public String volume;
    public Mode mode;
    public String path;
    public String title;
    public Integer volumeStep;
    public Integer volumePrevious;
    public boolean black;

    public static String pathLast;

    public Player(String name, String id) {
        this.name = name;
        this.id = id;
        this.volumePrevious = 1;
        this.volumeStep = 5;
        this.black = false;
    }

    public static String name(String index) {
        Content content = Fluent.post(Requests.name(index).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Content content = Fluent.post(Requests.id(index).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

    public static String mode(String player) {
        Content content = Fluent.post(Requests.mode(player).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("PLAYER: " + player + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public static String check(String player) {
        log.info("PLAYER: " + player + " CHECK");
        Content content = Fluent.post(Requests.mode(player).toString());
        Response response;
        log.info("CHECK CONTENT: " + content);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        log.info("CHECK RESPONSE: " + response);
        log.info("CHECK: " + player + " MODE: " + response.result._mode);
        return "response.result._mode";
    }

    public static void channel(String player, Integer channel) {
        log.info("PLAYER: " + player + " CHANNEL: " + channel);
        Fluent.post(Requests.play(player, channel).toString());
    }

    public static void play(String player, String path) {
        log.info("PLAYER: " + player + " PATH: " + path);
        Fluent.post(Requests.play(player, path).toString());
    }
    public void play( Integer channel) {
        log.info("PLAYER: " + this.name + " CHANNEL: " + channel);
        Fluent.post(Requests.play(this.name, channel).toString());
    }

    public static void volume(String player, String value) {
        log.info("PLAYER: " + player + " VOLUME: " + value);
        Fluent.post(Requests.volume(player, value).toString());
    }

    public static String volumeget(String player) {
        Content content = Fluent.post(Requests.mode(player).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("PLAYER: " + player + " MODE: " + response.result._volume);
        return response.result._volume;
    }

    public static Player playerByName(String name) {

        return serverLMS.players.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
    }

    // -----------------------------------------------------

    public void pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        Fluent.post(Requests.pause(this.name).toString());
    }

    public void volume(String value) {
        log.info("PLAYER: " + this.name + " VOLUME: " + value);
        Fluent.post(Requests.volume(this.name, value).toString());
    }

    public String volumeget() {
        // Запрос громкости, должно вернуть json c параметром value=0, где 0 текущая громкость
        log.info("GET PLAYER VOLUME");
//        Content content = Fluent.post(Requests.volume(this.name).toString());
//        Response response;
//        ObjectMapper objectMapper = new ObjectMapper();
//        try {
//            response = objectMapper.readValue(content.asString(), Response.class);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        log.info("PLAYER: " + this.name + " VOLUME: " + response.result._volume);
//        return response.result._volume;
        return "{\"value\":\"4\"}";
    }

    public static String channelget(String player) {
        // Запрос состояния, должно вернуть json c параметром value=(true|false)
        log.info("GET PLAYER CHANNEL");
        return "{\"value\":\"true\"}";
    }

    public static String stateget(String player) {
        // Запрос состояния, должно вернуть json c параметром value=(true|false)
        log.info("GET PLAYER STATE");
        return "{\"value\":\"true\"}";
    }

    public String mode() {
        log.info("GET PLAYER MODE");
        Content content = Fluent.post(Requests.mode(this.name).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public void wake() {
        log.info("PLAYER: " + this.name + " WAKE");
        Fluent.post(Requests.play(this.name, SILENT).toString());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Fluent.post(Requests.pause(this.name).toString());
    }

    public static void wake(String name) {
        log.info("PLAYER: " + name + " WAKE");
        Fluent.post(Requests.play(name, SILENT).toString());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Fluent.post(Requests.pause(name).toString());
    }

    public void check(Player player) {
        log.info("PLAYER: " + this.name + " CHECK");

        serverLMS.players.contains(player);

    }

    public void sync(String toPlayer) {
        log.info("PLAYER: " + this.name + " PAUSE");
        Fluent.post(Requests.sync(this.name, toPlayer).toString());
    }


    @Override
    public String toString() {
        return "\nPlayer{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", volume='" + volume + '\'' +
                ", mode=" + mode +
                ", path='" + path + '\'' +
                ", title='" + title + '\'' +
                ", volumeStep=" + volumeStep +
                ", volumePrevious=" + volumePrevious +
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
