package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Content;
import org.knovash.squeezealice.enums.Mode;

@Log4j2
@Data
@AllArgsConstructor
public class Player {

    String name;
    String id;
    String volume;


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

    Mode mode;
    String path;
    String title;


    public static int counter;

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

    public static void play(){}

    public static void count() {
        Response response;
        Content content;
        String json;
        ObjectMapper objectMapper = new ObjectMapper();

        content = Fluent.post(Requests.getCount.toString());
        json = content.asString();
        try {
            response = objectMapper.readValue(json, Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info(response.result._count);
        counter = Integer.parseInt(response.result._count);
    }

    public static String name(String index) {
        Response response;
        Content content;
        String json;
        ObjectMapper objectMapper = new ObjectMapper();
        content = Fluent.post(Requests.getName(index).toString());
        json = content.asString();
        try {
            response = objectMapper.readValue(json, Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        log.info(response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response;
        Content content;
        String json;
        ObjectMapper objectMapper = new ObjectMapper();
        content = Fluent.post(Requests.getId(index).toString());
        json = content.asString();
        try {
            response = objectMapper.readValue(json, Response.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        log.info(response.result._id);
        return response.result._id;
    }


    public static void alone() {
    }

    public static void separate() {
    }

    public static void sync() {
    }

    public static void off() {
    }

    public static void high() {
    }

    public static void low() {
    }

    public static void volume(String player, String value) {
        log.info(player, value);
        Fluent.post(Requests.setVolume(player, value).toString());
    }

}
