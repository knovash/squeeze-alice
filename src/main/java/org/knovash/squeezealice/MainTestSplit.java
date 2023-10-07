package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.net.SocketException;
import java.net.UnknownHostException;

@Log4j2
public class MainTestSplit {

    public static String sss = null;

    public static void main(String[] args) throws SocketException, UnknownHostException {
        log.info("  ---+++===[ START ]===+++---");

        String sss = "667 888 888";
        log.info(sss);
        JsonUtils.valueToJsonFile("secret", sss);

        String ggg = JsonUtils.valueFromJsonFile("secret.json");
        log.info(ggg);


    }
}