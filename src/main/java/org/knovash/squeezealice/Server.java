package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
//@NoArgsConstructor
@AllArgsConstructor
public class Server {



    public Server() {

    }
    public List<Player> serverPlayers;
    public void players() {

        log.info("SERVER");
        Player.count();
        Integer counter = Player.counter;
        Player player = new Player();
        List<Player> players = new ArrayList<>();

//        players.add(player);

        for (Integer i = 0; i < counter; i++) {

            String name = Player.name(i.toString());
            String id = Player.id(i.toString());
            log.info("INDEX: " + i + " NAME: " + name + " ID: " + id);
//            Player newplayer = new Player("ddd","rrr");
//            players.add(newplayer);
            players.add(new Player(name, id));

            log.info("\nPLAYERS:\n" + players);
        }

        log.info("\nPLAYERS:\n" + players);

        this.serverPlayers = players;

    }


    public List<Player> getServerPlayers() {
        return serverPlayers;
    }

    public void setServerPlayers(List<Player> serverPlayers) {
        this.serverPlayers = serverPlayers;
    }
}
