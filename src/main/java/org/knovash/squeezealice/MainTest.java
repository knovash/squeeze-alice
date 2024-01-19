package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Yandex;

import java.io.IOException;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        String str = " \"value\" : \"4\",";
        log.info(str);
       str =  str.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1_$2_");
        log.info(str);



    }


}