package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Levenstein;
import org.knovash.squeezealice.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");


        String orig1 = "jazz";
        String orig2 = "techno";
        String orig3 = "chillout";
        String name1 = "jaz";
        String name2 = "jazz";
        String name3 = "tecno";
        String name4 = "techno";
        String name5 = "chill";

        List<String> playlist = new ArrayList<>();
        playlist.add("Deep Organic House");
        playlist.add("Smooth Jazz");
        playlist.add("Chillout");
        playlist.add("LoFi Hip-Hop");
        playlist.add("Atmospheric Breaks");
        playlist.add("Future Garage");
        playlist.add("Deep Progressive House");
        playlist.add("Melodic Progressive");
        playlist.add("Chillout Dreams");
        playlist.add("Drum and Bass");
        playlist.add("Deep Tech");
        playlist.add("Future Synthpop");
        playlist.add("Electropop");
        playlist.add("Dark PsyTrance");
        playlist.add("Rock");
        playlist.add("Techno");
        playlist.add("Undeground Techno");
        playlist.add("jazz");

        int dist = Levenstein.compareWordAndWord(orig1, name2);
        log.info(orig1 + " " + name2 + " " + dist);

        log.info(orig1 + " " + name1 + " " + Levenstein.compareWordAndWord(orig1, name2));
        log.info(orig2 + " " + name3 + " " + Levenstein.compareWordAndWord(orig2, name3));
        log.info(orig1 + " " + name1 + " " + Levenstein.compareWordAndWord(orig1, name2));
        log.info(orig1 + " " + name1 + " " + Levenstein.compareWordAndWord(orig1, name2));

//        String val = "джаз";
//        Levenstein.index = 100;
//        playlist.stream().forEach(n -> Levenstein.compareWordAndWord(val, n));
//        String valCyr = Utils.convertCyrilic(val);
//        playlist.stream().forEach(n -> Levenstein.compareWordAndWord(valCyr, n));
//
//        log.info(Levenstein.findVal);
//        log.info(Levenstein.index);
    }

}