package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");


//         "value" : "4",
        String str = " \"value\" : \"4\",";

        log.info(str);

       str =  str.replaceAll("(\"value\" :) \"([0-9a-z]+)\"", "$1_$2_");

        log.info(str);

    }


}