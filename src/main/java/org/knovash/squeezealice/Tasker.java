package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;

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

    public static String forTaskerWidgetsRefreshJson(Player player, String lines) {
        log.info("REQUEST PLAYERS VOLUME AND TITLE. START");
        log.info("PLAYER " + player);
//        lmsPlayers.updatePlayers(); // пред обновлением виджетов таскера
        lmsPlayers.checkUpdated(); // TODO DEBUG
        lmsPlayers.players.stream().filter(p -> p.connected).forEach(p -> {
            p.volumeGet(); // получить для каждого плеера громкость
            p.title(); // получить для каждого плеера титул
        });
        log.info("REQUEST PLAYERS VOLUME AND TITLE. FINISH");
        nowPlaying = player.title; // для виджета одной иконкой для телефона где неработает плагин
        nowPlayingTv = player.name + " - " + player.volume + " - " + player.mode + " - " + player.title; // для виджета одной иконкой для телефона где неработает плагин

        forTaskerPlaylist(player, Integer.valueOf(lines)); // для виджета плейлиста
        forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        forTaskerWidgetsIcons(); // для виджетов иконок плееров

        String responseJson = "{\n" +
                "  \"PLAYLIST\": \"" + widgetPlaylist + "\",\n" +
                "  \"PLAYERS_PLAY\": \"" + widgetPlayersPlay + "\",\n" +
                "  \"PLAYERS_STOP\": \"" + widgetPlayersStop + "\",\n" +
                "  \"ROOMSPLAYERS\": \"" + widgetsNames + "\",\n" +
                "  \"MODES\": \"" + widgetsModes + "\",\n" +
                "  \"SYNCS\": \"" + widgetsSyncs + "\",\n" +
                "  \"SEPARATES\": \"" + widgetsSeparates + "\",\n" +
                "  \"NOWPLAYING\": \"" + nowPlaying + "\",\n" +
                "  \"NOWPLAYINGTV\": \"" + nowPlayingTv + "\",\n" + // незабыть запятую
                "  \"CURRENTPLAYER\": \"" + player.name + "\"\n" +
                "}";
        log.info(responseJson);
        return responseJson;
    }

    public static String playerNameByWidgetName(String value) {
        log.info("GET PLAYER BY WIDGET: " + value);
        String playerName = null;
        String roomName = null;

        Player player = lmsPlayers.playerByNearestName(value);
        if (player == null) player = lmsPlayers.playerByNearestRoom(value);
        if (player == null) return null;

        roomName = player.room;
        playerName = player.name;

        log.info("ROOM: " + roomName + " PLAYER: " + player.name);
        String result = roomName + "," + playerName;
        return result;
    }

    public static String forTaskerPlaylist(Player player, Integer lines) {
        log.info("CREATE PLAYLIST FOR TASKER. ACTIVE PLAYER: " + player);
        player.requestPlaylistTracks();
        List<String> playlist = player.playerStatus.result.playlist_loop.stream()
                .map(item -> (item.playlist_index + 1) + ". " + item.title)
                .collect(Collectors.toList());
        playlist.replaceAll(t -> t.replaceAll(",", " ")); // удалить из названий символы , потому что в таскере , разделитель строк
        Integer index = Integer.parseInt(player.playerStatus.result.playlist_cur_index);
        log.info("PLAYLIST CURRENT INDEX: " + index);
        playlist.set(index, ">" + playlist.get(index)); // Заменяем элемент по конкретному индексу
        playlist = Utils.linesFromList(playlist, index, lines); // показывать только часть плейлиста вокруг играющего
        String result = String.join(", ", playlist);
        log.info("PLAYLIST: " + playlist);
        widgetPlaylist = result;
        return result;
    }

    public static String forTaskerPlaylistFull(Player player) {
        log.info("CREATE PLAYLIST FOR TASKER. ACTIVE PLAYER: " + player);
        player.requestPlaylistTracks();
        List<String> playlist = player.playerStatus.result.playlist_loop.stream()
                .map(item -> (item.playlist_index + 1) + ". " + item.title)
                .collect(Collectors.toList());
        playlist.replaceAll(t -> t.replaceAll(",", " ")); // удалить из названий символы , потому что в таскере , разделитель строк
        Integer index = Integer.parseInt(player.playerStatus.result.playlist_cur_index);
        log.info("PLAYLIST CURRENT INDEX: " + index);
        playlist.set(index, ">" + playlist.get(index)); // Заменяем элемент по конкретному индексу
        String result = String.join(", ", playlist);
        log.info("PLAYLIST: " + playlist);
        widgetPlaylist = result;
        return result;
    }

    public static String forTaskerPlayersList() {
        log.info("CREATE PLAYERS LIST FOR TASKER");
        lmsPlayers.checkUpdated(); // TODO DEBUG

        Function<Player, String> formatter = p -> {
            String name = (p.room != null) ? p.room + " - " + p.name : p.name;
            String vol = (p.volume != null) ? p.volume : "-";
            if (!p.connected) return name + " - offline";
            return name + " - " + vol + " - " + p.title;
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
        return playing + ";" + notPlaying;
    }

    public static void forTaskerWidgetsIcons() {
        log.info("CREATE ICONS DATA FOR TASKER");
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
    }

    public static String ready() {
        log.info("READY: " + ready);
        if (ready == null) return "yes";
        return ready;
    }
}