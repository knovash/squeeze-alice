package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.DeviceUtils;
import org.knovash.squeezealice.provider.Yandex;
//import org.knovash.squeezealice.spotify.SpotifyUtils;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.web.Html;

import java.util.HashMap;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.utils.Utils.altNames;

@Log4j2
public class SwitchQueryCommand {

    public static Context action(String query, Context context) {
        log.info("QUERY: " + query);
        String response;
        String name;
        Player player = null;
        HashMap<String, String> parameters = context.queryMap;
        context.code = 200;
        if (!parameters.containsKey("action")) {
            log.info("BAD REQUEST NO ACTION IN QUERY");
            context.json = "BAD REQUEST NO ACTION IN QUERY";
            return context;
        }
        String action = parameters.get("action");
        log.info("ACTION: " + action);

        if (parameters.get("player") != null) {
            name = parameters.get("player");
            log.info("NAME: " + name);
            name = Utils.getAltPlayerNameByName(name);
            log.info("ALT NAME: " + name);
            player = lmsPlayers.getPlayerByName(name);
            if (player == null) {
                log.info("NO PLAYER : " + name + " TRY UPDATE FROM LMS AND RETRY");
                lmsPlayers.update();
                player = lmsPlayers.getPlayerByName(name);
                if (player == null) {
                    log.info("NO PLAYER: " + name + " ON SERVER");
                    context.json = "ERROR: NO PLAYER IN LMS " + name + "Try check alt names: " + altNames;
                    return context;
                }
                lmsPlayers.write();
            }
            if (player.isBlack()) {
                log.info("PLAYER: " + name + " IN BLACK");
                context.json = "PLAYER IN BLACKLIST " + name;
                return context;
            }
        }

        switch (action) {
            case ("channel"):
                SwitchAliceCommand.channel(player, Integer.valueOf(parameters.get("value")));
                response = "CHANNEL COMPLETE";
                break;
            case ("volume"):
                SwitchAliceCommand.volume(player, parameters.get("value"));
                response = "VOLUME COMPLETE";
                break;
            case ("all_low_high"):
                SwitchAliceCommand.allLowOrHigh(parameters.get("value"));
                response = "PRESET COMPLETE";
                break;
            case ("turn_on_music"):
            case ("turn_on_speaker"):
                SwitchAliceCommand.turnOnMusicSpeaker(player);
                response = "MUSIC ON COMPLETE";
                break;
            case ("turn_off_music"):
                SwitchAliceCommand.turnOffMusic();
                response = "MUSIC OFF COMPLETE";
                break;
            case ("turn_off_speaker"):
                SwitchAliceCommand.turnOffSpeaker(player);
                response = "SPEAKER OFF COMPLETE";
                break;
            case ("toggle_music"):
                response = SwitchAliceCommand.toggleMusic(player);
                break;
            case ("turn_on_spotify"):
            case ("spotify"):
                log.info("SPOTIFY");
                SwitchAliceCommand.turnOnSpotify(player);
                response = "SPOTIFY COMPLETE";
                break;
            case ("update_players"):
            case ("update"):
                lmsPlayers.update();
                response = "UPDATE COMPLETE";
                break;
            case ("show_log"):
            case ("log"):
                log.info("SHOW LOG");
                response = Utils.logLastLines(parameters);
                break;
            case ("silence"):
                log.info("SILENCE");
                player.playSilence();
                response = "SILENCE COMPLETE";
                break;
            case ("change_value"):
                log.info("CHANGE PLAYER VALUE");
                Utils.changePlayerValue(parameters);
                response = "VALUE COMPLETE";
                break;
            case ("alt_name_add"):
                log.info("ALT NAME ADD");
                Utils.addPlayerAltName(parameters);
                response = "ALT NAME COMPLETE";
                break;
            case ("remove"):
                log.info("REMOVE PLAYER");
                player.remove();
                response = "REMOVE COMPLETE";
                break;
            case ("state"):
                log.info("SEND SERVER STATE");
                response = Utils.state();
                break;
            case ("players"):
                log.info("SEND PLAYERS");
                response = Utils.players();
                break;
            case ("home"):
                log.info("SEND SERVER HOME");
                response = Utils.home();
                break;
            case ("time_volume_get"):
                log.info("SEND TIME AND VOLUME");
                response = Utils.timeVolumeGet(player);
                break;
            case ("time_volume_set"):
                log.info("CHANGE TIME AND VOLUME");
                response = Utils.timeVolumeSet(player, parameters);
                break;
            case ("time_volume_del"):
                log.info("DELETE TIME AND VOLUME");
                response = Utils.timeVolumeDel(player, parameters);
                break;
            case ("cred_spotify"):
                log.info("CREDENTIALS SPOTIFY");
                Spotify.credentialsSpotify(parameters);
                response = Html.formSpotifyLogin();
                break;
            case ("cred_yandex"):
                log.info("CREDENTIALS YANDEX");
                Yandex.credentialsYandex(parameters);
                response = Html.formYandexLogin();
                break;
            case ("backup"):
                log.info("BACKUP SERVER");
                response = Utils.backupServer(parameters);
                break;
            case ("speaker_create"):
                log.info("CREATE SPEAKER");
                DeviceUtils.create(parameters);
                response = Html.formSpeakers();
                break;
            case ("speaker_edit"):
                log.info("EDIT SPEAKER");
                DeviceUtils.edit(parameters);
                log.info("EDIT OK");
                response = Html.formSpeakers();
                break;
            case ("player_edit"):
                log.info("EDIT PLAYER");
                lmsPlayers.editPlayer(parameters);
                log.info("EDIT OK");
                response = Html.formPlayers();
                break;
            case ("speaker_remove"):
                log.info("REMOVE SPEAKER");
                DeviceUtils.remove(parameters);
                response = Html.formSpeakers();
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