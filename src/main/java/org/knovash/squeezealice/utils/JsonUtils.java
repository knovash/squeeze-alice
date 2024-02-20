package org.knovash.squeezealice.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import org.knovash.squeezealice.LmsPlayers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InaccessibleObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class JsonUtils {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());

    public static String pojoToJson(Object pojo) {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        try {
//            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo).replace("\\", "");// для State переделанного в String
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T jsonToPojo(String json, Class<T> clazz) {
        json = json.replace("\\", "");
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {

            log.info("ERROR " + e);
        }
        return null;
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
        if (!file.exists()) {
            log.info("FILE NOT FOUND " + fileName);
            return null;
        }
        try {
            return objectMapper.readValue(file, clazz);
        } catch (IOException | InaccessibleObjectException e) {
            log.info("ERROR READ lms_players.json" + e);
//            throw new RuntimeException(e);
        }
        return null;
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
            return null;
//            throw new RuntimeException(e);
        }
    }

    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> clazzKey, Class<V> clazzValue) {
        log.info("JSON TO MAP");
        JavaType type = objectMapper.getTypeFactory().constructMapType(Map.class, clazzKey, clazzValue);
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V> void mapToJsonFile(Map<K, V> map, String fileName) {
//  только для сохранения кредов спотифай в файл
        File file = new File(fileName);
        try {
            objectWriter.writeValue(file, map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String jsonGetValue(String json, String valueName) {
//  в голосовый коммандах и споти аус
        if (!json.contains(valueName)) return null;
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String result = null;
        result = jsonNode.findValue(valueName).asText();
        return result;
    }

    public static void valueToJsonFile(String valueName, String value) {
//  только для записи ip в файл lms_ip.json
        ValuePojo valuePojo = new ValuePojo(value);
        JsonUtils.pojoToJsonFile(valuePojo, valueName + ".json");
    }

    public static String valueFromJsonFile(String fileName) {
//  только для получения ip из файла lms_ip.json
        ValuePojo valuePojo = new ValuePojo();
        valuePojo = JsonUtils.jsonFileToPojo(fileName, ValuePojo.class);
        if (valuePojo == null) return null;
        String json = JsonUtils.pojoToJson(valuePojo);
        String value = JsonUtils.jsonGetValue(json, "value");
        return value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static private class ValuePojo {

        public String value;
    }
}