package org.knovash.squeezealice.server;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.Parser;
import org.knovash.squeezealice.web.PageIndex;
import org.knovash.squeezealice.web.PagePlayers;

import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.lmsPlayers;
import static org.knovash.squeezealice.web.PagePlayers.*;
@Log4j2
public class ProcessForm {

    public static void processFormContext(Context context) {
//        log.info("PROCESS CONTEXT START");
        Map<String, String> bodyMap = Parser.bodyToMap(context.body);
        context.code = 200;
        if (bodyMap.containsKey("action")) {
            String action = bodyMap.get("action");
            log.info("SWITCH CASE action: " + action);
            switch (action) {
                case statusbar_refresh:
                    PageIndex.refresh((HashMap<String, String>) bodyMap);
                    context.setRedirect("/");
                    break;
                case delay_expire_save:
                    lmsPlayers.delayExpireSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case toggle_wake_save:
                    lmsPlayers.toggleWakeSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case toggle_voice_save:
                    lmsPlayers.toggleVoiceSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case autoremote_save:
                    lmsPlayers.autoremoteSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case autoremote_remove:
                    lmsPlayers.autoremoteRemove((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
//                case alt_sync_save:
//                    lmsPlayers.altSyncSave((HashMap<String, String>) bodyMap);
//                    context.bodyResponse = PagePlayers.page();
//                    break;
                case last_this_save:
                    lmsPlayers.lastThisSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;


                case volume_amp_lms:
                    lmsPlayers.volumeAmpLmsSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;
                case volume_amp_ffs:
                    lmsPlayers.volumeAmpFfsSave((HashMap<String, String>) bodyMap);
                    context.bodyResponse = PagePlayers.page();
                    break;


                case player_save:
                    lmsPlayers.playerSave((HashMap<String, String>) bodyMap);
                    context.setRedirect("/players");
                    break;
                case player_remove:
                    lmsPlayers.playerRemove((HashMap<String, String>) bodyMap);
                    context.setRedirect("/players");
                    break;
                case lms_save:
                    lmsPlayers.lmsSave((HashMap<String, String>) bodyMap);
                    context.setRedirect("/lms");
                    break;
                case volumio_save:
                    lmsPlayers.volumioSave((HashMap<String, String>) bodyMap);
                    context.setRedirect("/volumio_settings");
                    break;
                default:
                    log.info("ACTION ERROR " + action);
                    break;
            }
        }
//        log.info("PROCESS CONTEXT FINISH");
    }
}
