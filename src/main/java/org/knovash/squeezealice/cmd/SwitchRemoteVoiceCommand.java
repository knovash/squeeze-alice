package org.knovash.squeezealice.cmd;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.player.ActionsAsync;
import org.knovash.squeezealice.player.ActionsSync;

import static org.knovash.squeezealice.Main.start;

@Log4j2
public class SwitchRemoteVoiceCommand {


    public static String remoteVoiceCommandSwithc(String command, Player player) {
        log.info(start);

        // Обработка всех команд, требующих плеер (обернуто в try-catch для устойчивости)
        try {
//            if (command.contains("что играет"))
//                return ActionsSync.whatsPlaying(player, true);
//            if (command.contains("лимит"))
//                return ActionsSync.volumeLimitSet(player, command);
//            if (command.contains("какая громкость"))
//                return ActionsSync.whatsVolume(player);
//            if (command.matches("добавь( в)? избранное"))
//                return ActionsAsync.channelAdd(player);
//            if (command.matches("переключи.*сюда"))
//                return ActionsAsync.switchHere(player);
//            if (command.matches("(включи )?отдельно"))
//                return ActionsAsync.separateOn(player);
//            if (command.matches("(включи )?вместе"))
//                return ActionsAsync.separateOff(player);
//            if (command.matches("(включи )?только тут"))
//                return ActionsAsync.onlyHere(player);
//            if (command.matches("(включи )?(рандом|шафл|shuffle|random)"))
//                return ActionsAsync.shuffleOn(player);
//            if (command.matches("(выключи )?(рандом|шафл|shuffle|random)"))
//                return ActionsAsync.shuffleOff(player);
//            if (command.matches("(включи )?(повтор)"))
//                return ActionsAsync.repeatOn(player);
//            if (command.matches("(выключи )?(повтор)"))
//                return ActionsAsync.repeatOff(player);
//            if (command.matches("(включи )?(дальше|следующий)")) {
//                ActionsAsync.nextChannelOrTrack(player);
//                return "включаю следующий";
//            }

            if (command == null || "включи".equals(command.toLowerCase().trim())) {
                log.info("BAD COMMAND SKIP");
                return null;
            }

            if (command.matches("^(?:подключи пульт(?: (?:к|в|на).*)?|включи пульт)$")) {

                String answer = ActionsAsync.connectBtRemote(command, player);
                if (answer != null) player.say(answer, true, true);
                return answer;
            }
            if (command.contains("где пульт")) {
                String answer = ActionsSync.whereBtRemote();
                player.say(answer, true, true);
                return answer;
            }

            if (command.matches("(включи )?(канал|избранное) .*"))
                return ActionsAsync.channelPlayByName(command, player);
            if (command.startsWith("включи альбом"))
                return ActionsSync.spotifyPlayAlbum(command, player, true);
            if (command.startsWith("включи трек"))
                return ActionsSync.spotifyPlayTrack(command, player, true);
            if (command.startsWith("включи плейлист"))
                return ActionsAsync.playPlaylist(player, command);
            if (command.startsWith("включи"))
                return ActionsSync.spotifyPlayArtist(command, player, true);

        } catch (Exception e) {
            log.error("Ошибка при обработке команды '{}': {}", command, e.getMessage(), e);
            return "Произошла внутренняя ошибка, попробуйте позже";
        }
        return "";
    }

}
