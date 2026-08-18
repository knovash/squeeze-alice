package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.SettingsTasker.KeyString.*;
import static org.knovash.squeezealice.web.SettingsTasker.KeyToggle.*;
import static org.knovash.squeezealice.web.SettingsTasker.*;

@Log4j2
public class Tasker {

    public static String widgetsNames;
    public static String widgetsModes;
    public static String widgetsSyncs;
    public static String widgetsSeparates;
    public static String nowPlaying;
    public static String nowPlayingTv;
    public static String widgetPlayersPlay;
    public static String widgetPlayersStop;
    public static String widgetPlaylist;
    public static String ready;
    public static String taskerIp;

    public static String forTaskerWidgetsRefreshJson(Player player, String lines) {
        log.info(start);

        log.info("TASKER SELECTED PLAYER " + player);
        log.info("CLEAR PLAYERS STATUS");
        lmsPlayers.players.forEach(p -> p.statusClear()); // TODO  public void statusClear() {
        log.info("UPDATE PLAYERS STATUS"); // TODO  public void resetPlayerStatus() {
        lmsPlayers.players.forEach(p -> p.status()); // статус каждого плеера потому что надо громкость каждого, сервер статус не дает громкость


        log.info("FOR TASKER PLAYERS LIST");
        lmsPlayers.players.forEach(p -> p.title()); // обновить титулы всех плееров
        forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        log.info("FOR TASKER ICONS");
        forTaskerWidgetsIcons(); // для виджетов иконок плееров


        String playerName = "unknown";
        String playerVolume = "unknown";
        String room = "null";

        if (player != null) {
            nowPlaying = player.title; // для виджета одной иконкой для телефона где неработает плагин
            nowPlayingTv = player.name + " - " + player.volume + " - " + player.mode + " - " + player.title; // для виджета одной иконкой для телефона где неработает плагин
            log.info("FOR TASKER PLAYLIST");
            widgetPlaylist = forTaskerPlaylist(player, Integer.valueOf(lines), false); // для виджета плейлиста
            if (player.room != null) room = player.room;
            playerName = player.room;
            playerVolume = player.volume;
        } else {
            widgetPlaylist = "no player in LMS";
        }

        String responseJson = "{\n" +
                "  \"PLAYLIST\": \"" + widgetPlaylist + "\",\n" +
                "  \"PLAYERS_PLAY\": \"" + widgetPlayersPlay + "\",\n" +
                "  \"PLAYERS_STOP\": \"" + widgetPlayersStop + "\",\n" +
                "  \"ROOMSPLAYERS\": \"" + widgetsNames + "\",\n" +
                "  \"MODES\": \"" + widgetsModes + "\",\n" +
                "  \"SYNCS\": \"" + widgetsSyncs + "\",\n" +
                "  \"SEPARATES\": \"" + widgetsSeparates + "\",\n" +
                "  \"NOWPLAYING\": \"" + nowPlaying + "\",\n" +
                "  \"NOWPLAYINGTV\": \"" + nowPlayingTv + "\",\n" +
                "  \"CURRENTVOLUME\": \"" + playerVolume + "\",\n" +
                "  \"CURRENTPLAYER\": \"" + playerName + "\",\n" +
                "  \"CURRENTROOM\": \"" + room + "\"\n" +
                "}";
        log.info(responseJson);
        log.info(finish);
        return responseJson;
    }

    public static String playerNameByWidgetName(String value) {
        String name = null;
        Player player = lmsPlayers.playerByPlayerNameOrRoomName(value, value);
        if (player != null) name = player.name;
        return name;
    }

    public static String forTaskerPlaylist(Player player, Integer lines, boolean requestPlaylist) {
        // TODO если запрос из таскер надо сделать запрос playlist_cur_index
        // получить плейлист playlist_loop плеера player
        // если плейлист пустой вернуть empty
        // добавить нумерацию
        // добавить > к текущей позиции
        // удалить , из названий
        log.info(start);
        log.info("CREATE PLAYLIST FOR TASKER. ACTIVE PLAYER: " + player);
        if (requestPlaylist) {
            player.status();
        }
        int tracks = 0;
        String playlistTitle = "";
        if (player.playerStatus != null && player.playerStatus.result != null && player.playerStatus.result.playlist_loop != null) {
            tracks = player.playerStatus.result.playlist_loop.size();
        }
        log.info("TRACKS: " + tracks);
        if (tracks == 0) {
            log.info("TASKER PLAYLIST EMPTY");
            log.info(finish);
            return "empty";
        }
        List<String> playlist = null;
        if (tracks > 1) {
            playlist = player.playerStatus.result.playlist_loop.stream()
                    .map(item -> (item.playlist_index + 1) + ". " + item.title)
                    .collect(Collectors.toList());
            playlist.replaceAll(t -> t.replaceAll(",", " ")); // удалить из названий символы , потому что в таскере , разделитель строк
            Integer index = Integer.parseInt(player.playerStatus.result.playlist_cur_index);
            log.info("PLAYLIST CURRENT INDEX: " + index);
            playlist.set(index, ">" + playlist.get(index)); // Заменяем элемент по конкретному индексу
            playlist = Utils.linesFromList(playlist, index, lines); // показывать только часть плейлиста вокруг играющего
        } else {
            log.info("PLAYLIST TITLE: " + player.title);

            playlist = player.playerStatus.result.playlist_loop.stream()
                    .map(item -> item.title)
                    .collect(Collectors.toList());

            if (!player.titleCrop(player.title).equals(playlist.get(0))) {
                playlistTitle = player.titleCrop(player.title);
                playlist.set(0, playlistTitle + " - " + playlist.get(0));
            }

            playlist.replaceAll(t -> t.replaceAll(",", " ")); // удалить из названий символы , потому что в таскере , разделитель строк
        }


        String result = String.join(", ", playlist);
        log.info("PLAYLIST: " + playlist);
        log.info(finish);
        return result;
    }

