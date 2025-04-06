package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerHtml implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER HTML START >>>>>>>>>>>>>>>");

        ContextForm context = ContextForm.contextCreate(httpExchange);
        if (context.path.equals("/html/manual")) {

            try {
                byte[] response = getResource("manual.html");
                httpExchange.getResponseHeaders().set("Content-Type", "text/html");
                httpExchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (IOException e) {
                httpExchange.sendResponseHeaders(404, 0);
                httpExchange.close();
            }
        } else {
            String path = context.path;
            String image = path.replaceAll(".*html/", "");
            log.info("IMAGE:" + image);

            try {
                byte[] response = getResource(image);
                httpExchange.getResponseHeaders().set("Content-Type", "image/png");
                httpExchange.sendResponseHeaders(200, response.length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response);
                }
            } catch (IOException e) {
                httpExchange.sendResponseHeaders(404, 0);
                httpExchange.close();
            }
        }


        log.info("HANDLER HTML FINISH <<<<<<<<<<<<<<<");
    }

    private static byte[] getResource(String filename) throws IOException {
        return Server.class.getClassLoader()
                .getResourceAsStream(filename)
                .readAllBytes();
    }
}