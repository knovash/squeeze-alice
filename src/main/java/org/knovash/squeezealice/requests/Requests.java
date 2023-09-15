package org.knovash.squeezealice.requests;

public class Requests {

    public static RequestToLms count() {
        return RequestToLms.create("", new String[]{"player", "count", "?"});
    }

    public static RequestToLms name(String index) {
        return RequestToLms.create("", new String[]{"player", "name", index, "?"});
    }

    public static RequestToLms id(String index) {
        return RequestToLms.create("", new String[]{"player", "id", index, "?"});
    }

    public static RequestToLms mode(String player) {
        return RequestToLms.create(player, new String[]{"mode", "?"});
    }

    public static RequestToLms pause(String player) {
        return RequestToLms.create(player, new String[]{"pause", "1"});
    }

    public static RequestToLms play(String player) {
        return RequestToLms.create(player, new String[]{"pause", "0"});
    }

    public static RequestToLms play(String player, Integer id) {
        return RequestToLms.create(player, new String[]{"favorites", "playlist", "play", "item_id:" + id});
    }

    public static RequestToLms play(String player, String url) {
        return RequestToLms.create(player, new String[]{"playlist", "play", url});
    }

    public static RequestToLms volume(String player, String value) {
        return RequestToLms.create(player, new String[]{"mixer", "volume", value});
    }

    public static RequestToLms volume(String player) {
        return RequestToLms.create(player, new String[]{"mixer", "volume", "?"});
    }

    public static RequestToLms path(String player) {
        return RequestToLms.create(player, new String[]{"path", "?"});
    }

    public static RequestToLms syncgroups() {
        return RequestToLms.create("", new String[]{"syncgroups", "?"});
    }

    public static RequestToLms unsync(String player) {
        return RequestToLms.create(player, new String[]{"sync", "-"});
    }

    public static RequestToLms sync(String player, String head) {
        return RequestToLms.create(head, new String[]{"sync", player});
    }
}