package org.knovash.squeezealice.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Log4j2
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());

    public static String pojoToJson(Object pojo) {
        try {
//            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo).replace("\\", "");// для State переделанного в String
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonToPojo(String json, Class<T> clazz) {
//        log.info("JSON TO POJO: " + json);
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String listToJson(List<T> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void pojoToJsonFile(Object pojo, String fileName) {
        File file = new File(fileName);
        try {
            objectWriter.writeValue(file, pojo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void listToJsonFile(List<T> list, String fileName) {
        File file = new File(fileName);
        try {
            objectWriter.writeValue(file, list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonFileToPojo(String fileName, Class<T> clazz) {
        File file = new File(fileName);
        if (!file.exists()) return null;
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonFileToPojoTrows(String fileName, Class<T> clazz) throws IOException {
        File file = new File(fileName);
        return objectMapper.readValue(file, clazz);
    }

    public static <T> List<T> jsonFileToList(String fileName, Class<T> clazz) {
        File file = new File(fileName);
        if (!file.exists()) return null;
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        try {
            return objectMapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> Map<K, V> jsonFileToMap(String fileName, Class<K> clazzKey, Class<V> clazzValue) {
        File file = new File(fileName);
        if (!file.exists()) return null;
        JavaType type = objectMapper.getTypeFactory().constructMapType(Map.class, clazzKey, clazzValue);
        try {
            return objectMapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> clazzKey, Class<V> clazzValue) {
//        log.info("JSON TO MAP");
//        JavaType type = objectMapper.getTypeFactory().constructMapType(Map.class, clazzKey, clazzValue);
//        try {
//            return objectMapper.readValue(json, type);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static <K, V> void mapToJsonFile(Map<K, V> map, String fileName) {
        File file = new File(fileName);
        try {
            objectWriter.writeValue(file, map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String jsonGetValue(String json, String valueName) {
//        log.info("JSON: " + json);
//        log.info("VALUE NAME: " + valueName);
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        log.info("NODE: " + jsonNode);
        String result = null;
//        log.info("findValue: " + jsonNode.findValue(valueName));
//        log.info("textValue: " + jsonNode.findValue(valueName).textValue());
//        log.info("asText: " + jsonNode.findValue(valueName).asText());
//        log.info("toString: " + jsonNode.findValue(valueName).toString());
        result = jsonNode.findValue(valueName).asText();
//        log.info("VALUE: " + result);
        return result;
    }

    public static String jsonGetNode(String json) {
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

//        jsonNode.get("payload").get("devices").get(0).get("capabilities").get(0).get("state").

        return "NODE " + jsonNode.get("payload").get("devices").get(0).get("capabilities").get(0).get("state");
    }

    public static void valueToJsonFile(String valueName, String value) {
        ValuePojo valuePojo = new ValuePojo(value);
        JsonUtils.pojoToJsonFile(valuePojo, valueName + ".json");
    }

    public static String valueFromJsonFile(String fileName) {
        ValuePojo valuePojo = new ValuePojo();
        valuePojo = JsonUtils.jsonFileToPojo(fileName, ValuePojo.class);
        if (valuePojo == null) return null;
//        log.info("VALUE POJO: " + valuePojo);
        String json = JsonUtils.pojoToJson(valuePojo);
//        log.info("VALUE JSON: " + json);
        String value = JsonUtils.jsonGetValue(json, "value");
//        log.info("VALUE : " + value);
        return value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static private class ValuePojo {

        public String value;
    }

    public static String replaceState(String json, String newState) {
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ((ObjectNode) jsonNode
                .get("payload")
                .get("devices").get(0)
                .get("capabilities").get(0))
                .put("state", newState);
        log.info(jsonNode);
        return String.valueOf(jsonNode);
    }
}