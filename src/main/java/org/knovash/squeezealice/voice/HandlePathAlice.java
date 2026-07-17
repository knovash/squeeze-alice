package org.knovash.squeezealice.voice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.player.ActionsAsync;
import org.knovash.squeezealice.player.ActionsSync;
import org.knovash.squeezealice.provider.response.Device;
import org.knovash.squeezealice.utils.JsonUtils;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class HandlePathAlice {

    public static Context processContext(Context context) {
        log.info(start);
        String body = context.body;
        context.code = 200;
        String command = JsonUtils.jsonGetValue(body, "command");

        if (command == null) {
            context.bodyResponse = "я не поняла команду";
            return context;
        }

        String aliceId = JsonUtils.jsonGetValue(body, "application_id");
        String room = Main.roomsAndAliceIds.get(aliceId);
        log.info("ROOM BY aliceId: " + room);

        String answer = processCommand(aliceId, room, command);

        log.info("ANSWER: {}", answer);
        context.bodyResponse = answer;
        log.info(finish);
        return context;
    }

    public static String processCommand(String aliceId, String room, String command) {
        log.info(start);

        command = command.trim().toLowerCase();
        log.info("COMMAND: " + command);

        // Базовые команды без зависимостей
        if (command.isEmpty())
            return "Я умею управлять плеерами подключенными в Lyrion Music Server. \n" +
                    "Скажите Алисе:\n" +
                    ", включи или выключи музыку \n" +
                    ", музыку громче или тише \n" +
                    ", переключи канал";
        if (command.contains("помощь") || command.contains("помоги") || command.contains("подскажи"))
            return "У вас локально должен быть установлен Lyrion Music Server и приложение навыка";
        if (command.contains("что ты умеешь") || command.contains("что ты можешь"))
            return "Я умею управлять плеерами подключенными в Lyrion Music Server";

        // Команды привязки комнаты (не требуют наличия комнаты в idRooms)
        if (command.startsWith("это комната")) {
            if (command.contains("с колонкой")) {
                return ActionsAsync.selectRoomWithSpeaker(command, aliceId);
            } else {
                return ActionsAsync.selectRoomByCommand(command, aliceId);
            }
        }

        // Получаем комнату по идентификатору сессии
        lmsPlayers.updatePlayers();
        if (room == null)
            return "скажите навыку, это комната и название комнаты";

        // Команды выбора колонки (требуют комнату, но не плеер)
        if (command.matches("выбери колонку.*"))
            return ActionsAsync.selectPlayerByCommand(command, room);
        if (command.matches("включи колонку.*"))
            return ActionsAsync.runPlayerByCommand(command, room);

        // Ищем устройство в комнате и соответствующий плеер
        Device device = SmartHome.deviceByRoom(room);
        if (device == null)
            return "устройство в умном доме не найдено, скажите навыку, выбери колонку, и название колонки";

        Player player = lmsPlayers.playerByRoom(device.room);
        log.info("PLAYER: " + player.name);
        if (player == null)
            return "колонка в комнате не выбрана, скажите навыку, выбери колонку, и название колонки";

        // Обработка всех команд, требующих плеер (обернуто в try-catch для устойчивости)
        try {
            if (command.contains("что играет")) // синхронно, собирает информацию и тут же отдает ответ
                return ActionsAsync.whatsPlaying(player, true);
            if (command.contains("лимит")) // синхронно, собирает информацию и тут же отдает ответ
                return ActionsAsync.volumeLimitSet(player, command);
            if (command.contains("какая громкость")) // синхронно, собирает информацию и тут же отдает ответ
                return ActionsAsync.whatsVolume(player);
            if (command.matches("(включи )?(канал|избранное) .*"))
                return ActionsAsync.channelPlayByName(command, player);
            if (command.matches("добавь( в)? избранное"))
                return ActionsAsync.channelAdd(player);
            if (command.matches("переключи.*сюда"))
                return ActionsAsync.switchHere(player);
            if (command.matches("(включи )?отдельно"))
                return ActionsAsync.separateOn(player);
            if (command.matches("(включи )?вместе"))
                return ActionsAsync.separateOff(player);
            if (command.matches("(включи )?только тут"))
                return ActionsAsync.onlyHere(player);
            if (command.matches("(включи )?(рандом|шафл|shuffle|random)"))
                return ActionsAsync.shuffleOn(player);
            if (command.matches("(выключи )?(рандом|шафл|shuffle|random)"))
                return ActionsAsync.shuffleOff(player);
            if (command.matches("(включи )?(повтор)"))
                return ActionsAsync.repeatOn(player);
            if (command.matches("(выключи )?(повтор)"))
                return ActionsAsync.repeatOff(player);
            if (command.matches("(включи )?(дальше|следующий)")) {
                ActionsAsync.nextChannelOrTrack(player);
                return "включаю следующий";
            }

            if (command.matches("^(?:подключи пульт(?: (?:к|в|на).*)?|включи пульт)$"))
                return ActionsAsync.connectBtRemote(command, player);
            if (command.contains("где пульт"))
                return ActionsAsync.whereBtRemote();
            if (command.startsWith("включи альбом"))
                return ActionsAsync.playAlbum(player, command);
            if (command.startsWith("включи трек"))
                return ActionsAsync.playTrack(player, command);
            if (command.startsWith("включи плейлист"))
                return ActionsAsync.playPlaylist(player, command);
            if (command.startsWith("включи"))
                return ActionsAsync.playArtist(player, command);
        } catch (Exception e) {
            log.error("Ошибка при обработке команды '{}': {}", command, e.getMessage(), e);
            return "Произошла внутренняя ошибка, попробуйте позже";
        }

        return "Я не поняла команду";
    }
}