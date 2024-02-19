package org.knovash.squeezealice.utils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;

@Log4j2
@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class Levenstein {
//    https://stackoverflow.com/questions/13564464/problems-with-levenshtein-algorithm-in-java

    public static String findVal;
    public static int filalDistance;
    public static String finalElement;

    public static String getNearestElementInList(String value, List<String> list) {
        filalDistance = 100;
        if (Utils.isCyrillic(value) && !Utils.isCyrillic(list.get(0))) {
            value = Utils.convertCyrilic(value);
        }
        String finalValue = value;
        if (value.contains(" ")) {
            list.stream().forEach(name -> Levenstein.compareWordAndWord(finalValue, name));
        } else {
            list.stream().forEach(name -> Levenstein.compareWordAndFrase(finalValue, name));
        }
        if (filalDistance > 5) finalElement = null;
        return finalElement;
    }

    public static int compareWordAndFrase(String val, String frase) {
        int d = Arrays.stream(frase.split(" "))
                .map(word -> dist(val.toLowerCase().toCharArray(), word.toLowerCase().toCharArray()))
                .sorted().findFirst().get();
        log.info(val + " & " + frase + " distance= " + d);
        if (d < filalDistance) {
            filalDistance = d;
            finalElement = frase;
            log.info("FD " + filalDistance + " FE " + finalElement);
        }
        return d;
    }

    public static int compareWordAndWord(String val, String s2) {
        int d = dist(val.toLowerCase().toCharArray(), s2.toLowerCase().toCharArray());
        log.info(val + " & " + s2 + " distance= " + d);
        if (d < filalDistance) {
            filalDistance = d;
            finalElement = s2;
            log.info("FD " + filalDistance + " FE " + finalElement);
        }
        return d;
    }

    public static int dist(char[] s1, char[] s2) {
        // distance matrix - to memoize distances between substrings
        // needed to avoid recursion
        int[][] d = new int[s1.length + 1][s2.length + 1];
        // d[i][j] - would contain distance between such substrings:
        // s1.subString(0, i) and s2.subString(0, j)
        for (int i = 0; i < s1.length + 1; i++) {
            d[i][0] = i;
        }
        for (int j = 0; j < s2.length + 1; j++) {
            d[0][j] = j;
        }
        for (int i = 1; i < s1.length + 1; i++) {
            for (int j = 1; j < s2.length + 1; j++) {
                int d1 = d[i - 1][j] + 1;
                int d2 = d[i][j - 1] + 1;
                int d3 = d[i - 1][j - 1];
                if (s1[i - 1] != s2[j - 1]) {
                    d3 += 1;
                }
                d[i][j] = Math.min(Math.min(d1, d2), d3);
            }
        }
        return d[s1.length][s2.length];
    }
}
