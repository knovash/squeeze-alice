package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.voice.SwitchVoiceCommand;
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
        String playerInQuery = queryParams.get("player");
        log.info("PLAYER IN QUERY: " + playerInQuery);
        Player player = lmsPlayers.getPlayerByNameInQuery(playerInQuery);
        log.info("PLAYER BY NAME IN QUERY: " + player);
        String value = queryParams.get("value");
        log.info("VALUE: " + value);
        switch (action) {
            case ("channel"):
                Actions.playChannel(player, Integer.valueOf(value));
                response = "PLAY CHANNEL " + value;
                break;
            case ("play"):
                Actions.turnOnMusic(player);
                response = "PLAY";
                break;
            case ("toggle_music"):
            case ("play_pause"):
                log.info("CASE TOGGLE MUSIC " + player);
                response = Actions.toggleMusic(player);
                break;
            case ("toggle_music_all"):
            case ("play_pause_all"):
                log.info("CASE TOGGLE MUSIC ALL");
                response = Actions.toggleMusicAll(player);
                break;
            case ("stop_all"):
            case ("pause_all"):
                log.info("CASE STOP MUSIC ALL");
                response = Actions.stopMusicAll();
                break;
            case ("next"):
                player.next();
                response = "NEXT";
                break;
            case ("prev"):
                player.prev();
                response = "PREV";
                break;
            case ("next_track"):
                player.nextTrack();
                response = "NEXT";
                break;
            case ("prev_track"):
                player.prevTrack();
                response = "PREV";
                break;
            case ("next_channel"):
                player.nextChannel();
                response = "NEXT";
                break;
            case ("prev_channel"):
                player.prevChannel();
                response = "PREV";
                break;
            case ("volume_dn"):
                player.volumeSet("-3");
                response = "VOLUME DN";
                break;
            case ("volume_up"):
                player.volumeSet("+3");
                response = "VOLUME UP";
                break;
            case ("separate_on"):
                player.separate_on();
                response = "SEPARATE ON";
                break;
            case ("alone_on"):
                player.alone_on();
                response = "ALONE ON";
                break;
            case ("separate_alone_off"):
                player.separate_alone_off();
                response = "ALONE OK";
                break;
            case ("transfer"):
                Spotify.transfer(player);
                response = "TRANSFER";
                break;
            case ("whatsplaying"):
//                SwitchVoiceCommand.createResponse(SwitchVoiceCommand.whatsPlaying(player));
                response = "WHATSPLAYING";
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
                response = JsonUtils.pojoToJson(SmartHome.devices);
                break;
            case ("state_players"):
                log.info("STATE LMS PLAYERS");
                response = JsonUtils.pojoToJson(lmsPlayers);
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
            case ("spotify_save_creds"):
                log.info("CREDENTIALS SPOTIFY");
                SpotifyAuth.save(queryParams);
                response = Html.formSpotifyLogin();
                break;
            case ("cred_yandex"):
                log.info("CREDENTIALS YANDEX");
                Yandex.credentialsYandex(queryParams);
                response = Html.formYandexLogin();
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
            case ("restart"):
                log.info("RESTART SERVER");
                Utils.restart();
                response = "RESTART";
                break;
            case ("reboot"):
                log.info("REBOOT SERVER");
                Utils.reboot();
                response = "REBOOT";
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