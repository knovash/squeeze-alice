package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.HttpUtils;
import org.knovash.squeezealice.requests.Html;

import java.util.HashMap;

import static org.knovash.squeezealice.Utils.altNames;

@Log4j2
public class Switch {

    public static String action(String query) {
        log.info("QUERY: " + query);
        String actionStatus;
        String name = null;
        Player player = null;
        HashMap<String, String> parameters = HttpUtils.getQueryParameters(query);
        if (!parameters.containsKey("action")) {
            log.info("NO ACTION IN QUERY");
            return query + " BAD REQUEST try /cmd?action=state";
        }
        String action = parameters.get("action");
        log.info("ACTION: " + action);
        if (parameters.get("player") != null) {
            name = parameters.get("player");
            log.info("NAME: " + name);
            name = Utils.altPlayerName(name);
            log.info("ALT NAME: " + name);
            player = Server.playerByName(name);
            if (player == null) {
                log.info("NO PLAYER : " + name + " TRY UPDATE FROM LMS AND RETRY");
                Server.updatePlayers();
                player = Server.playerByName(name);
                if (player == null) {
                    log.info("NO PLAYER: " + name + " ON SERVER");
                    return ("ERROR: NO PLAYER IN LMS " + name + "Try check alt names: " + altNames);
                }
            }
            if (player.isBlack()) {
                log.info("PLAYER: " + name + " IN BLACK");
                return ("PLAYER IN BLACKLIST " + name);
            }
        }

        switch (action) {
            case ("channel"):
                Action.channel(player, Integer.valueOf(parameters.get("value")));
                actionStatus = "CHANNEL COMPLETE";
                break;
            case ("volume"):
                Action.volume(player, parameters.get("value"));
                actionStatus = "VOLUME COMPLETE";
                break;
            case ("all_low_high"):
                Action.allLowOrHigh(parameters.get("value"));
                actionStatus = "PRESET COMPLETE";
                break;
            case ("turn_on_music"):
            case ("turn_on_speaker"):
                Action.turnOnMusicSpeaker(player);
                actionStatus = "MUSIC ON COMPLETE";
                break;
            case ("turn_off_music"):
                Action.turnOffMusic();
                actionStatus = "MUSIC OFF COMPLETE";
                break;
            case ("turn_off_speaker"):
                Action.turnOffSpeaker(player);
                actionStatus = "SPEAKER OFF COMPLETE";
                break;
            case ("turn_on_spotify"):
            case ("spotify"):
                log.info("SPOTIFY");
                Action.turnOnSpotify(player);
                actionStatus = "SPOTIFY COMPLETE";
                break;
            case ("update_players"):
            case ("update"):
                Server.updatePlayers();
                actionStatus = "UPDATE COMPLETE";
                break;
            case ("show_log"):
            case ("log"):
                log.info("SHOW LOG");
                actionStatus = Utils.logLastLines(parameters);
                break;
            case ("silence"):
                log.info("SILENCE");
                player.playSilence();
                actionStatus = "SILENCE COMPLETE";
                break;
            case ("change_value"):
                log.info("CHANGE PLAYER VALUE");
                Utils.changePlayerValue(parameters);
                actionStatus = "VALUE COMPLETE";
                break;
            case ("alt_name_add"):
                log.info("ALT NAME ADD");
                Utils.altNameAdd(parameters);
                actionStatus = "ALT NAME COMPLETE";
                break;
            case ("remove"):
                log.info("REMOVE PLAYER");
                player.remove();
                actionStatus = "REMOVE COMPLETE";
                break;
            case ("state"):
                log.info("SEND SERVER STATE");
                actionStatus = Utils.state();
                break;
            case ("time_volume_get"):
                log.info("SEND TIME AND VOLUME");
                actionStatus = Utils.timeVolumeGet(player);
                break;
            case ("time_volume_set"):
                log.info("CHANGE TIME AND VOLUME");
                actionStatus = Utils.timeVolumeSet(player, parameters);
                break;
            case ("time_volume_del"):
                log.info("DELETE TIME AND VOLUME");
                actionStatus = Utils.timeVolumeDel(player, parameters);
                break;
            case ("cred"):
                log.info("CREDENTIALS");
                actionStatus = SpotifyUtils.credentials(parameters);
                break;
            case ("backup"):
                log.info("BACKUP SERVER");
                actionStatus = Utils.backupServer(parameters);
                break;
            case ("speaker_create"):
                log.info("CREATE SPEAKER");
                NewDevice.create(parameters);
                actionStatus = Html.formSpeakers();
                break;
            case ("speaker_edit"):
                log.info("EDIT SPEAKER");
                NewDevice.edit(parameters);
                log.info("EDIT OK");
                actionStatus = Html.formSpeakers();
                break;
            case ("speaker_remove"):
                log.info("REMOVE SPEAKER");
                NewDevice.remove(parameters);
                actionStatus = Html.formSpeakers();
                break;
            default:
                log.info("ACTION NOT FOUND: " + action);
                actionStatus = "ACTION NOT FOUND: " + action;
                break;
        }
        return actionStatus;
    }
}