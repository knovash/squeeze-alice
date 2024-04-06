package org.knovash.squeezealice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.response.CapabilitiesZ;

@Log4j2
public class MainTest {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        try {
            whenDeserializingPolymorphic_thenCorrect();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void whenDeserializingPolymorphic_thenCorrect() throws JsonProcessingException {

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


        String jsonF = "{\n" +
                "  \"type\" : \"devices.capabilities.on_off\",\n" +
                "  \"retrievable\" : true," +
                "  \"stateX\" :" +
                "{" +
                "  \"instance\" : \"AAA\"," +
                "  \"relative\" : true" +

                "}"+

                "}";
        String jsonE = "{\n" +
                "  \"type\" : \"devices.capabilities.range\",\n" +
                "  \"retrievable\" : true" +

                "}";

//        JSON to POJO

        CapabilitiesZ capabilitiesE = new ObjectMapper().readerFor(CapabilitiesZ.class).readValue(jsonE);
        log.info("\n\n" + capabilitiesE);
        log.info(CapabilitiesZ.CapabilitiesString.class);
        log.info(capabilitiesE.getClass());

        CapabilitiesZ capabilitiesF = new ObjectMapper().readerFor(CapabilitiesZ.class).readValue(jsonF);
        log.info("\n\n" + capabilitiesF);
        log.info(CapabilitiesZ.CapabilitiesBoolean.class);
        log.info(capabilitiesF.getClass());

//        POJO to JSON

        String jsonNewF;
        try {
            jsonNewF = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilitiesF);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("\n\n" + jsonNewF);

        String jsonNewE;
        try {
            jsonNewE = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilitiesE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.info("\n\n" + jsonNewE);


    }
}