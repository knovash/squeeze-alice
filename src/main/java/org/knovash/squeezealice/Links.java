package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Links {

    private List<Link> links = new ArrayList<>();

    @Override
    public String toString() {
        return "Links{" +
                "links=" + links +
                '}';
    }

    public void addLinkRoom(String id, String idExt, String roomId, String roomName, String playerName) {
        Link link = new Link();
        link.id = id;
        link.idExt = idExt;
        link.roomId = roomId;
        link.roomName = roomName;
        link.playerName = playerName;
        int count = (int) links.stream()
                .filter(link1 -> link1.roomId.equals(roomId))
                .peek(link1 -> {
                    if (id != null) link1.id = id;
                    if (idExt != null) link1.idExt = idExt;
                    if (roomId != null) link1.roomId = roomId;
                    if (roomName != null) link1.roomName = roomName;
                    if (playerName != null) link1.playerName = playerName;
                }).count();
        if (count < 1) links.add(link);
    }

    public void addLinkPlayer(String roomName, String playerName) {
        Link link = new Link();
        link.id = null;
        link.idExt = null;
        link.roomId = null;
        link.roomName = roomName;
        link.playerName = playerName;
        log.info("-----------------" + roomName + "-----" + playerName);
//        обнулить старые связи комнат с этим плеером
        links.stream()
                .filter(link1 -> link1.playerName != null)
                .filter(link1 -> link1.playerName.equals(playerName))
                .forEach(link1 -> link1.playerName = null);
//        найти связь с такой комнатой и привязать этот плеер
        links.stream()
                .filter(link1 -> link1.roomName != null)
                .filter(link1 -> link1.roomName.equals(roomName))
                .peek(link1 -> log.info("#### " + link1))
                .forEach(link1 -> link1.playerName = playerName);

    }

    public void read() {
        log.info("READ LINKS");
        this.links = new ArrayList<>();
        Links linksFromFile = JsonUtils.jsonFileToPojo("links.json", Links.class);
        if (linksFromFile == null) {
            log.info("NO LINKS FILE");
        } else {
            this.links = linksFromFile.links;
//            log.info(this.links);
        }
    }

    public void write() {
        log.info("WRITE LINKS");
//        JsonUtils.pojoToJsonFile(this, "links.json");
    }


    public String playerNameByRoomName(String roomName) {
        Link link = this.links.stream().filter(l -> l.roomName.equals(roomName)).findFirst().orElse(null);
        if (link != null) return link.playerName;
        return null;
    }

    public String playerNameByIdExt(String idExt) {
        Link link = this.links.stream().filter(l -> l.idExt.equals(idExt)).findFirst().orElse(null);
        if (link != null) return link.playerName;
        return null;
    }

    public String playerNameById(String id) {
        Link link = this.links.stream().filter(l -> l.id.equals(id)).findFirst().orElse(null);
        if (link != null) return link.playerName;
        return null;
    }

}