package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public class Preset {

    public static String volume() {
        Integer volume = 5;
        if (LocalTime.now().isAfter(LocalTime.of(7, 00)) &&
                LocalTime.now().isAfter(LocalTime.of(9, 00))) {
            volume = 5;
        }
        if (LocalTime.now().isAfter(LocalTime.of(9, 00)) &&
                LocalTime.now().isAfter(LocalTime.of(20, 30))) {
            volume = 10;
        }
        if (LocalTime.now().isAfter(LocalTime.of(22, 00)) &&
                LocalTime.now().isAfter(LocalTime.of(7, 00))) {
            volume = 5;
        }
        return String.valueOf(volume);
    }
//    public static void main(String[] args) {
//
//        Map<Integer, Integer> preset = new HashMap<>();
//        preset.put(7, 5);
//        preset.put(8, 10);
//        preset.put(22, 5);
//        preset.put(22, 5);
//
//        Map<String, Integer> presets = new HashMap<>();
//        presets.put("morn", 5);
//        presets.put("day", 15);
//        presets.put("night", 5);
//        presets.put("low", 10);
//        presets.put("norm", 20);
//        presets.put("high", 30);
//
//        log.info("TIME " + LocalTime.now().getHour());
//
//        Integer time = LocalTime.now().getHour();
//        log.info("TIME " + time);
//        Integer volume = null;
//
//        LocalTime time1;
//
//        time1 = LocalTime.now();
//        System.out.println(time1);
//
//        if (LocalTime.now().isAfter(LocalTime.of(7, 00)) &&
//                LocalTime.now().isAfter(LocalTime.of(9, 00))) {
//            volume = presets.get("morn");
//        }
//        if (LocalTime.now().isAfter(LocalTime.of(9, 00)) &&
//                LocalTime.now().isAfter(LocalTime.of(20, 30))) {
//            volume = presets.get("day");
//        }
//        if (LocalTime.now().isAfter(LocalTime.of(22, 00)) &&
//                LocalTime.now().isAfter(LocalTime.of(7, 00))) {
//            volume = presets.get("night");
//        }
//
//        System.out.println(volume);
//
//    }


}