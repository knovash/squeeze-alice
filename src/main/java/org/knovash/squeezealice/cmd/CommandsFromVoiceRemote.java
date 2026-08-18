package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.player.ActionsAsync;

import static org.knovash.squeezealice.Main.start;

@Log4j2
public class CommandsFromVoiceRemote {

    public static void remoteVoiceCommands(String command, Player player) {
        log.info(start);

        if (command == null || "включи".equals(command.toLowerCase().trim())) {
            log.info("BAD COMMAND SKIP");
            return;
        }
        if (command.matches("^(?:подключи пульт(?: (?:к|в|на).*)?|включи пульт)$")) {
            String answer = ActionsAsync.connectBtRemote(command, player);
            if (answer != null) player.say(answer, true, true);
            return;
        }
        if (command.startsWith("включи альбом")) {
            ActionsAsync.spotifyPlayCommand(player, command, "album");
            return;
        }
        if (command.startsWith("включи трек")) {
            ActionsAsync.spotifyPlayCommand(player, command, "track");
            return;
        }
        if (command.startsWith("включи плейлист")) {
            ActionsAsync.spotifyPlayCommand(player, command, "playlist");
            return;
        }
        if (command.startsWith("включи")) {
            ActionsAsync.spotifyPlayCommand(player, command, "artist");
        }
    }
}