package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.LmsPlayers;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class VoiceCommand {

    public static Context action(Context context) {
        log.info("");
        String body = context.body;
        log.info("");
        String response = "повторите";
        String command = JsonUtils.jsonGetValue(body, "command");
        log.info("COMMAND: " + command); // текст из диалога

        String alice_id = JsonUtils.jsonGetValue(body, "application_id");
        Player.lastAliceId = alice_id;
        log.info("alice_id: " + alice_id);
        String playerName = LmsPlayers.playerNameByAliceId(alice_id);
        log.info("playerName: " + playerName);

        response = SwitchVoiceCommand.action(command, playerName);

        context.json = response;
        context.code = 200;
        return context;
    }
}