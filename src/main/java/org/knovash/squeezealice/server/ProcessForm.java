package org.knovash.squeezealice.server;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.Parser;
import org.knovash.squeezealice.web.PageIndex;
import org.knovash.squeezealice.web.PagePlayers;

import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.ActionsRunner.actionsRunner;
import static org.knovash.squeezealice.web.PagePlayers.*;
import static org.knovash.squeezealice.web.Settings.settings;
import static org.knovash.squeezealice.web.SettingsButtons.settingsButtons;
import static org.knovash.squeezealice.web.SettingsTasker.settingsTasker;
import static org.knovash.squeezealice.web.SettingsVoice.settingsVoice;

@Log4j2
public class ProcessForm {

    public static void processFormContext(Context context) {
        Map<String, String> bodyMap = Parser.bodyToMap(context.body);
        context.code = 200;
        if (bodyMap.containsKey("action")) {
            String action = bodyMap.get("action");


            // Обработка действий из ActionsRunner
            if (action.startsWith("ACTION_")) {
                actionsRunner.handleAction(context);  // предполагается, что actionsRunner статически доступен в Main
                return;
            }


            log.info("SWITCH CASE action: " + action);
            switch (action) {
                case "settings_save_all":
                    settings.handleSaveAll(context);
                    break;
                case "settings_buttons_save":
                    settingsButtons.handleSaveAll(context);
                    break;
                case "settings_tasker_save":
                    settingsTasker.handleSaveAll(context);
                    break;
                case "settings_voice_save":
                    settingsVoice.handleSaveAll(context);
                    break;
                case statusbar_refresh:
                    PageIndex.refresh((HashMap<String, String>) bodyMap);
                    context.setRedirect("/");
                    break;
                case delay_expire_save:
                    lmsPlayers.delayExpireSave((HashMap<String, String>) bodyMap);
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
                default:
                    log.info("ACTION ERROR " + action);
                    break;
            }
        }
    }
}
