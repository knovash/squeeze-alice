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
        log.info("----------UPDATE START---------");
        log.info("PLAYER " + player);
//        lmsPlayers.updatePlayers(); // пред обновлением виджетов таскера
        lmsPlayers.checkUpdated(); // TODO DEBUG
        lmsPlayers.players.stream().filter(p -> p.connected).forEach(p -> {
            p.volumeGet();
            p.title();
        });
        log.info("----------UPDATE FINISH---------");
        nowPlaying = player.title; // для виджета одной иконкой для телефона где неработает плагин
        log.info("1111");
        nowPlayingTv = player.name + " - " + player.volume + " - " + player.mode + " - " + player.title; // для виджета одной иконкой для телефона где неработает плагин

        log.info("2222   " + lines);
        log.info("++++   " + player);
        forTaskerPlaylist(player, Integer.valueOf(lines)); // для виджета плейлиста
        log.info("333");
        forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        log.info("444");
        forTaskerWidgetsIcons(); // для виджетов иконок плееров
        log.info("555");

        String responseJson = "{\n" +
                "  \"PLAYLIST\": \"" + widgetPlaylist + "\",\n" +
                "  \"PLAYERS_PLAY\": \"" + widgetPlayersPlay + "\",\n" +
                "  \"PLAYERS_STOP\": \"" + widgetPlayersStop + "\",\n" +
                "  \"ROOMSPLAYERS\": \"" + widgetsNames + "\",\n" +
                "  \"MODES\": \"" + widgetsModes + "\",\n" +
                "  \"SYNCS\": \"" + widgetsSyncs + "\",\n" +
                "  \"SEPARATES\": \"" + widgetsSeparates + "\",\n" +
                "  \"NOWPLAYING\": \"" + nowPlaying + "\",\n" +
                "  \"NOWPLAYINGTV\": \"" + nowPlayingTv + "\"\n" +
                "}";
        log.info(responseJson);
        return responseJson;
    }

    public static String playerNameByWidgetName(String value) {
        log.info("GET PLAYER BY WIDGET: " + value);
        Player player1 = null;
        String playerName = "ERROR";
        String roomName;
        roomName = Utils.getCorrectRoomName(value);
        if (roomName != null) player1 = lmsPlayers.playerByRoom(roomName);
        if (player1 != null) playerName = player1.name;
        if (player1 == null) {
            playerName = Utils.getCorrectPlayerName(value);
            if (playerName != null) roomName = lmsPlayers.playerByName(playerName).room;
        }
        log.info("ROOM: " + roomName + " PLAYER: " + playerName);
        String result = roomName + "," + playerName;
        return result;
    }

    public static String forTaskerPlaylist(Player player, Integer lines) {
        log.info("---REQUEST PLAYLIST ----");
        player.requestPlaylistTracks();
        log.info("----5555  " + player.playerStatus);
        List<String> playlist = player.playerStatus.result.playlist_loop.stream()
                .map(item -> (item.playlist_index + 1) + ". " + item.title)
                .collect(Collectors.toList());
        log.info("PLAYLIST " + playlist);
// удалить из названий символы , потому что в таскере , разделитель строк
        playlist.replaceAll(t -> t.replaceAll(",", " "));
        Integer index = Integer.parseInt(player.playerStatus.result.playlist_cur_index);
        log.info("PLAYLIST INDEX: " + index);
// Заменяем элемент по конкретному индексу
        playlist.set(index, ">" + playlist.get(index));
// показывать только часть плейлиста вокруг играющего
        playlist = Utils.linesFromList(playlist, index, lines);
        String result = String.join(", ", playlist);
        log.info("PLAYLIST: " + playlist);
        widgetPlaylist = result;
        return result;
    }

    public static String forTaskerPlayersList() {
        log.info("TASKER PLAYERS LIST");
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

}