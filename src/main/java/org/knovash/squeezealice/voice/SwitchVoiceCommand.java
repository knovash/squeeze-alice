package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;

import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class SwitchVoiceCommand {

    public static Context processContext(Context context) {
        String body = context.body;
        context.code = 200;
        String command = JsonUtils.jsonGetValue(body, "command");
        if (command == null) {
            context.bodyResponse = "я не поняла команду";
            return context;
        }
        String aliceId = JsonUtils.jsonGetValue(body, "application_id");
        String answer = processCommand(aliceId, command);
        log.info("ANSWER: {}", answer);
        context.bodyResponse = answer;
        return context;
    }

    public static String processCommand(String aliceId, String command) {
        String cmd = command.trim().toLowerCase();
        log.info("COMMAND: " + command);

        // Базовые команды без зависимостей
        if (cmd.isEmpty())
            return "Я умею управлять плеерами подключенными в Lyrion Music Server. \n" +
                    "Скажите Алисе:\n" +
                    ", включи или выключи музыку \n" +
                    ", музыку громче или тише \n" +
                    ", переключи канал";
        if (cmd.contains("помощь") || cmd.contains("помоги") || cmd.contains("подскажи"))
            return "У вас локально должен быть установлен Lyrion Music Server и приложение навыка";
        if (cmd.contains("что ты умеешь") || cmd.contains("что ты можешь"))
            return "Я умею управлять плеерами подключенными в Lyrion Music Server";

        // Команды привязки комнаты (не требуют наличия комнаты в idRooms)
        if (cmd.startsWith("это комната")) {
            if (cmd.contains("с колонкой")) {
                return ActionsAsync.selectRoomWithSpeaker(command, aliceId);
            } else {
                return ActionsAsync.selectRoomByCommand(command, aliceId);
            }
        }

        // Получаем комнату по идентификатору сессии
        String room = Main.roomsAndAliceIds.get(aliceId);
        if (room == null)
            return "скажите навыку, это комната и название комнаты";

        // Команды выбора колонки (требуют комнату, но не плеер)
        if (cmd.matches("выбери колонку.*"))
            return ActionsAsync.selectPlayerByCommand(command, room);
        if (cmd.matches("включи колонку.*"))
            return ActionsAsync.runPlayerByCommand(command, room);

        // Ищем устройство в комнате и соответствующий плеер
        Device device = SmartHome.deviceByRoom(room);
        if (device == null)
            return "устройство в умном доме не найдено, скажите навыку, выбери колонку, и название колонки";

        lmsPlayers.updatePlayers();

        Player player = lmsPlayers.playerByRoom(device.room);
        if (player == null)
            return "колонка в комнате не выбрана, скажите навыку, выбери колонку, и название колонки";

        // Обработка всех команд, требующих плеер (обернуто в try-catch для устойчивости)
        try {
            if (cmd.contains("что играет"))
                return ActionsSync.whatsPlaying(player);
            if (cmd.contains("какая громкость"))
                return ActionsSync.whatsVolume(player);
            if (cmd.matches("(включи )?(канал|избранное) .*"))
                return ActionsAsync.channelPlayByName(command, player);
            if (cmd.matches("добавь( в)? избранное"))
                return ActionsAsync.channelAdd(player);
            if (cmd.matches("переключи.*сюда"))
                return ActionsAsync.switchHere(player);
            if (cmd.matches("(включи )?отдельно"))
                return ActionsAsync.separateOn(player);
            if (cmd.matches("(включи )?вместе"))
                return ActionsAsync.separateOff(player);
            if (cmd.matches("(включи )?только тут"))
                return ActionsAsync.onlyHere(player);
            if (cmd.matches("(включи )?(рандом|шафл|shuffle|random)"))
                return ActionsAsync.shuffleOn(player);
            if (cmd.matches("(выключи )?(рандом|шафл|shuffle|random)"))
                return ActionsAsync.shuffleOff(player);
            if (cmd.matches("(включи )?(повтор)"))
                return ActionsAsync.repeatOn(player);
            if (cmd.matches("(выключи )?(повтор)"))
                return ActionsAsync.repeatOff(player);
            if (cmd.matches("(включи )?(дальше|следующий)")) {
                ActionsAsync.nextChannelOrTrack(player);
                return "включаю следующий";
            }
            if (cmd.matches("подключи пульт (к|в|на).*"))
                return ActionsAsync.connectBtRemote(command, player);
            if (cmd.contains("где пульт"))
                return ActionsAsync.whereBtRemote();
            if (cmd.startsWith("включи альбом"))
                return ActionsAsync.playAlbum(player, command);
            if (cmd.startsWith("включи трек"))
                return ActionsAsync.playTrack(player, command);
            if (cmd.startsWith("включи плейлист"))
                return ActionsAsync.playPlaylist(player, command);
            if (cmd.startsWith("включи"))
                return ActionsAsync.playArtist(player, command);
        } catch (Exception e) {
            log.error("Ошибка при обработке команды '{}': {}", command, e.getMessage(), e);
            return "Произошла внутренняя ошибка, попробуйте позже";
        }

        return "Я не поняла команду";
    }
}