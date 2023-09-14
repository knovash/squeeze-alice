package org.knovash.squeezealice;



import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

    public class Controller {


        public static void start() {
            HttpServer server = null;
            try {
                server = HttpServer.create(new InetSocketAddress(8000), 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server.createContext("/cmd", new MyHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("START");
        }

//        static class MyHandler implements HttpHandler {
//            @Override
//            public void handle(HttpExchange t) throws IOException {
//                String response = "This is the response";
//                t.sendResponseHeaders(200, response.length());
//                OutputStream os = t.getResponseBody();
//                os.write(response.getBytes());
//                os.close();
//            }
//        }

    }