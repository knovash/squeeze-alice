package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.Main.rooms;

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

    public static String playerNameByWidgetName(String value) {
        log.info("GET PLAYER BY WIDGET: " + value);
        Player player1 = null;
        String playerName = "ERROR";
        String roomName;
        roomName = Utils.getCorrectRoomName(value);
        if (roomName != null) player1 = lmsPlayers.playerByCorrectRoom(roomName);
        if (player1 != null) playerName = player1.name;
        if (player1 == null) {
            playerName = Utils.getCorrectPlayerName(value);
            if (playerName != null) roomName = lmsPlayers.playerByCorrectName(playerName).room;
        }
        log.info("ROOM: " + roomName + " PLAYER: " + playerName);
        String result = roomName + "," + playerName;
        return result;
    }


    public static String forTaskerPlayersList() {
        log.info("TASKER PLAYERS LIST");
        List<String> favList = lmsPlayers.players.get(0).favorites(); // forTaskerPlayersList

        Function<Player, String> playerFormatter = p -> {
            if (!p.connected) p.playlistNameShort = "unknown";
            if (!p.connected) p.playlistNameShort = "offline";
            String playlistName = p.requestPlaylistName(); // forTaskerPlayersList -------------------------------------
            String favoritesIndex = "";
            if (playlistName != null && favList.contains(playlistName))
                favoritesIndex = (favList.indexOf(playlistName) + 1) + ".";
            if (playlistName == null) playlistName = p.requestTitle(); // --------------------------------------
            else playlistName = p.playlistNameShort; // forTaskerPlayersList
            String roomNamePlayerName = p.room + " - " + p.name;
            if (p.room == null) roomNamePlayerName = p.name;
//            log.info(roomNamePlayerName + " - " + p.volume + " - " + favoritesIndex + playlistName);
            return roomNamePlayerName + " - " + p.volume + " - " + favoritesIndex + playlistName;
        };

        log.info("\nPLAYING PLAYERS LIST:");
        widgetPlayersPlay = lmsPlayers.players.stream()
                .filter(p -> p.playing)
                .sorted(Comparator.comparing(
                        p -> p.playlistNameShort,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(playerFormatter)
                .collect(Collectors.joining(","));
        if (widgetPlayersPlay.isEmpty()) widgetPlayersPlay = "all players stop";
        log.info("\nNOT PLAYING PLAYERS LIST:");
        widgetPlayersStop = lmsPlayers.players.stream()
                .filter(p -> !p.playing)
                .sorted(Comparator.comparing(
                        p -> p.playlistNameShort,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .map(playerFormatter)
                .collect(Collectors.joining(","));
        if (widgetPlayersStop.isEmpty()) widgetPlayersStop = "all players play";
        log.info("RESULT PLAYING PLAYERS LIST: " + widgetPlayersPlay);
        log.info("RESULT NOT PLAYING PLAYERS LIST: " + widgetPlayersStop);
        String result = widgetPlayersPlay + ";" + widgetPlayersStop;
        return result;
    }

    public static String forTaskerWidgetsRefreshJson(Player player, String lines) {
        log.info("\nSTART TASKER WIDGETS REFRESH JSON");
        log.info("\nTASKER WIDGET PLAYERS LIST");
        forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        log.info("\nTASKER WIDGET ICONS");
        forTaskerWidgetsIcons(); // для виджетов иконок плееров
        String responsePlaylist = "[]";
        nowPlaying = "offline";
        if (player != null) {
            log.info("\nTASKER WIDGET PLAY LIST FOR " + player.name);
            responsePlaylist = player.forTaskerPlaylist(lines); // для виджета плейлиста
            if (responsePlaylist == null) {
//                log.info("--- responsePlaylist = player.playlistNameShort;");
//                log.info("--- PLAYLIST NAME SHORT: " + player.playlistNameShort);
                responsePlaylist = player.playlistNameShort;
            }
            log.info("\nTASKER WIDGET ONE ICON NOW PLAYING");
//            log.info("--- LmsPlayers.nowPlaying = player.playlistNameShort");
//            log.info("PLAYLIST NAME SHORT: " + player.playlistNameShort);
//            log.info("title: " + player.title);
//            log.info("playlistName: " + player.playlistName);
//            log.info("playlistNameShort: " + player.playlistNameShort);
            String title = player.playlistNameShort;
            if(player.playlistNameShort == null) title = player.requestTitle();
            nowPlaying = title; // для виджета одной иконкой для телефона где неработает плагин
            nowPlayingTv = player.name + " - " + player.volume + " - " + player.mode + " - " + title; // для виджета одной иконкой для телефона где неработает плагин
        }

//        log.info("\nTASKER WIDGET PLAYERS LIST");
//        this.forTaskerPlayersList(); // для виджета списка плееров name-volume-mode-title
        String responseJson = "{\n" +
                "  \"PLAYLIST\": \"" + responsePlaylist + "\",\n" +
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

    public static void forTaskerWidgetsIcons() {
        lmsPlayers.syncgroups();
        List<String> iconsNames = new ArrayList<>(rooms);
        iconsNames.addAll(lmsPlayers.players.stream().map(p -> p.name).collect(Collectors.toList()));
        List<String> modes = new ArrayList<>();
        List<String> syncs = new ArrayList<>();
        List<String> separates = new ArrayList<>();
        iconsNames.forEach(name -> {
            Player player = lmsPlayers.playerByCorrectRoom(name);
            if (player == null) player = lmsPlayers.playerByCorrectName(name);
            modes.add(player != null ? player.mode : "null"); // forTaskerWidgetsIcons
            syncs.add(player != null ? String.valueOf(player.sync) : "false");
            separates.add(player != null ? String.valueOf(player.separate) : "null");
        });
        log.info("NAMES: " + iconsNames);
        log.info("MODES: " + modes);
        log.info("SYNCS:" + syncs);
        log.info("SEPARATES: " + separates);
        widgetsNames = String.join(",", iconsNames);
        widgetsModes = String.join(",", modes);
        widgetsSyncs = String.join(",", syncs);
        widgetsSeparates = String.join(",", separates);
    }


}