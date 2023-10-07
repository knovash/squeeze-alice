package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.net.SocketException;

@Log4j2
public class MainTest {

    public static void main(String[] args) throws SocketException, NoSuchFieldException, IllegalAccessException {
        log.info("  ---+++===[ START ]===+++---");


       String sss =  Utils.searchLmsIp();
       log.info("LMS   " + sss);

    }
}