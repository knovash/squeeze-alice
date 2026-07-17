package org.knovash.squeezealice.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class HandlerHtml implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER HTML START >>>>>>>>>>>>>>>");

        Context context = Context.contextCreate(httpExchange);
        String path = context.path;

        // Обработка запроса к manual.html (ресурс внутри JAR)
        if (path.equals("/html/manual")) {
            serveManualPage(httpExchange);
            log.info("HANDLER HTML FINISH <<<<<<<<<<<<<<<");
            return;
        }

        // Все остальные запросы вида /html/имя_файла.png — считаем изображениями
        String fileName = path.replaceAll(".*html/", "");
        log.info("IMAGE REQUESTED: " + fileName);

        if (fileName.isEmpty()) {
            send404(httpExchange);
            log.info("HANDLER HTML FINISH <<<<<<<<<<<<<<<");
            return;
        }

        // Загружаем изображение из файловой системы (папка ./img/)
        byte[] imageData = loadImageFromFileSystem(fileName);
        if (imageData == null) {
            send404(httpExchange);
            log.info("HANDLER HTML FINISH <<<<<<<<<<<<<<<");
            return;
        }

        // Определяем MIME-тип по расширению
        String contentType = getContentType(fileName);
        httpExchange.getResponseHeaders().set("Content-Type", contentType);
        httpExchange.sendResponseHeaders(200, imageData.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(imageData);
        }

        log.info("HANDLER HTML FINISH <<<<<<<<<<<<<<<");
    }

    /**
     * Отдаёт страницу manual.html из classpath (ресурс, упакованный в JAR).
     */
    private void serveManualPage(HttpExchange httpExchange) throws IOException {
        try {
            byte[] response = loadResourceFromClasspath("manual.html");
            httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            httpExchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response);
            }
        } catch (IOException e) {
            log.error("Failed to load manual.html", e);
            send404(httpExchange);
        }
    }

    /**
     * Загружает ресурс из classpath (для manual.html).
     */
    private byte[] loadResourceFromClasspath(String filename) throws IOException {
        try (InputStream is = Server.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new IOException("Resource not found: " + filename);
            }
            return is.readAllBytes();
        }
    }

    /**
     * Загружает изображение из файловой системы из папки ./img/
     */
    private byte[] loadImageFromFileSystem(String filename) {
        try {
            // Путь: ./img/имя_файла (относительно текущей рабочей директории)
            Path imagePath = Paths.get(".", "img", filename);
            if (!Files.exists(imagePath)) {
                log.warn("Image file not found: " + imagePath.toAbsolutePath());
                return null;
            }
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            log.error("Error reading image file: " + filename, e);
            return null;
        }
    }

    /**
     * Определяет MIME-тип по расширению файла.
     */
    private String getContentType(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "application/octet-stream";
        }
        String ext = filename.substring(dotIndex + 1).toLowerCase();
        switch (ext) {
            case "png":  return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "gif":  return "image/gif";
            case "svg":  return "image/svg+xml";
            case "webp": return "image/webp";
            default:     return "application/octet-stream";
        }
    }

    /**
     * Отправляет ответ 404 и закрывает соединение.
     */
    private void send404(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(404, 0);
        httpExchange.close();
    }
}