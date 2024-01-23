package org.knovash.squeezealice;

import com.sun.net.httpserver.Headers;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;

import java.io.IOException;
import java.time.LocalTime;

import static java.time.temporal.ChronoUnit.MINUTES;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        Headers headers1 = new Headers();
        headers1.add("fsfd","fsd GFDG");
        headers1.add("ffff","33333");

        log.info(headers1);

        Headers headers2 = new Headers();
        headers2.add("GFDGDFG","4444");
        headers2.add("BBBB","7777");

        headers2.add("zzzz","777ddd7 SS");

        log.info(headers2);

        headers2.putAll(headers1);

        log.info(headers2);



    }

}