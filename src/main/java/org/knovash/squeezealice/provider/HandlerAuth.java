package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.Utils;
import org.knovash.squeezealice.provider.pojo.Auth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

@Log4j2
public class HandlerAuth implements HttpHandler {

//    https://yandex.ru/dev/dialogs/smart-home/doc/reference/resources.html

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String head;
        String query;
        String response;
        log.info("");
        log.info(" ---===[ REQUEST ]===---");
        head = httpExchange.getRequestHeaders().values().toString();
        query = httpExchange.getRequestURI().getQuery();
//        query = httpExchange.getRequestBody()
        log.info("HEAD: " + head);
        log.info("QUERY: " + query);


        HashMap<String, String> parameters = Utils.getQueryParameters(query);

        parameters.entrySet().stream().forEach(p -> log.info(p.toString()));

        Auth auth = new Auth();
        auth.client_id = parameters.get("client_id");
        auth.redirect_uri = parameters.get("redirect_uri");
        auth.response_type = parameters.get("response_type");
        auth.scope = parameters.get("scope");
        auth.state = parameters.get("state");

        String jsonAuthResponse = JsonUtils.pojoToJson(auth);



//        String jsontoken =
//                "{\"access_token\":\"BQAOe_yDYLAU1XlNZn0YWJ7Tv6RywoplSH4c3vNFP8ClmSlJF5vV8iJS5F1JG1NBI6Q-3VK8Tzlz_Sq_ahHuywNQaGOTLKm7tf-BSBzdQV5uyufBvBU\"," +
//                        " \"token_type\":\"Bearer\", \"expires_in\":3600}";

        response = jsonAuthResponse;
//        response = "AUTH";

        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

