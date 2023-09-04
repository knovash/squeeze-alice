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


    public Player() {
    }

    public Player(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public Player(String name) {
        this.name = name;
        this.id = id;
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
                '}';
    }

    public static void play(String player, String channel) {
        log.info("static PLAYER: " + player + " CHANNEL: " + channel);
        Fluent.post(Requests.favoritePlay(player, channel).toString());
    }


    public static String name(String index) {
        Content content = Fluent.post(Requests.getName(index).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Content content = Fluent.post(Requests.getId(index).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(response.result._id);
        return response.result._id;
    }

    public static String mode(String player) {
        Content content = Fluent.post(Requests.getMode(player).toString());
        Response response;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            response = objectMapper.readValue(content.asString(), Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(response.result._mode);
        return response.result._mode;
    }


    public void pause() {
        log.info("pause " + this.name);
        Fluent.post(Requests.playerPause(this.name).toString());
    }

    public void volumeSet(String value) {
        log.info("PLAYER: " + this.name + " VOLUME: " + value);
        Fluent.post(Requests.volumeset(this.name, value).toString());
    }

    public static void volumeSet(String player, String value) {
        log.info("static PLAYER: " + player + " VOLUME: " + value);
        Fluent.post(Requests.volumeset(player, value).toString());
    }

    public static void volumeGet(String player) {
        log.info(player);
        Fluent.post(Requests.volumeget(player).toString());
    }

    public static void favoritePlay(String player) {
        log.info(player);
        Fluent.post(Requests.favoritePlay(player, "3").toString());
    }

}
