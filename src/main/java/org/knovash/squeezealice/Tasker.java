package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.*;

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
        log.info(start);
        if (player == null) return null;
        log.info("TASKER SELECTED PLAYER " + player);
        log.info("CLEAR PLAYERS STATUS");
        lmsPlayers.players.forEach(p -> p.statusClear()); // TODO  public void statusClear() {
        log.info("UPDATE PLAYERS STATUS"); // TODO  public void resetPlayerStatus() {
        lmsPlayers.players.forEach(p -> p.status()); // статус каждого плеера потому что надо громкость каждого, сервер статус не дает громкость
        nowPlaying = player.title; // для виджета одной иконкой для телефона где неработает плагин
        nowPlayingTv = player.name + " - " + player.volume + " - " + player.mode + " - " + player.title; // для виджета одной иконкой для телефона где неработает плагин

        log.info("FOR TASKER PLAYLIST");
        forTaskerPlaylist(player, Integer.valueOf(lines)); // для виджета плейлиста
        lmsPlayers.players.forEach(p -> p.title()); // обновить титулы всех плееров
        log.info("FOR TASKER PLAYERS LIST");
        forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        log.info("FOR TASKER ICONS");
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
                "  \"NOWPLAYINGTV\": \"" + nowPlayingTv + "\",\n" +
                "  \"CURRENTPLAYER\": \"" + player.name + "\"\n" +
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

    public static String forTaskerPlaylist(Player player, Integer lines) {
        log.info(start);
        log.info("CREATE PLAYLIST FOR TASKER. ACTIVE PLAYER: " + player);
        int tracks = player.playlistTracksCurrentCount();
        if (tracks == 0) {
            log.info("PLAYLIST EMPTY");
            widgetPlaylist = "empty";
            return "empty";
        }
        List<String> playlist = player.playerStatus.result.playlist_loop.stream()
                .map(item -> (item.playlist_index + 1) + ". " + item.title)
                .collect(Collectors.toList());
        if (playlist.size() > 1) {
            playlist.replaceAll(t -> t.replaceAll(",", " ")); // удалить из названий символы , потому что в таскере , разделитель строк
            Integer index = Integer.parseInt(player.playerStatus.result.playlist_cur_index);
            log.info("PLAYLIST CURRENT INDEX: " + index);
            playlist.set(index, ">" + playlist.get(index)); // Заменяем элемент по конкретному индексу
            playlist = Utils.linesFromList(playlist, index, lines); // показывать только часть плейлиста вокруг играющего
        } else {
            playlist.replaceAll(t -> t.replaceAll("1.", ""));
        }
        String result = String.join(", ", playlist);
        log.info("PLAYLIST: " + playlist);
        widgetPlaylist = result;
        log.info(finish);
        return result;
    }

    public static String forTaskerPlayersList() {
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
        return playing + ";" + notPlaying;
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

    public static String ready() {
        log.info("READY: " + ready);
        if (ready == null) return "yes";
        return ready;
    }
}