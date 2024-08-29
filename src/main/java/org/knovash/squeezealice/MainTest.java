package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.knovash.squeezealice.provider.Yandex.yandex;

@Log4j2
public class MainTest {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("config");
    public static String lmsIp = bundle.getString("lmsIp");
    public static String lmsPort = bundle.getString("lmsPort");
    public static String lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
    public static String silence = bundle.getString("silence");
    public static int port = Integer.parseInt(bundle.getString("port"));
    public static LmsPlayers lmsPlayers = new LmsPlayers();
    public static Map<String, String> config = new HashMap<>();
    public static Map<String, String> rooms = new HashMap<>();

    public static void main(String[] args) {
        log.info("START");

//        runInFuture(() -> func1());
//        runInFuture(MainTest::func2);

//        CompletableFuture.supplyAsync(() -> {
//            try {
//                Request.Post("https://api.iot.yandex.net/v1.0/scenarios/f2ddb649-62e7-4fe2-be01-23d477dd2974/actions")
//                        .setHeader("Authorization", "OAuth " + yandex.bearer)
//                        .execute();
//            } catch (IOException e) {
//                log.info("SAY ERROR");
//            }
//            return "";
//        });


        CompletableFuture.runAsync(() -> func1());
        CompletableFuture.runAsync(() -> func2());


    }


    public static Boolean func1() {
        log.info("SAY ERRO45345345345R");
        return true;
    }

    public static Boolean func2() {
        log.info("SAY ERROdfgdfgdfgdfgdR");
        return true;
    }

    public static <T> void runInFuture(Predicate<T> function) {
        function.test(null);
    }


    public static void complete() {
        CompletableFuture.supplyAsync(() -> {
            try {
                Request.Post("https://api.iot.yandex.net/v1.0/scenarios/f2ddb649-62e7-4fe2-be01-23d477dd2974/actions")
                        .setHeader("Authorization", "OAuth " + yandex.bearer)
                        .execute();
            } catch (IOException e) {
                log.info("SAY ERROR");
            }
            return "";
        });
    }


}