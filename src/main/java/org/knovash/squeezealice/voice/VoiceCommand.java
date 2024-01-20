package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.provider.SmartHome;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class VoiceCommand {

    public static Context action(Context context) {
        log.info("");
        String body = context.body;
        log.info("");
        String response = "повторите";
        String command = JsonUtils.jsonGetValue(body, "command");
        log.info("COMMAND: " + command); // текст из диалога

//"user_id":"72282F32117030A55F5ADBB52DE84809DB4829960DBB246FDC15F35D27372FA1"}, гостиная
//"user_id":"72282F32117030A55F5ADBB52DE84809DB4829960DBB246FDC15F35D27372FA1" гостиная
//"user_id":"72282F32117030A55F5ADBB52DE84809DB4829960DBB246FDC15F35D27372FA1"}, душ
//"application_id":"76F751A76299DE71E1E9784E207AFC2BA1AB01361D8F8B9483A857FA87C087FA" гостиная
//"application_id":"76F751A76299DE71E1E9784E207AFC2BA1AB01361D8F8B9483A857FA87C087FA" гостиная
//"application_id":"B9AC4386E4621FE3F21AC35537D5F52CA9028F5406F599788E0F328329E2E02F" душ
//"user_id":       "76F751A76299DE71E1E9784E207AFC2BA1AB01361D8F8B9483A857FA87C087FA"} гостиная
//"user_id":       "76F751A76299DE71E1E9784E207AFC2BA1AB01361D8F8B9483A857FA87C087FA" гостиная
//"user_id":       "B9AC4386E4621FE3F21AC35537D5F52CA9028F5406F599788E0F328329E2E02F" душ

        String applicationId = JsonUtils.jsonGetValue(body, "application_id");
        log.info("applicationId: " + applicationId);
        log.info(SmartHome.applicationIdAndPlayerName.get(applicationId));
        String playerName = SmartHome.applicationIdAndPlayerName.get(applicationId);
        log.info("playerName: " + playerName);

        if (command != null) {
            response = SwitchVoiceCommand.action(command, playerName);
        }

        context.json = response;
        context.code = 200;
        return context;
    }
}