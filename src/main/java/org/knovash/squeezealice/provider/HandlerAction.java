package org.knovash.squeezealice.provider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.provider.pojo.Device;
import org.knovash.squeezealice.provider.pojo.Payload;
import org.knovash.squeezealice.provider.pojo.ResponseQuery;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class HandlerAction implements HttpHandler {

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
        log.info("HEAD: " + head);
        log.info("QUERY: " + query);


        ResponseQuery responseQuery = new ResponseQuery();
        responseQuery.request_id = "33";
        Device device1 = new Device();
        device1.id = "11";
        Device device2 = new Device();
        device2.id = "22";
        Device[] dd = new Device[2];
        dd[0] =device1;
        dd[1] =device2;
        log.info(dd[0]);
        Payload payload = new Payload();
        payload.devices = dd;
        log.info(payload);
        responseQuery.payload = payload;
        log.info(responseQuery);
        response = JsonUtils.pojoToJson(responseQuery);

        log.info("RESPONSE: " + response);
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(response.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}

