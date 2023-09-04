package org.knovash.squeezealice.requests;

public class Requests {

    public static Request getCount() {
        return Request.create("", new String[]{"player", "count", "?"});
    }

    public static Request getName(String index) {
        return Request.create("", new String[]{"player", "name", index, "?"});
    }

    public static Request getId(String index) {
        return Request.create("", new String[]{"player", "id", index, "?"});
    }

    public static Request getMode(String player) {
        return Request.create(player, new String[]{"mode", "?"});
    }

    public static Request getPath(String player) {
        return Request.create(player, new String[]{"path", "?"});
    }

    public static Request getSyncgroups() {
        return Request.create("", new String[]{"syncgroups", "?"});
    }

    public static Request getPlaylistName = Request.create("HomePod", new String[]{"playlist", "name", "?"});
    public static Request getFavorites = Request.create("HomePod", new String[]{"favorites", "items", "0", "9", "want_url:1"});


    public static Request playerPlay(String player) {
        return Request.create(player, new String[]{"pause"}); // pause\play
    }

    public static Request playerPlay(String player, String path) {
        return Request.create(player, new String[]{"playlist", "play", path, ""}); // play path
    }

    public static Request playerPause(String player) {
        return Request.create(player, new String[]{"pause", "1"}); // pause
    }

    public static Request favoritePlay(String player, String id) {
        return Request.create(player, new String[]{"favorites", "playlist", "play", "item_id:" + id});
    }


    public static Request unsync(String player) {
        return Request.create(player, new String[]{"sync", "-"});
    }

    public static Request sync(String player, String head) {
        return Request.create(player, new String[]{"sync", head});
    }

    public static Request volumeset(String player, String value) {
        return Request.create(player, new String[]{"mixer", "volume", value});
    }

    public static Request volumeget(String player) {
        return Request.create(player, new String[]{"mixer", "volume", "?"});
    }
}