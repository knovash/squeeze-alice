package org.knovash.squeezealice.yandex;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
public class YandexTTS {

    public static void playerSay(Player player, String text, Boolean file, Boolean restoreTry) {
        if (player == null) return;
        log.info("PLAYER: " + player.name + " RESTORE: " + restoreTry + " TEXT: " + text + " FILE: " + file + " TOGGLE: " + lmsPlayers.toggleVoice);
        if (!lmsPlayers.toggleVoice) return;
        player.savePlaylistScript(); // сохранить плейлист перед уведомлением
        int volumeNotification = player.valueVolumeByTime() + 10; // громкость уведомления
        if (volumeNotification > 50) volumeNotification = 50;
        player.unsync();
        player.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null); // разбудить плеер если неиграет
        player.pause();
        player.volumeSet(String.valueOf(volumeNotification)); // установить громкость уведомления

        if (file)
            player.playFile("http://" + Main.myIp + ":8010/music/sounds/" + text + ".mp3");// воспроизвести файл звук
        else
            player.playFile(textToVoiceFile(text));// синтез и воспроизвести файл голоса

        player.saveLastTime();
        player.waitSeconds(1); // минимальная длина звука, потом опрос
        waitForPlaybackCompletion(player, 40); // опрос дождаться конца уведомления
        player.volumeSet(player.savedPlaylistVolume); // установить громкость до уведомления
        if (!restoreTry) return;
        player.restorePlaylistScript(); // востановить плейлист до уведомления
    }

    public static String textToVoiceFile(String text) {
        log.info("START VOICE FILE FROM TEXT: " + text);
        String outputFile = "/home/music/speech.mp3";
        try {
            byte[] audioData = synthesize(text);
            saveToFile(audioData, outputFile);
            log.info("VOICE FILE SAVED " + outputFile);
            new File(outputFile).setReadable(true, false);  // Даём права на чтение всем (LMS работает от пользователя squeezeboxserver)
        } catch (Exception e) {
            log.error("❌ Ошибка при синтезе или сохранении: {}", e.getMessage(), e);
            return null;
        }
        String httpUrl = "http://" + Main.myIp + ":8010/music/speech.mp3";
        log.info("FINISHED. VOICE FILE URL:" + httpUrl);
        return httpUrl;
    }

    public static byte[] synthesize(String text) throws IOException {
        String urlString = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Api-Key " + config.yandexSstTttsApiKey);
        conn.setDoOutput(true);
        String params = "text=" + URLEncoder.encode(text, "UTF-8")
                + "&lang=ru-RU"
                + "&voice=oksana"
                + "&emotion=neutral"
                + "&format=mp3"
                + "&sampleRateHertz=48000";
//        log.debug("Параметры запроса: {}", params);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes("UTF-8"));
            os.flush();
        }
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (InputStream is = conn.getInputStream()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] data = baos.toByteArray();
                StringBuilder hex = new StringBuilder();
                int show = Math.min(16, data.length);
                for (int i = 0; i < show; i++) {
                    hex.append(String.format("%02X ", data[i]));
                }
                return data;
            }
        } else {
            try (InputStream err = conn.getErrorStream()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(err));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String errorMsg = "ERROR CODE: " + responseCode + ", " + sb.toString();
                log.info(errorMsg);
                throw new IOException(errorMsg);
            }
        }
    }

    public static void saveToFile(byte[] data, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        }
    }

    private static void waitForPlaybackCompletion(Player player, int timeoutMaxSeconds) {
        log.info("WAIT FOR PLAYBACK FINISH...");
        int attempts = timeoutMaxSeconds / 2; // 500 мс на шаг
        for (int i = 0; i < attempts; i++) {
            String mode = player.mode();
            if (mode == null) {
                log.warn("Не удалось получить статус плеера, прерываем ожидание");
                break;
            }
            // Завершено, если режим stop или очередь пуста
            if (!"play".equals(mode)
            ) {
                log.info("FINISHED mode=" + mode);
                return;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.warn("Таймаут ожидания завершения уведомления ({} сек)", timeoutMaxSeconds);
    }
}