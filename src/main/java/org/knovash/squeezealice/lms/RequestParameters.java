package org.knovash.squeezealice.lms;

import java.util.Arrays;
import java.util.List;

public class RequestParameters {

    public static RequestToLms count() {
        return RequestToLms.create("", new String[]{"player", "count", "?"});
    }

    public static RequestToLms searchSpotifyArtist(String artistName) {
        return RequestToLms.create("", new String[]{"search", "artist:" + artistName, "20", "score"});
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

    public static RequestToLms togglePlayPause(String player) {
        return RequestToLms.create(player, new String[]{"pause"});
    }

    public static RequestToLms play(String player) {
        return RequestToLms.create(player, new String[]{"pause", "0"});
    }

    public static RequestToLms playFavoritesId(String player, Integer id) { // id начинается с 0
        return RequestToLms.create(player, new String[]{"favorites", "playlist", "play", "item_id:" + id});

    }

    public static RequestToLms play(String player, String url) {
        return RequestToLms.create(player, new String[]{"playlist", "play", url});
    }

    public static RequestToLms playFile(String player, String file) {
        return RequestToLms.create(player, new String[]{"playlist", "play", file});
    }

    public static RequestToLms playlistClear(String player, String file) {
        return RequestToLms.create(player, new String[]{"playlist", "clear"});
    }

    public static RequestToLms favorites(String player, String value) {
        return RequestToLms.create(player, new String[]{"favorites", "items", "0", "100"});
    }

    public static RequestToLms favoritesAdd(String player, String url, String title) {
//        {"id":"1","method":"slim.request","params":["",["favorites","add","url:http://prem2.di.fm/chillout_hi?78------7ceab43a2","title:SSSSS"]]}
        return RequestToLms.create(player, new String[]{"favorites", "add", "url:" + url, "title:" + title});
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

    public static RequestToLms playlistClear(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "clear"});
    }

    //--------------------
    public static RequestToLms playlistSave(String player, String playlistName) {
        return RequestToLms.create(player, new String[]{"playlist", "save", playlistName});
//        return RequestToLms.create(player, new String[]{"playlist", "save", player + "_restore"});
    }

    public static RequestToLms playlistRestore(String player, String playlistName) {
        return RequestToLms.create(player, new String[]{"playlist", "load", playlistName});
//        return RequestToLms.create(player, new String[]{"playlist", "load", player + "_restore"});
    }

    public static RequestToLms playlistRename(String player, String nameOld, String nameNew) {
        return RequestToLms.create(player, new String[]{"playlist", "rename", nameOld, nameNew});
    }

    public static RequestToLms playlistIndexSet(String player, String index) {
        return RequestToLms.create(player, new String[]{"playlist", "index", index});
    }

    public static RequestToLms playlistTimeSet(String player, String time) {
        return RequestToLms.create(player, new String[]{"time", time});
    }

    public static RequestToLms playlistModeSet(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "clear"});
    }


    public static RequestToLms playlisturl(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "url", "?"});
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

    public static RequestToLms shuffleOn(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "shuffle", "1"});
    }

    public static RequestToLms shuffleOff(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "shuffle", "0"});
    }

    public static RequestToLms repeatOn(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "repeat", "1"});
    }

    public static RequestToLms repeatOff(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "repeat", "0"});
    }

    public static RequestToLms prevtrack(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "jump", "-1"});
    }

    public static RequestToLms nexttrack(String player) {
        return RequestToLms.create(player, new String[]{"playlist", "jump", "+1"});
    }


    public static RequestToLms forward(String player) {
        return RequestToLms.create(player, new String[]{"time", "+" + 20});
    }

    public static RequestToLms rewind(String player) {
        return RequestToLms.create(player, new String[]{"time", "-" + 20});
    }

    public static RequestToLms track(String player, String track) {
        return RequestToLms.create(player, new String[]{"playlist", "jump", track});
    }

    public static RequestToLms tracks(String player) {
//        {"id": 1, "method": "slim.request", "params":["HomePod", ["playlist", "tracks", "?"]]}
        return RequestToLms.create(player, new String[]{"playlist", "tracks", "?"});
    }

    public static RequestToLms serverstatusname() {
//        {"id": 1, "method": "slim.request", "params":["HomePod2", ["status", "1", "1"]]}
//        "player count": 5,
        return RequestToLms.create("", new String[]{"serverstatus", "name"});
    }

    public static RequestToLms status(String player, Integer tracks) {
//        {"id": 1, "method": "slim.request", "params":["HomePod2", ["status", "1", "1"]]}
        return RequestToLms.create(player, new String[]{"status", "0", String.valueOf(tracks)});
    }
}