    public static void forTaskerPlayersList() {
        log.info(start);
        Function<Player, String> formatter = p -> {
            String remote = "";
            if (p.name.equals(lmsPlayers.btPlayerName)) {
                remote = " - R";
            }
            String name = (p.room != null) ? p.room + " - " + p.name : p.name;
            String vol = (p.volume != null) ? p.volume : "-";
            if (!p.connected) return name + " - offline";
            return name + remote + " - " + vol + " - " + p.title;
        };

        Comparator<Player> byTitle = Comparator.comparing(player -> player.title, Comparator.nullsLast(Comparator.naturalOrder()));

        String playing = lmsPlayers.players.stream() // Играющие плееры
                .filter(p -> p.playing)
                .sorted(byTitle)
                .map(formatter)
                .collect(Collectors.joining(","));
        if (playing.isEmpty()) playing = "all players stop";

        String notPlaying = lmsPlayers.players.stream() // Неиграющие плееры
                .filter(p -> !p.playing)
                .sorted(byTitle)
                .map(formatter)
                .collect(Collectors.joining(","));
        if (notPlaying.isEmpty()) notPlaying = "all players play";
        log.info("Playing: " + playing);
        log.info("Not playing: " + notPlaying);
        widgetPlayersPlay = playing;
        widgetPlayersStop = notPlaying;
        log.info(finish);
    }

    public static void forTaskerWidgetsIcons() {
        log.info(start);
        lmsPlayers.syncgroups();
        List<String> iconsNames = new ArrayList<>(Yandex.rooms);
        iconsNames.addAll(lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
        List<String> modes = new ArrayList<>();
        List<String> syncs = new ArrayList<>();
        List<String> separates = new ArrayList<>();
        iconsNames.forEach(name -> {
            Player player = lmsPlayers.playerByRoom(name);
            if (player == null) player = lmsPlayers.playerByName(name);
            modes.add(player != null ? player.mode : "null");
            syncs.add(player != null ? String.valueOf(player.sync) : "false");
            separates.add(player != null ? String.valueOf(player.separate) : "null");
        });
        widgetsNames = String.join(",", iconsNames);
        widgetsModes = String.join(",", modes);
        widgetsSyncs = String.join(",", syncs);
        widgetsSeparates = String.join(",", separates);
        log.info(finish);
    }

    public static String ready() { // для варианта когда Таскер пулит запросы в ожидании разрешения обновления
        log.info("READY: " + ready);
        if (ready == null) return "yes";
        return ready;
    }

    public static void sendRequestToTaskerRunRefreshAsync(String playerName, String roomName) { // для варианта когда сервер делает запрос в Таскер команду на обновление
        if (!TASKER_TOGGLE_REFRESH.value) {
            log.info("TOGGLE TASKER REFRESH STATES DISABLED !!!");
            return;
        }
        log.info(start);
        HttpClientWrapper httpClient = new HttpClientWrapper();
        URI uri = null;
        try {
            uri = new URI("http", null, TASKER_STRING_IP.value,
                    Integer.parseInt(TASKER_STRING_PORT.value), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("pleer", playerName);
        jsonBody.put("room", roomName);
        String body = jsonBody.toString();
        log.info("TASKER REQUEST REFRESH" + uri + jsonBody);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        URI finalUri = uri;
        CompletableFuture.runAsync(() -> httpClient.doPost(finalUri.toString(), body, headers));
        log.info(finish);
    }

    public static void taskerIpSet(String body) {
        TASKER_STRING_IP.value = "";
        TASKER_STRING_PORT.value = "";
        if (body == null || body.trim().isEmpty()) return;
        try {
            JSONObject json = new JSONObject(body);
            if (json.has("ip")) {
                TASKER_STRING_IP.value = json.getString("ip");
            }
            if (json.has("port")) {
                Object portValue = json.get("port");
                TASKER_STRING_PORT.value = portValue.toString();
            }
            settingsTasker.saveToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}