package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Utils;
import org.knovash.squeezealice.yandex.Yandex;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class MainTest {

    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static Map<String, String> idRooms = new HashMap<>();
    public static List<String> rooms = new ArrayList<>();
    public static ZoneId zoneId = ZoneId.of("Europe/Minsk");
    public static Config config = new Config();
    public static Boolean lmsServerOnline;
    public static String yandexToken = "";

    public static void main(String[] args) {
        log.info("TIME ZONE: " + zoneId + " TIME: " + LocalTime.now(zoneId).truncatedTo(MINUTES));


        List<String> list = new ArrayList<>();
        for (int i = 1; i < 16; i++) {
            list.add(String.valueOf(i));
        }
        System.out.println(list);

        int target = 1;
        List<String> result = linesFromList(list, target, 7);
        System.out.println(result + " " + target); // [2, 3, 4, 5, 6, 7, 8]

    }


    public static List<String> linesFromList(List<String> list, int index, int lines) {
//   показывать из плейлиста часть сторок до и после играющего трека
        int left = lines;
        int start = Math.max(0, index - left);
        int end = Math.min(list.size(), start + lines*2+1);
        int delta =lines*2+1-(end - start);
        return new ArrayList<>(list.subList(start-delta, end));
    }
}
