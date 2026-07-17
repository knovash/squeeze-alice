package org.knovash.squeezealice.yandex;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.http.HttpResponseResult;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.Main.*;

@Log4j2
public class YandexTTS {

    private static final HttpClientWrapper httpClient = new HttpClientWrapper();

    public static String textToVoiceFile(String text) {
        log.info("TEXT: " + text);
        String outputFile = "/home/music/speech.mp3";
        try {
            byte[] audioData = synthesize(text);
            saveToFile(audioData, outputFile);

            new File(outputFile).setReadable(true, false);

            log.info("NOTIFICATION AMP FFMPEG " + lmsPlayers.volumeAmpFfmpeg);
            if (lmsPlayers.volumeAmpFfmpeg) {
                log.info("VOLUME AMP FFMPEG");
                // Изменение громкости (коэффициент 1.5)
                double volumeGain = 3; // можно вынести в config
                try {
                    changeVolume(outputFile, outputFile, volumeGain);
                } catch (Exception e) {
                    log.error("Не удалось изменить громкость: {}", e.getMessage());
                    // Продолжаем с исходным файлом
                }
            }else log.info("SKIP VOLUME AMP FFMPEG");

        } catch (Exception e) {
            log.error("❌ Ошибка при синтезе или сохранении: {}", e.getMessage(), e);
            return null;
        }
        String httpUrl = "http://" + Main.myIp + ":8010/music/speech.mp3";
        log.info("VOICE:" + httpUrl);
        return httpUrl;
    }

    public static byte[] synthesize(String text) throws IOException {
        String urlString = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";
        String params = "text=" + URLEncoder.encode(text, "UTF-8")
                + "&lang=ru-RU"
                + "&voice=oksana"
                + "&emotion=neutral"
                + "&format=mp3"
                + "&sampleRateHertz=48000";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Api-Key " + config.yandexSstTttsApiKey);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        byte[] body = params.getBytes(StandardCharsets.UTF_8);
        HttpResponseResult result = httpClient.doPostBytes(urlString, body, headers);

        if (result.isSuccess() && result.getBodyBytes() != null) {
            return result.getBodyBytes();
        } else {
            String errorMsg = "TTS request failed with code " + result.getStatusCode() + ", body: " + result.getBody();
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }
    }

    public static void saveToFile(byte[] data, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        }
    }

    public static void waitForPlaybackCompletion(Player player, int timeoutMaxSeconds) {
        log.info(start);
        int attempts = timeoutMaxSeconds / 2;
        for (int i = 0; i < attempts; i++) {
            String mode = player.mode();
            if (mode == null) {
                log.warn("Не удалось получить статус плеера, прерываем ожидание");
                break;
            }
            if (!"play".equals(mode)) {
                log.info(finish);
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

    public static void changeVolume(String inputFile, String outputFile, double gain) throws IOException, InterruptedException {
        File in = new File(inputFile);
        if (!in.exists()) {
            throw new FileNotFoundException("Входной файл не найден: " + inputFile);
        }

        // Создаём временный файл в той же директории
        File tempFile = File.createTempFile("speech_", ".mp3", in.getParentFile());
        String tempFilePath = tempFile.getAbsolutePath();

        String[] command = {
                "ffmpeg",
                "-i", inputFile,
                "-af", "volume=" + gain,
                "-y",
                tempFilePath
        };

//        log.info("Запуск ffmpeg: " + String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true); // объединяем stderr и stdout
        Process process = pb.start();

        // Читаем вывод (для логирования и диагностики ошибок)
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("ffmpeg: " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // Удаляем временный файл, если он создан
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw new IOException("ffmpeg завершился с кодом " + exitCode + ". Вывод:\n" + output.toString());
        }

        // Проверяем, что временный файл создан и не пуст
        if (!tempFile.exists() || tempFile.length() == 0) {
            tempFile.delete();
            throw new IOException("Временный файл не создан или пуст: " + tempFilePath);
        }

        // Перемещаем временный файл на место выходного (перезаписываем)
        File out = new File(outputFile);
        // Если выходной файл существует, удаляем его
        if (out.exists()) {
            if (!out.delete()) {
                // Если не удалось удалить, пробуем перезаписать с помощью Files.copy
                Files.copy(tempFile.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFile.delete();
                out.setReadable(true, false);
                log.info("Громкость изменена: " + inputFile + " -> " + outputFile + " (коэффициент " + gain + ")");
                return;
            }
        }
        // Переименовываем временный файл
        if (!tempFile.renameTo(out)) {
            // Если renameTo не работает (например, разные файловые системы), копируем
            Files.copy(tempFile.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.delete();
        }
        out.setReadable(true, false);
        log.info("Громкость изменена: " + inputFile + " -> " + outputFile + " (коэффициент " + gain + ")");
    }
}