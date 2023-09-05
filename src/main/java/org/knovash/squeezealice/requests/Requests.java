package org.knovash.squeezealice.requests;

public class Requests {

    public static Request count() {
        return Request.create("", new String[]{"player", "count", "?"});
    }

    public static Request name(String index) {
        return Request.create("", new String[]{"player", "name", index, "?"});
    }

    public static Request id(String index) {
        return Request.create("", new String[]{"player", "id", index, "?"});
    }

    public static Request mode(String player) {
        return Request.create(player, new String[]{"mode", "?"});
    }

    public static Request pause(String player) {
        return Request.create(player, new String[]{"pause", "1"});
    }

    public static Request play(String player, Integer id) {
        return Request.create(player, new String[]{"favorites", "playlist", "play", "item_id:" + id});
    }

    public static Request play(String player, String url) {
        return Request.create(player, new String[]{"playlist", "play", url});
    }

    public static Request volume(String player, String value) {
        return Request.create(player, new String[]{"mixer", "volume", value});
    }

    public static Request volume(String player) {
        return Request.create(player, new String[]{"mixer", "volume", "?"});
    }

    public static Request path(String player) {
        return Request.create(player, new String[]{"path", "?"});
    }

    public static Request syncgroups() {
        return Request.create("", new String[]{"syncgroups", "?"});
    }

    public static Request unsync(String player) {
        return Request.create(player, new String[]{"sync", "-"});
    }

    public static Request sync(String player1, String player2) {
        return Request.create(player1, new String[]{"sync", player2});
    }
}