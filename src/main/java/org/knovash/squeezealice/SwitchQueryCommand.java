package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.web.Html;

import java.util.HashMap;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(Context context) {
        HashMap<String, String> queryParams = context.queryMap;
        log.info("QUERY: " + queryParams);
        String response;
        context.json = "BAD REQUEST NO ACTION IN QUERY";
        if (!queryParams.containsKey("action")) return context;
        context.code = 200;

        String action = queryParams.get("action");
        log.info("ACTION: " + action);
        Player player = lmsPlayers.getPlayerByNameInQuery(queryParams.get("player"));
        String value = queryParams.get("value");
        switch (action) {
            case ("channel"): // TASKER
                Actions.channel(player, Integer.valueOf(value));
                response = "CHANNEL COMPLETE";
                break;
            case ("volume"): // TASKER
                Actions.volumeByQuery(player, value);
                response = "VOLUME COMPLETE";
                break;
            case ("all_low_high"): // TASKER
                Actions.allLowOrHigh(value);
                response = "PRESET COMPLETE";
                break;
            case ("turn_on_music"): // TASKER
                Actions.turnOnMusicSpeaker(player);
                response = "MUSIC ON COMPLETE";
                break;
            case ("turn_off_music"): // TASKER
                Actions.turnOffMusic();
                response = "MUSIC OFF COMPLETE";
                break;
            case ("turn_off_speaker"): // TASKER
                Actions.turnOffSpeaker(player);
                response = "SPEAKER OFF COMPLETE";
                break;
            case ("toggle_music"): // TASKER
                response = Actions.toggleMusic(player);
                break;
            case ("turn_on_spotify"): // TASKER
                log.info("SPOTIFY");
                Actions.turnOnSpotify(player);
                response = "SPOTIFY COMPLETE";
                break;
            case ("log"): // WEB HOME
                log.info("SHOW LOG");
                response = Utils.logLastLines(queryParams);
                break;
            case ("silence"): // TASKER
                log.info("SILENCE");
                player.playSilence();
                response = "SILENCE COMPLETE";
                break;
            case ("change_value"): // TASKER
                log.info("CHANGE PLAYER VALUE");
                Utils.changePlayerValue(queryParams);
                response = "VALUE COMPLETE";
                break;
            case ("state_devices"):
                log.info("STATE ALICE DEVICES");
                response = Utils.stateDevices();
                break;
            case ("state_players"):
                log.info("STATE LMS PLAYERS");
                response = Utils.statePlayers();
                break;
            case ("time_volume_get"):
                log.info("SEND TIME AND VOLUME");
                response = Utils.timeVolumeGet(player);
                break;
            case ("time_volume_set"):
                log.info("CHANGE TIME AND VOLUME");
                response = Utils.timeVolumeSet(player, queryParams);
                break;
            case ("time_volume_del"):
                log.info("DELETE TIME AND VOLUME");
                response = Utils.timeVolumeDel(player, queryParams);
                break;
            case ("cred_spotify"):
                log.info("CREDENTIALS SPOTIFY");
                Spotify.credentialsSpotify(queryParams);
                response = Html.formSpotifyLogin();
                break;
            case ("cred_yandex"):
                log.info("CREDENTIALS YANDEX");
                Yandex.credentialsYandex(queryParams);
                response = Html.formYandexLogin();
                break;
            case ("backup"):
                log.info("BACKUP SERVER");
                response = Utils.backupServer(queryParams);
                break;

//      WEB SPEAKERS
            case ("speaker_create"):
                log.info("SPEAKER CREATE");
                SmartHome.create(queryParams);
                SmartHome.write();
                response = Html.formSpeakers();
                break;
            case ("speaker_edit"):
                log.info("SPEAKER SAVE");
                SmartHome.save(queryParams);
                SmartHome.write();
                log.info("EDIT OK");
                response = Html.formSpeakers();
                break;
            case ("speaker_remove"):
                log.info("SPEAKER REMOVE");
                SmartHome.remove(queryParams);
                SmartHome.write();
                response = Html.formSpeakers();
                break;
            case ("speakers_clear"):
                log.info("SPEAKERS CLEAR");
                SmartHome.clear();
                SmartHome.write();
                response = Html.formSpeakers();
                break;

//      WEB PLAYERS
            case ("players_update"):
                log.info("PLAYERS UPDATE");
                lmsPlayers.update();
                lmsPlayers.write();
                response = Html.formSpeakers();
                break;
            case ("players_clear"):
                log.info("PLAYERS CLEAR");
                lmsPlayers.clear();
                lmsPlayers.write();
                response = Html.formSpeakers();
                break;
            case ("player_save"):
                log.info("PLAYER SAVE");
                lmsPlayers.playerSave(queryParams);
                lmsPlayers.write();
                response = Html.formPlayers();
                break;
            case ("player_remove"):
                log.info("PLAYER REMOVE");
                lmsPlayers.playerRemove(queryParams);
                lmsPlayers.write();
                response = Html.formPlayers();
                break;
            case ("reboot_service_sa"):
                log.info("PLAYER REMOVE");
                Utils.rebootServiceSa();
                response = Html.index();
                break;
            case ("reboot_localhost"):
                log.info("PLAYER REMOVE");
                Utils.rebootLocalhost();
                response = Html.index();
                break;
            case ("reboot_service_lms"):
                log.info("PLAYER REMOVE");
                Utils.rebootServiceLms();
                response = Html.index();
                break;
            default:
                log.info("ACTION NOT FOUND: " + action);
                response = "ACTION NOT FOUND: " + action;
                break;
        }
        context.json = response;
        return context;
    }
}

//        reboot_service_sa>Пер
//        reboot_localhost>Пере
//        reboot_service_lms>Пе