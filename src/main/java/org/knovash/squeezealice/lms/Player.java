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

@Log4j2
@Data
@AllArgsConstructor
public class Player {

    String name;
    String id;
    String volume;
    Mode mode;
    String path;
    String title;
    Integer volumeStep;
  public   Integer volumeLastAlice;

    public Player() {
    }

    public Player(String name, String id) {
        this.name = name;
        this.id = id;
        this.volumeLastAlice = 1;
        this.volumeStep = 2;
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

    public static void channel(String player, String channel) {
        log.info("PLAYER: " + player + " CHANNEL: " + channel);
        Fluent.post(Requests.channel(player, channel).toString());
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
        return ServerLMS.players.stream().filter(p -> p.getName().equals(name)).findFirst().orElse(null);
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
//        Fluent.post(Requests.volume(this.name, value).toString());
    }

    public void check() {
        log.info("PLAYER: " + this.name + " CHECK");
//        Fluent.post(Requests.volume(this.name, value).toString());
    }
}
