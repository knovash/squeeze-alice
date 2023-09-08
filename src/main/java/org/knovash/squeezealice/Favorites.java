package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Response;
import org.knovash.squeezealice.Fluent;
import org.knovash.squeezealice.JsonUtils;
import org.knovash.squeezealice.requests.Loop;
import org.knovash.squeezealice.requests.Requests;
import org.knovash.squeezealice.requests.ResponseFromLms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class Favorites {

    public String fav_number;
    public String fav_title;
    public String fav_url;

//    public static void main(String[] args) {
//        getFavoritesFromServer();
//    }

    public static void getFavoritesFromServer() {
        String json = "{\"id\": 1, \"method\": \"slim.request\", \"params\":[\"homepod\", [\"favorites\", \"items\", \"0\",\"15\",\"want_url:1\"]]}";

        Response response = Fluent.post(json);
        Content content;
        HttpResponse httpResponse;
        try {
            content = response.returnContent();
            httpResponse = response.returnResponse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ResponseFromLms responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        log.info("RESPONSE: " + responseFromLms);
        log.info("SATUS: " + httpResponse.getStatusLine());
//        return re.resultFromLms._path;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response.toString());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        log.info("\nNODE: \n" + jsonNode);
        String node = String.valueOf(jsonNode.findValue("loop_loop"));
//        log.info("\nNODEget: \n" + node);
        List<Loop> loops = new ArrayList<>();
        loops = JsonUtils.jsonToList(node, Loop.class);
//        loops.stream().forEach(loop -> log.info(loop.id + " " + loop.name + " " + loop.url));
        loops.stream().forEach(loop -> loop.setId(loop.id.replaceFirst(".*\\.", "")));
        loops.stream().forEach(loop -> log.info(loop.id + " " + loop.name + " " + loop.url));
        log.info("FILTER " + loops.stream().filter(loop -> loop.id.equals("3")).findFirst().orElse(null).name);

        JsonUtils.pojoToJsonFile(loops, "fav.json");


    }

    public static boolean checkExists(Integer index) {
        log.info("CHECK FAVORITE " + index + " EXISTS ON SERVER");
        return true;
    }
}