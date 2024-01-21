package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;

import java.io.IOException;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        String str = " \"value\" : \"4\",";
        log.info(str);
       str =  str.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1_$2_");
        log.info(str);

        LocalTime ttt = LocalTime.now().truncatedTo(MINUTES);
        log.info(ttt);
//        String ts = ttt.toString();
        String ts = timeToString(ttt);
                log.info(ts);

        LocalTime tn = stringToTime(ts);

        log.info(tn);



    }

    public static String timeToString(LocalTime time){
        String timeStr = time.toString();
        return timeStr;
    }

    public static LocalTime stringToTime(String  timeStr){
        LocalTime time = LocalTime.parse(timeStr);
        return time;
    }


}