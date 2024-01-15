package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        Map<Integer, Integer> ddd = new HashMap<>();
        ddd.put(8, 5);
        ddd.put(10, 15);
        ddd.put(22, 10);
        log.info(ddd);

        String header = "23:32,33:87,2:3";

//        Map<String, String> properties = Splitter.on(",")
//                .withKeyValueSeparator(":")
//                .split(inputString);

        Map<String, String> headerMap = Arrays.stream(header.split(","))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        log.info(headerMap);

        String str = headerMap.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(","));
        log.info(str);

        log.info(
headerMap.entrySet().stream()
        .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getKey()), entry -> Integer.valueOf(entry.getValue())))
        );

        Map<Integer,Integer> iii = headerMap.entrySet().stream()
                .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getKey()), entry -> Integer.valueOf(entry.getValue())));



        Map<Integer, Integer> headddderMap = Arrays.stream(header.split(","))
                .map(s -> s.split(":"))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]))
                .entrySet().stream()
                .collect(Collectors.toMap(entry -> Integer.valueOf(entry.getKey()), entry -> Integer.valueOf(entry.getValue())));

    }


}