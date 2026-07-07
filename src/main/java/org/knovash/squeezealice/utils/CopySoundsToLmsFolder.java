package org.knovash.squeezealice.utils;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.yandex.YandexTTS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Log4j2
public class CopySoundsToLmsFolder {

    public static void copySoundsToLmsFolder() {
        log.info("COPY SOUND FILES");
        String targetDir = "/home/music/";
        File targetFolder = new File(targetDir);
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            log.error("Не удалось создать папку: {}", targetDir);
            return;
        }

        URL soundsUrl = YandexTTS.class.getClassLoader().getResource("sounds");
        if (soundsUrl == null) {
            log.warn("Папка 'sounds' не найдена в ресурсах");
            return;
        }

        try {
            if ("file".equals(soundsUrl.getProtocol())) {
                // Режим разработки (IDE) – файлы лежат на диске
                File sourceFolder = new File(soundsUrl.toURI());
                copyFolderFromFileSystem(sourceFolder, targetFolder);
            } else if ("jar".equals(soundsUrl.getProtocol())) {
                // Режим работы из JAR
                copyFolderFromJar(soundsUrl, targetDir);
            } else {
                log.warn("Неизвестный протокол ресурса: {}", soundsUrl.getProtocol());
            }
        } catch (Exception e) {
            log.error("Ошибка при копировании папки sounds: {}", e.getMessage(), e);
        }
    }

    /**
     * Копирование папки из файловой системы (для IDE).
     */
    private static void copyFolderFromFileSystem(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists() && !target.mkdirs()) {
                throw new IOException("Не удалось создать папку " + target.getAbsolutePath());
            }
            File[] children = source.listFiles();
            if (children != null) {
                for (File child : children) {
                    copyFolderFromFileSystem(child, new File(target, child.getName()));
                }
            }
        } else {
            Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            target.setReadable(true, false);
            log.info("Скопирован файл: {}", target.getAbsolutePath());
        }
    }

    /**
     * Копирование папки из JAR-архива.
     */
    private static void copyFolderFromJar(URL jarUrl, String targetDir) throws IOException, URISyntaxException {
        // Извлекаем путь к JAR-файлу
        String urlPath = jarUrl.getPath();
        String jarPath = urlPath.substring(5, urlPath.indexOf("!"));
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            String prefix = "sounds/";
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(prefix) && !entry.isDirectory()) {
                    String relativePath = name.substring(prefix.length());
                    File targetFile = new File(targetDir, relativePath);
                    // Создаём родительские папки, если необходимо
                    File parent = targetFile.getParentFile();
                    if (parent != null && !parent.exists() && !parent.mkdirs()) {
                        log.warn("Не удалось создать папку: {}", parent.getAbsolutePath());
                    }
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        targetFile.setReadable(true, false);
                        log.info("Скопирован файл из JAR: {}", relativePath);
                    }
                }
            }
        }
    }
}