package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.Utils;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.knovash.squeezealice.Main.config;

@Log4j2
public class LmsSearchForIp {

    private static final int TIMEOUT = 1000;
    private static final String NETWORK_PREFIX = "192.168.1.";

    public static String findServerIp() {

//        проверить ip из конфига
        log.info("CHECK LMS IP FROM CONFIG: " + config.lmsIp + " " + config.lmsPort);
        if (Utils.checkIpIsLms(config.lmsIp)) return config.lmsIp;
        else {
            log.info("LMS NOT FOUND AT IP FROM CONFIG: " + config.lmsIp + " " + config.lmsPort);
        }

//        проверить ip этого компа
        String myip = Utils.getMyIpAddres();
        log.info("CHECK LMS IP FROM THIS HOST: " + myip + " " + config.lmsPort);
        if (Utils.checkIpIsLms(myip)) return myip;
        else {
            log.info("LMS NOT FOUND AT IP FROM THIS HOST: " + myip + " " + config.lmsPort);
        }

//        искать ip в локальной сети
        log.info("SEARCH LMS IP IN NETWORK. WAIT...");
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        AtomicReference<String> foundIp = new AtomicReference<>();

        // Проверяем диапазон 192.168.1.1-192.168.1.254
        for (int i = 50; i <= 150; i++) {
            String ip = NETWORK_PREFIX + i;
            executor.execute(() -> checkIp(ip, config.lmsPort, foundIp));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String tryIp = foundIp.get();
        log.info("RESULT: " + tryIp);
        if (tryIp != null) {
            log.info("LMS FOUND IN NETWORK " + tryIp);
        } else {

            log.info("LMS NOT FOUND IN NETWORK " + tryIp + " " + config.lmsPort);
        }

        return tryIp;
    }

    private static void checkIp(String ip, String port, AtomicReference<String> foundIp) {
        log.info("TRY IP: " + ip + " PORT: " + config.lmsPort);
        if (foundIp.get() != null) return;

        try (Socket socket = new Socket()) {
            // Быстрая проверка доступности порта
            socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), TIMEOUT);

            // Проверяем HTTP-заголовок
            URL url = new URL("http://" + ip + ":" + port);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);

            String serverHeader = conn.getHeaderField("Server");
            log.info("IP: " + ip + " PORT: " + port + " HEADER: " + serverHeader);
            if (serverHeader != null && (serverHeader.contains("Lyrion Music Server") || serverHeader.contains("Lyrion Music Server"))) {
                foundIp.compareAndSet(null, ip);
            }
        } catch (IOException e) {
            // Игнорируем ошибки подключения
        }
    }
}