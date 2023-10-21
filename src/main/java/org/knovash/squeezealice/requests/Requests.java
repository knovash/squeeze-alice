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

    public static RequestToLms play_pause(String player) {
        return RequestToLms.create(player, new String[]{"pause"});
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

    public static RequestToLms playlistname(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "name", "?"});
    }

    public static RequestToLms albumname(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "album", "?"});
    }

    public static RequestToLms trackname(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "title", "?"});
    }

    public static RequestToLms artistname(String player) {
        return RequestToLms.create(player, new String[]{"artist", "?"});
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

    public static RequestToLms shuffleon(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "shuffle", "1"});
    }

    public static RequestToLms shuffleoff(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "shuffle", "0"});
    }

    public static RequestToLms prevtrack(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "jump", "-1"});
    }

    public static RequestToLms nexttrack(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "jump", "+1"});
    }
}