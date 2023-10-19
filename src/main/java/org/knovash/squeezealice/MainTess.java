package org.knovash.squeezealice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MainTess {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());


    public static void main(String[] args) throws JsonProcessingException {
        log.info("  ---+++===[ START ]===+++---");
        log.info("READ CONFIG FROM PROPERTIES");

//        NewDevice.create("колонка", "HomePod", "Гостиная");
//        NewDevice.create("колонка", "JBL black", "Спальня");
//        NewDevice.create("колонка", "JBL wt", "Спальня");
//
//
//        log.info(Home.devices.stream().map(d -> d.customData.lmsName+ " --- " + d.id).collect(Collectors.toList()));
//
//        Device dd = Home.devices.getLast();
//
//        log.info(Home.devices.indexOf(dd));



    }
}


