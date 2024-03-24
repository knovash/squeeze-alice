package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Log4j2
public class MainTest {


    private static Object TimeUnit;

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

  timer();

    }

    public static void timer(){
        Runnable drawRunnable = new Runnable() {
            public void run() {
                System.out.println("-----");
            }
        };
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(drawRunnable, 0, 5, java.util.concurrent.TimeUnit.SECONDS);
    }


}