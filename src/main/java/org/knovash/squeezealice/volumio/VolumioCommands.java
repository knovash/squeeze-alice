package org.knovash.squeezealice.volumio;

import lombok.extern.log4j.Log4j2;

/**
 * Утилитарный класс для формирования URL команд Volumio (GET-версия).
 * Все методы возвращают полный URL для выполнения GET-запроса.
 */
@Log4j2
public class VolumioCommands {

    private static final String COMMANDS_PATH = "/api/v1/commands/";
    private static final String STATE_PATH = "/api/v1/getState";

    public static String getState(String baseUrl) {
        return baseUrl + STATE_PATH;
    }

    public static String play(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=play";
    }

    public static String pause(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=pause";
    }

    public static String toggle(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=toggle";
    }

    public static String next(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=next";
    }

    public static String prev(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=prev";
    }

    public static String stop(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=stop";
    }

    public static String volumeUp(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=volume&volume=plus";
    }

    public static String volumeDown(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=volume&volume=minus";
    }

    public static String setVolume(String baseUrl, int volume) {
        log.info("?cmd=volume&volume=" + volume);
        return baseUrl + COMMANDS_PATH + "?cmd=volume&volume=" + volume;
    }

    public static String mute(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=volume&volume=mute";
    }

    public static String unmute(String baseUrl) {
        return baseUrl + COMMANDS_PATH + "?cmd=volume&volume=unmute";
    }
}