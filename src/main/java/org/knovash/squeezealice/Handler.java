package org.knovash.squeezealice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

public class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        System.out.println("\nMETHOD " + httpExchange.getRequestMethod());
        System.out.println("HEADERS " + httpExchange.getRequestHeaders().values());
        System.out.println("URI " + httpExchange.getRequestURI());
        System.out.println("URI " + httpExchange.getRequestURI().getQuery());

        String query = httpExchange.getRequestURI().getQuery();
        HashMap<String, String> parameters = new HashMap<>();
        Arrays.asList(query.split("&"))
                .stream()
                .peek(System.out::println)
                .map(s -> s.split("="))
                .forEach(s -> parameters.put(s[0], s[1]));
        System.out.println("\nMAP\n" + parameters);

        String action = String.valueOf(parameters.get("action"));

        System.out.println("ACTION: " + action);

        switch (action) {
            case ("play"):
                System.out.println("play");
                break;
            case ("stop"):
                System.out.println("stop");
                break;
            case ("stopall"):
                System.out.println("stopall");
                break;
            case ("separate"):
                System.out.println("separate");
                break;
            case ("alone"):
                System.out.println("alone");
                break;
            case ("syncto"):
                System.out.println("syncto");
                break;
            case ("volume"):
                System.out.println("volume");
                Player.volume(parameters.get("player"),parameters.get("value"));
                break;
            case ("volumedn"):
                System.out.println("volumedn");
                break;
            case ("volumeset"):
                System.out.println("volumeset");
                break;
            case ("volumehigh"):
                System.out.println("volumehigh");
                break;
            case ("volumelow"):
                System.out.println("volumelow");
                break;
            case ("channelplay"):
                System.out.println("channelplay");
                break;
            case ("select"):
                System.out.println("select");
                break;
            case ("off"):
                System.out.println("off");
                break;
            case ("spotify"):
                System.out.println("spotify");
                break;
            default:
                System.out.println("default");
                break;
        }


//        if ("GET".equals(httpExchange.getRequestMethod())) {
//            System.out.println("GET");
//            System.out.println(httpExchange.getRequestMethod());
//
//        } else if ("POST".equals(httpExchange)) {
//            System.out.println("POST");
//            requestParamValue = handleGetRequest(httpExchange);
//        }

        handleResponse(httpExchange);
    }

    private String handleGetRequest(HttpExchange httpExchange) {
        return httpExchange
                .getRequestURI()
                .toString()
                .split("\\?")[1]
                .split("=")[1];
    }

    private void handleResponse(HttpExchange httpExchange) throws IOException {
        OutputStream outputStream = httpExchange.getResponseBody();
        String response = "{value\":\"100\"}";
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.sendResponseHeaders(200, response.length());
        System.out.println(httpExchange.getResponseBody().toString());
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}