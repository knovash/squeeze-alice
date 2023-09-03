package org.knovash.squeezealice;

public class Requests {

    public static RequestData getCount = RequestUtils.create("", new String[]{"player", "count", "?"});
    public static RequestData getMode = RequestUtils.create("HomePod", new String[]{"mode", "?"});

    public static RequestData getName(String index) {
        return RequestUtils.create("", new String[]{"player", "name", index, "?"});
    }

    public static RequestData getId(String index) {
        return RequestUtils.create("", new String[]{"player", "id", index, "?"});
    }


    public static RequestData getPath = RequestUtils.create("HomePod", new String[]{"path", "?"});
    public static RequestData getVolume = RequestUtils.create("HomePod", new String[]{"mixer", "volume", "?"});
    public static RequestData getSyncgroups = RequestUtils.create("", new String[]{"syncgroups", "?"});
    public static RequestData getPlaylistName = RequestUtils.create("HomePod", new String[]{"playlist", "name", "?"});
    public static RequestData getFavorites = RequestUtils.create("HomePod", new String[]{"favorites", "items", "0", "9", "want_url:1"});


    public static RequestData playerPausePlay = RequestUtils.create("HomePod", new String[]{"pause"});
    public static RequestData playerPlayPath = RequestUtils.create("HomePod", new String[]{"playlist", "play", "path", ""});

    public static RequestData playerUnSync(String player) {
        return RequestUtils.create(player, new String[]{"sync", "-"});
    }

    public static RequestData playerSync(String player, String head) {
        return RequestUtils.create(player, new String[]{"sync", head});
    }

    public static RequestData setVolume(String player, String value) {
        return RequestUtils.create(player, new String[]{"mixer", "volume", value});
    }
}

/**
 * request samples
 * {"id": 1, "method": "slim.request", "params":["", ["player", "count", "?"]]}
 * {"id": 1, "method": "slim.request", "params":["HomePod", ["mode", "?"]]}
 * {"id": 1, "method": "slim.request", "params":["HomePod", ["path", "?"]]}
 * {"id": 1, "method": "slim.request", "params":["HomePod", ["mixer", "volume", "?"]]}
 * {"id": 1, "method": "slim.request", "params":["HomePod", ["mixer", "volume", "2"]]}
 */