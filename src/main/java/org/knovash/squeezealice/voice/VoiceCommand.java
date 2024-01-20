package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

@Log4j2
public class VoiceCommand {

    public static Context action(Context context) {
        log.info("");
        String body = context.body;
        log.info("");
        String response = "повторите";
        String command = JsonUtils.jsonGetValue(body, "command");
        log.info("COMMAND: " + command); // текст из диалога
        String applicationId = JsonUtils.jsonGetValue(body, "application_id");
        String playerName = Utils.appIdPlayer(applicationId);
        if (command != null) {
            response = SwitchVoiceCommand.action(command, playerName);
        }
        context.json = response;
        context.code = 200;
        return context;
    }
}