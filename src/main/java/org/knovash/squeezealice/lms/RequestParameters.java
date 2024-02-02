package org.knovash.squeezealice.lms;

public class RequestParameters {

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

    public static Request play_pause(String player) {
        return Request.create(player, new String[]{"pause"});
    }

    public static Request play(String player) {
        return Request.create(player, new String[]{"pause", "0"});
    }

    public static Request play(String player, Integer id) {
        return Request.create(player, new String[]{"favorites", "playlist", "play", "item_id:" + id});
    }

    public static Request play(String player, String url) {
        return Request.create(player, new String[]{"playlist", "play", url});
    }

    public static Request favorites(String player) {
        return Request.create(player, new String[]{"favorites", "items", "0", "10" + "want_url:1"});
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

    public static Request playlistname(String player) {
        return Request.create(player, new String[]{"playlist", "name", "?"});
    }

    public static Request albumname(String player) {
        return Request.create(player, new String[]{"playlist", "album", "?"});
    }

    public static Request trackname(String player) {
        return Request.create(player, new String[]{"playlist", "title", "?"});
    }

    public static Request artistname(String player) {
        return Request.create(player, new String[]{"artist", "?"});
    }

    public static Request syncgroups() {
        return Request.create("", new String[]{"syncgroups", "?"});
    }

    public static Request unsync(String player) {
        return Request.create(player, new String[]{"sync", "-"});
    }

    public static Request sync(String player, String head) {
        return Request.create(head, new String[]{"sync", player});
    }

    public static Request shuffleon(String player) {
        return Request.create(player, new String[]{"playlist", "shuffle", "1"});
    }

    public static Request shuffleoff(String player) {
        return Request.create(player, new String[]{"playlist", "shuffle", "0"});
    }

    public static Request prevtrack(String player) {
        return Request.create(player, new String[]{"playlist", "jump", "-1"});
    }

    public static Request nexttrack(String player) {
        return Request.create(player, new String[]{"playlist", "jump", "+1"});
    }
}