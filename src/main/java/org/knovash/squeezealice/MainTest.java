package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.spotify.Type;

@Log4j2
public class MainTest {

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        String dd ="быстро включи   техно depeche mode\"";

        String target = dd.replaceAll(".*включи\\S*\\s", "")
                .replaceAll("\"", "")
                .replaceAll("\\s\\s", " ");

        log.info(target);

    }
}