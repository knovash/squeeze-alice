package org.knovash.squeezealice.utils;

import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Data
public class Levenstein {
//    https://stackoverflow.com/questions/13564464/problems-with-levenshtein-algorithm-in-java

    public static String findVal;
    public static int filalDistance;
    public static String finalElement;

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

    public static int getDistanceWordAndWord(String word1, String word2) {
//        log.info("START " + val);
        if (Utils.isCyrillic(word1) && !Utils.isCyrillic(word2)) word1 = Utils.convertCyrilic(word1);
        int d = dist(word1.toLowerCase().toCharArray(), word2.toLowerCase().toCharArray());
//        log.info(word1 + " & " + word2 + " DISTANCE: " + d);
        if (d < filalDistance) {
            filalDistance = d;
            finalElement = word2;
//            log.info("NEAREST: " + finalElement + " DISTANCE: " + filalDistance);
        }
        return d;
    }

    public static String compareWordAndWord(String wordNearest, String wordCorrect) {
        int dist = getDistanceWordAndWord(wordNearest, wordCorrect);
        if (dist < 3) return wordCorrect;
        return null;
    }

    public static Boolean compareWordAndWordBool(String wordNearest, String wordCorrect) {
        int dist = getDistanceWordAndWord(wordNearest, wordCorrect);
        if (dist < 3) return true;
        return false;
    }

    public static String getNearestElementInListWord(String value, List<String> list) {
//        log.info("SEARCH: '" + value + "' IN: " + list);
        filalDistance = 100;
        list.stream().forEach(name -> Levenstein.getDistanceWordAndWord(value, name));
        if (filalDistance > 2) finalElement = null;
//        log.info("RESULT: " + finalElement + " DISTANCE: " + filalDistance);
        return finalElement;
    }

    public static String search(String text, List<String> list) {
        log.info("SEARCH TEXT: " + text + " IN: " + list);

        List<String> words = Stream.of(text.split(" "))
                .filter(w -> w != "")
                .filter(w -> w != " ")
                .filter(w -> !(w.length() < 3))
                .collect(Collectors.toList());

//        log.info("WORDS: " + words + " SIZE: " + words.size());
        String result = null;
        result = words.stream()
                .map(w -> Levenstein.getNearestElementInListWord(w, list))
                .filter(w -> w != null)
                .findFirst().orElse(null);
//        log.info("RESULT: " + result);

        if (result == null)
            result = words.stream()
                    .filter(w -> words.indexOf(w) + 1 != words.size())
                    .map(w -> w + " " + words.get(1 + words.indexOf(w)))
                    .map(w -> Levenstein.getNearestElementInListWord(w, list))
                    .filter(w -> w != null)
                    .findFirst().orElse(null);
        log.info("RESULT: " + result);
        return result;
    }

//    mission control & Mission Control: Celebrating NASA and Space Explorers everywhere.
    public static String searchShortInLong(String shortName, String longName) {
        log.info(">>>> " + shortName + " IN " + longName);
        String result = null;
        List<String> shortWords = List.of(shortName.split(" "));
        List<String> longWords = List.of(longName.split(" "));
        int shortSize = shortWords.size();
        int longSize = longWords.size();
//        log.info("SHORT: " + shortWords.size() + " " + shortWords + " IN LONG: " + longWords.size() + " " + longWords);
        result = longWords.stream()
                .filter(w -> longWords.indexOf(w) + shortSize - 1 < longWords.size())
                .map(w -> joinWords(longWords, w, shortSize))
                .map(w -> Levenstein.compareWordAndWord(shortName, w))
                .filter(w -> w != null)
                .findFirst().orElse(null);
//        log.info("<<<< RESULT: " + result);
        if (result != null) return longName;
        return null;
    }

    public static String joinWords(List<String> words, String first, int count) {
//        log.info(words + " " + first + " " +count);
        int start = words.indexOf(first);
        List<String> sub = words.subList(start, start + count);
//        log.info(sub);
        String joinedWords = String.join(" ", sub);
        return joinedWords;
    }

    public static String searchTitleInFavorites(String title, List<String> favorites) {
        log.info("TITLE: " + title);
        log.info("FAVORITES: " + favorites);
        String result = "---";
        result = favorites.stream()
                .map(f -> searchShortInLong(title, f))
                .filter(f -> f != null)
                .findFirst()
                .orElse(null);
        log.info("RESULT: " + result);
        return result;
    }
}