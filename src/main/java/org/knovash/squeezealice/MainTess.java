package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.HttpUtils;
import org.knovash.squeezealice.provider.pojo.*;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class MainTess {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());


    public static void main(String[] args) throws JsonProcessingException {
        log.info("  ---+++===[ START ]===+++---");
        log.info("READ CONFIG FROM PROPERTIES");

//        ActionResult actionResult = new ActionResult();
//        actionResult.status = "DONE";
//
//        String sss = State.setState("volume", "7", "false");
//
//        log.info(sss);


//        String text = "{\"instance\" :\"channel\", \"relative\" :false, \"value\" :0, \"action_result\" :{\"status\" :\"DONE\", \"error_code\" :null, \"error_message\" :null}";
//        log.info(text);
//        Pattern pattern = Pattern.compile("value\\s*\"+\\s*:\\s*\\d*\\w*\\s*,");
//        Matcher matcher = pattern.matcher(text);
//        while (matcher.find()) {
//            text = text.substring(matcher.start(), matcher.end());
//        }
//        Pattern pattern2 = Pattern.compile(":\\s*\\d*\\w*");
//        Matcher matcher2 = pattern2.matcher(text);
//        while (matcher2.find()) {
//            text = text.substring(matcher2.start(), matcher2.end());
//        }
//        text = text.replaceAll(":\\s*","");
//        log.info(text);

        String json = "{\"payload\":{\"devices\":[{\"id\":\"0\",\"capabilities\":[{\"type\":\"devices.capabilities.range\",\"state\":{\"instance\":\"volume\",\"relative\":false,\"value\":67}}]}]}}";
        log.info(json);

//        ((ObjectNode)jsonNode).put("value", "NO");

        JsonNode jsonNode = null;

        jsonNode = objectMapper.readTree(json);

        ((ObjectNode) jsonNode
                .get("payload")
                .get("devices").get(0)
                .get("capabilities").get(0))
                .put("state", "fdf dfsdfs");

        log.info(jsonNode);


        String nnn = JsonUtils.replaceState(json, "sdsdsd");
        log.info(nnn);


    }
}


