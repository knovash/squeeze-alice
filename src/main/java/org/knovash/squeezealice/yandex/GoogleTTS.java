package org.knovash.squeezealice.yandex;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Main;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.http.HttpClientWrapper;
import org.knovash.squeezealice.http.HttpResponseResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.knovash.squeezealice.web.SettingsVoice.KeyString.*;
import static org.knovash.squeezealice.web.SettingsVoice.KeyToggle.*;
import static org.knovash.squeezealice.web.SettingsVoice.KeyValue.*;


@Log4j2
public class GoogleTTS {

    private static final HttpClientWrapper httpClient = new HttpClientWrapper();

    public static double duration;

    public static String textToVoiceFile(String text) {
        log.info("Google TTS TEXT: " + text);
        String outputFile = "/home/music/speech_google.mp3";
        try {
            byte[] audioData = synthesize(text);
            saveToFile(audioData, outputFile);
            new File(outputFile).setReadable(true, false);
            volumeAmp(outputFile);
        } catch (Exception e) {
            log.error("❌ Ошибка при синтезе Google TTS или сохранении: {}", e.getMessage(), e);
            return null;
        }

        try {
            duration = getDuration(outputFile);
        } catch (IOException | InterruptedException e) {
            log.error("Не удалось получить длительность: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        log.info("********* DURATION " + duration);

        String httpUrl = "http://" + Main.myIp + ":8010/music/speech_google.mp3";
        log.info("VOICE:" + httpUrl);
        return httpUrl;
    }

    public static void volumeAmp(String outputFile) {
        log.info("NOTIFICATION AMP FFMPEG " + VOICE_TOGGLE_VOLUME_AMP_FFMPEG.value);
        if (VOICE_TOGGLE_VOLUME_AMP_FFMPEG.value) {
            log.info("VOLUME AMP FFMPEG");
            double volumeGain = VOICE_VALUE_VOLUME_APM_FFMPEG.value;
            try {
                changeVolume(outputFile, outputFile, volumeGain);
            } catch (Exception e) {
                log.error("Не удалось изменить громкость: {}", e.getMessage());
            }
        } else {
            log.info("SKIP VOLUME AMP FFMPEG");
        }
    }

    public static byte[] synthesize(String text) throws IOException {
        log.info("VALUE_GOOGLE_TTS " + VOICE_VALUE_VOLUME_AMP_GOOGLE_TTS.value);
        String jsonPayload = String.format(
                "{" +
                        "\"input\":{\"text\":\"%s\"}," +
                        "\"voice\":{\"languageCode\":\"ru-RU\",\"name\":\"ru-RU-Standard-A\",\"ssmlGender\":\"FEMALE\"}," +
                        //"\"audioConfig\":{\"audioEncoding\":\"MP3\",\"volumeGainDb\":5.0}" +
                        "\"audioConfig\":{\"audioEncoding\":\"MP3\",\"volumeGainDb\":" +
                        VOICE_VALUE_VOLUME_AMP_GOOGLE_TTS.value
                        + "}" +
                        "}",
                escapeJson(text)
        );

        String apiKey = VOICE_STRING_GOOGLE_API_KEY.value;
        String urlString = "https://texttospeech.googleapis.com/v1/text:synthesize?key=" + apiKey;

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");

        byte[] body = jsonPayload.getBytes(StandardCharsets.UTF_8);
        HttpResponseResult result = httpClient.doPostBytes(urlString, body, headers);

        if (!result.isSuccess() || result.getBodyBytes() == null) {
            String errorMsg = "Google TTS request failed with code " + result.getStatusCode() +
                    ", body: " + result.getBody();
            log.error(errorMsg);
            throw new IOException(errorMsg);
        }

        String responseBody = new String(result.getBodyBytes(), StandardCharsets.UTF_8);
        String audioContent = extractAudioContent(responseBody);
        if (audioContent == null) {
            throw new IOException("Не удалось найти audioContent в ответе: " + responseBody);
        }

        return Base64.getDecoder().decode(audioContent);
    }

    /**
     * Извлекает значение поля "audioContent" из JSON-ответа.
     */
    private static String extractAudioContent(String json) {
        int keyIndex = json.indexOf("\"audioContent\"");
        if (keyIndex == -1) return null;
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        int startQuote = json.indexOf("\"", colonIndex);
        if (startQuote == -1) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    /**
     * Экранирует кавычки и обратные слеши в тексте для JSON.
     */
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static void saveToFile(byte[] data, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(data);
        }
    }

    public static void waitForPlaybackCompletion(Player player, int timeoutMaxSeconds) {
        log.info("Ожидание завершения воспроизведения...");
        int attempts = timeoutMaxSeconds / 2;
        for (int i = 0; i < attempts; i++) {
            String mode = player.mode();
            if (mode == null) {
                log.warn("Не удалось получить статус плеера, прерываем ожидание");
                break;
            }
            if (!"play".equals(mode)) {
                log.info("Воспроизведение завершено");
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

    // Методы changeVolume и getDuration полностью скопированы из YandexTTS

    public static void changeVolume(String inputFile, String outputFile, double gain) throws IOException, InterruptedException {
        File in = new File(inputFile);
        if (!in.exists()) {
            throw new FileNotFoundException("Входной файл не найден: " + inputFile);
        }

        File tempFile = File.createTempFile("speech_", ".mp3", in.getParentFile());
        String tempFilePath = tempFile.getAbsolutePath();

        String[] command = {
                "ffmpeg",
                "-i", inputFile,
                "-af", "volume=" + gain,
                "-y",
                tempFilePath
        };

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

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
            if (tempFile.exists()) {
                tempFile.delete();
            }
            throw new IOException("ffmpeg завершился с кодом " + exitCode + ". Вывод:\n" + output.toString());
        }

        if (!tempFile.exists() || tempFile.length() == 0) {
            tempFile.delete();
            throw new IOException("Временный файл не создан или пуст: " + tempFilePath);
        }

        File out = new File(outputFile);
        if (out.exists()) {
            if (!out.delete()) {
                Files.copy(tempFile.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFile.delete();
                out.setReadable(true, false);
                log.info("Громкость изменена: " + inputFile + " -> " + outputFile + " (коэффициент " + gain + ")");
                return;
            }
        }
        if (!tempFile.renameTo(out)) {
            Files.copy(tempFile.toPath(), out.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.delete();
        }
        out.setReadable(true, false);
        log.info("Громкость изменена: " + inputFile + " -> " + outputFile + " (коэффициент " + gain + ")");
    }

    public static double getDuration(String filePath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "ffprobe", "-v", "error", "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1", filePath
        );
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = reader.readLine();
        int exitCode = p.waitFor();
        if (exitCode != 0 || line == null || line.isEmpty()) {
            throw new IOException("Не удалось получить длительность файла: " + filePath);
        }
        return Double.parseDouble(line.trim());
    }
}