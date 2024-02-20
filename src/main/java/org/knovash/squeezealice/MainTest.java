package org.knovash.squeezealice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class MainTest {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");


        Map<String,String> rooms = new HashMap<>();

        rooms.put("dfds","fsfsd");
        rooms.put("gfd","t4ttee");
        rooms.put("bdsdf","t43t34");

        JsonUtils.mapToJsonFile(rooms,"rrr.json");


        Map<String,String> rooms2 = new HashMap<>();

        rooms2 = JsonUtils.jsonFileToMap("rrr.json", String.class, String.class);

        log.info("RRR " +rooms2);

    }
}