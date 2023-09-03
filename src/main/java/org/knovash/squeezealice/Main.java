package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class Main {

    public static void main(String[] args) throws JsonProcessingException {


//        String syncgroups = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"\",[\"syncgroups\",\"?\"]]}";
//        String mode = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"\",[\"syncgroups\",\"?\"]]}";
//        String path = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"\",[\"syncgroups\",\"?\"]]}";
//        String volume = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"\",[\"syncgroups\",\"?\"]]}";
//        String play = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"\",[\"syncgroups\",\"?\"]]}";
//
//
//        log.info("START");
//        RequestData requestData = Requests.getMode;
//
//        System.out.println(requestData);
//        ObjectMapper objectMapper = new ObjectMapper();
//        String json = objectMapper.writeValueAsString(requestData);
//
//        System.out.println("JSON: " + json);
//
//
//        Fluent.post("{\"id\":1,\"method\":\"slim.request\",\"params\":[\"HomePod\",[\"current_title\",\"?\"]]}");
//
//        Content ccc = Fluent.post(requestData.toString());
//        log.info("CONTENT " + ccc);
//
//
//        Content ccccc = Fluent.post(Requests.getCount.toString());
//        log.info("CONTENT " + ccccc);

//        Player.count();
//        log.info(Player.counter);
//        Player.name("1");
//        Server server = new Server();
//        server.players();
//        log.info("\n\nSERVER.PLAYERS: \n" + server.serverPlayers);
//        server.serverPlayers.stream().forEach(player -> log.info("MODE: "+player.mode));

        System.out.printf("Hello and welcome!");
        Server2.serverrun();

    }

}
