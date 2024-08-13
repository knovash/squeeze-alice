package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;
import org.knovash.squeezealice.spotify.SpotifyAuth;
import org.knovash.squeezealice.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class MainTest {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIp = bundle.getString("lmsIp");
    public static String lmsPort = bundle.getString("lmsPort");
    public static String lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static Map<String, String> config = new HashMap<>();
    public static Map<String, String> rooms = new HashMap<>();

    public static void main(String[] args) {
        log.info("START");
        String command;
        command = "Алиса, включи колонку";
        command = "Алиса, включи колонку";
        command = "Алиса, включи плеер";


        if (command.contains("включи колонку") || command.contains("выабери колонку") || command.contains("выбери плеер") || command.contains("включи плеер"))
            log.info("OK1");

        if (command.matches("включи .*колонку.*|.*плеер.*"))
            log.info("OK2");

        if (command.matches(".*включи (колонку|плеер)"))
            log.info("OK3");

        log.info("FINISH");

        command = "это комната веранда с колонкой хомпод";
        Pattern pattern = Pattern.compile("(?<=комната )[a-zA-Zа-яА-Я]*");
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) log.info(matcher.group());


    }
}