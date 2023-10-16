package org.knovash.squeezealice.provider.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class State {


    public String instance = null;
    public boolean relative = false;
    public int value;
    public ActionResult action_result = new ActionResult();

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());

    public static String setState(String instance, String value, String relative, ActionResult actionResult) {

        return "{\"instance\" :\"" + instance +
                "\", \"relative\" :" + relative + ", \"value\" :" + value +
                ", \"action_result\" :" + JsonUtils.pojoToJson(actionResult) +
                "}";
    }

    public static String setState(String instance, String value, String relative) {

        return "{\"instance\" :\"" + instance +
                "\", \"relative\" :" + relative + ", \"value\" :" + value +
                "}";
    }

    public static String getValue(String text, String value){

        Pattern pattern = Pattern.compile(value+"\\s*\"+\\s*:\\s*\\d*\\w*\\s*,");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            text = text.substring(matcher.start(), matcher.end());
        }
        Pattern pattern2 = Pattern.compile(":\\s*\\d*\\w*");
        Matcher matcher2 = pattern2.matcher(text);
        while (matcher2.find()) {
            text = text.substring(matcher2.start(), matcher2.end());
        }
        text = text.replaceAll(":\\s*","");

        return text;
    }

    public static String jsonGetState(String json) {

        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String state = String.valueOf(jsonNode.get("payload").get("devices").get(0).get("capabilities").get(0).get("state"));
        return state;
    }

//    public static String bodyStateToString(String text){
//
//        Pattern pattern = Pattern.compile(+"\\s*\"+\\s*:\\s*\\d*\\w*\\s*,");
//        Matcher matcher = pattern.matcher(text);
//        while (matcher.find()) {
//            text = text.substring(matcher.start(), matcher.end());
//        }
//
//        return "";
//    }



//    {"instance" :"channel", "relative" :false, "value" :0, "action_result" :{
//        "status" :"DONE", "error_code" :null, "error_message" :null}


}