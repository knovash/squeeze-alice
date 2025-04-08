package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Mask {

    public static String mask(String text, Integer percentCenterHide) {
        if (text == null) {
            return null;
        }
        Integer length = text.length();
        Integer centerHide = percentCenterHide * length / 100; // сколько символов скрыть по центру
        Integer egeShow = (length - centerHide) / 2; // сколько символов оставить по краям
//        log.info("L=" + length + "H%=" + percentCenterHide + "%  EGE=" + egeShow);
        String leftShow = text.substring(0, egeShow);
        String rightShow = text.substring(text.length() - egeShow);
        String hide =(".").repeat(centerHide);
        String result = leftShow + hide + rightShow;
        return result;
    }
}
