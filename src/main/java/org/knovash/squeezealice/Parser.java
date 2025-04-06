package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Parser {

    public static Map<String, String> run(String text) {
        log.info("PARSER START");
        Map<String, String> result = new HashMap<>();
        splitByAnd(text).stream()
//                .peek(p -> log.info(p))
                .map(p -> splitByEqual(p))
//                .peek(p -> log.info("   " + p.get(0) + "  =  " + p.get(1)))
                .forEach(p -> result.put(p.get(0), p.get(1)));
        log.info("RESULT:\n" + result);
        return result;
    }

    private static List<String> splitByAnd(String text) {
        return List.of(text.split("&(?![^{]*})"));
    }

    private static List<String> splitByEqual(String text) {
        return List.of(text.split("=(?![^{]*})"));
    }

}
