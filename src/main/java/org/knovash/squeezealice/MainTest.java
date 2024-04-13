package org.knovash.squeezealice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.ServerStatusName;
import org.knovash.squeezealice.utils.JsonUtils;

import java.sql.SQLOutput;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class MainTest {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());

    public static void main(String[] args) {
        System.out.println("  ---+++===[ START ]===+++---");
        try {
            deserializingPolymorphic();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deserializingPolymorphic() throws JsonProcessingException {

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


//        String jsonF = "{\n" +
//                "  \"type\" : \"devices.capabilities.on_off\",\n" +
//                "  \"retrievable\" : true,\n" +
//                "  \"reportable\" : false,\n" +
//                "  \"parameters\" : {\n" +
//                "    \"random_access\" : false\n" +
//                "  },\n" +
//                "  \"state\" : {\n" +
//                "    \"instance\" : \"AAA\",\n" +
//                "    \"relative\" : true,\n" +
//                "    \"action_result\" : {\n" +
//                "      \"status\" : \"DONE\"\n" +
//                "    },\n" +
//                "    \"value\" : true\n" +
//                "  },\n" +
//                "  \"val\" : true\n" +
//                "}";


        String json = "{\n" +
                "    \"method\": \"slim.request\",\n" +
                "    \"result\": {\n" +
                "        \"mac\": \"02:42:47:5d:84:a6\",\n" +
                "        \"info total artists\": 99,\n" +
                "        \"lastscan\": \"1712888734\",\n" +
                "        \"info total songs\": 420,\n" +
                "        \"info total genres\": 1,\n" +
                "        \"ip\": \"192.168.1.110\",\n" +
                "        \"player count\": 3,\n" +
                "        \"info total duration\": 146427.394,\n" +
                "        \"uuid\": \"6fa7f2b9-b9ff-4bb8-8ec2-9f4bcc22a496\",\n" +
                "        \"newplugins\": \"Plugin updates are available (UPnP/DLNA bridge)\",\n" +
                "        \"version\": \"9.0.0\",\n" +
                "        \"other player count\": 0,\n" +
                "        \"players_loop\": [\n" +
                "            {\n" +
                "                \"playerid\": \"bb:bb:6c:66:a3:fe\",\n" +
                "                \"seq_no\": 0,\n" +
                "                \"isplayer\": 1,\n" +
                "                \"isplaying\": 1,\n" +
                "                \"playerindex\": \"name\",\n" +
                "                \"model\": \"squeezelite\",\n" +
                "                \"name\": \"Radiotechnika\",\n" +
                "                \"connected\": 1,\n" +
                "                \"firmware\": 0,\n" +
                "                \"ip\": \"192.168.1.110:45886\",\n" +
                "                \"displaytype\": \"none\",\n" +
                "                \"power\": 1,\n" +
                "                \"canpoweroff\": 1,\n" +
                "                \"uuid\": null,\n" +
                "                \"modelname\": \"UPnPBridge\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"connected\": 1,\n" +
                "                \"name\": \"JBL black\",\n" +
                "                \"firmware\": 0,\n" +
                "                \"isplaying\": 0,\n" +
                "                \"playerindex\": 1,\n" +
                "                \"model\": \"squeezelite\",\n" +
                "                \"playerid\": \"cc:cc:90:1a:7e:f7\",\n" +
                "                \"isplayer\": 1,\n" +
                "                \"seq_no\": 0,\n" +
                "                \"power\": 1,\n" +
                "                \"canpoweroff\": 1,\n" +
                "                \"modelname\": \"CastBridge\",\n" +
                "                \"uuid\": null,\n" +
                "                \"ip\": \"192.168.1.110:43868\",\n" +
                "                \"displaytype\": \"none\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"power\": 1,\n" +
                "                \"canpoweroff\": 1,\n" +
                "                \"uuid\": null,\n" +
                "                \"modelname\": \"CastBridge\",\n" +
                "                \"ip\": \"192.168.1.110:43904\",\n" +
                "                \"displaytype\": \"none\",\n" +
                "                \"isplaying\": 0,\n" +
                "                \"playerindex\": 2,\n" +
                "                \"model\": \"squeezelite\",\n" +
                "                \"name\": \"Mi Box\",\n" +
                "                \"connected\": 1,\n" +
                "                \"firmware\": 0,\n" +
                "                \"seq_no\": 0,\n" +
                "                \"isplayer\": 1,\n" +
                "                \"playerid\": \"cc:cc:c6:86:44:c5\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"newversion\": \"A new version of Lyrion Music Server is available (9.0.0 - 1712895891). <a href=\\\"updateinfo.html?installerFile=/var/lib/squeezeboxserver/cache/updates/lyrionmusicserver_9.0.0~1712895891_arm.deb\\\" target=\\\"update\\\">Click here for further information</a>.\",\n" +
                "        \"info total albums\": 37,\n" +
                "        \"httpport\": \"9000\"\n" +
                "    },\n" +
                "    \"params\": [\n" +
                "        \"\",\n" +
                "        [\n" +
                "            \"serverstatus\",\n" +
                "            \"name\"\n" +
                "        ]\n" +
                "    ],\n" +
                "    \"id\": 1\n" +
                "}";

       json = JsonUtils.replaceSpace(json);
       json = json.replaceAll("\"newversion\": \".*\",","");

        log.info("JSON REPLACED " + json);
        ServerStatusName serverStatusName = JsonUtils.jsonToPojo(json, ServerStatusName.class);
        System.out.println(serverStatusName.toString());


//        pattern.matcher(json)
//                .results()                       // Stream<MatchResult>
//                .map(mr -> mr.group(1))          // Stream<String> - the 1st group of each result
//                .forEach(s -> System.out.println("S " + s));   // print them out (or process in other way...)

//        String jsonE = "{\n" +
//                "  \"type\" : \"devices.capabilities.range\",\n" +
//                "  \"retrievable\" : true,\n" +
//                "  \"reportable\" : false,\n" +
//                "  \"parameters\" : {\n" +
//                "    \"random_access\" : false\n" +
//                "  },\n" +
//                "  \"state\" : {\n" +
//                "    \"instance\" : \"INSTANCE\",\n" +
//                "    \"relative\" : true,\n" +
//                "    \"action_result\" : {\n" +
//                "      \"status\" : \"DONE\"\n" +
//                "    },\n" +
//                "    \"value\" : \"VALUE\"\n" +
//                "  },\n" +
//                "  \"val\" : \"vvvv\"\n" +
//                "}";
//
////        JSON to POJO
//       System.out.println("\n\n  ---+++===[ JSON to POJO ]===+++---\n\n");
//
//        CapabilitiesSubType capabilitiesE = new ObjectMapper().readerFor(CapabilitiesSubType.class).readValue(jsonE);
//        System.out.println("\n\nJSON to OBJECT E\n\n" + capabilitiesE);
//        System.out.println(capabilitiesE.getClass());
//
//        CapabilitiesSubType capabilitiesF = new ObjectMapper().readerFor(CapabilitiesSubType.class).readValue(jsonF);
//        System.out.println("\n\nJSON to OBJECT F\n\n" + capabilitiesF);
//        System.out.println(capabilitiesF.getClass());
//
////        POJO to JSON
//       System.out.println("\n\n  ---+++===[  POJO to JSON ]===+++---\n\n");
//
//        String jsonNewF;
//        try {
//            jsonNewF = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilitiesF);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//       System.out.println("\nJSON NEW F\n" + jsonNewF);
//
//        String jsonNewE;
//        try {
//            jsonNewE = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilitiesE);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//       System.out.println("\n JSON NEW E\n" + jsonNewE);
//
//        //        JSON to POJO
//       System.out.println("\n\n  ---+++===[  JSON to POJO ]===+++---\n\n");
//
//        CapabilitiesSubType capabilitiesEnew = new ObjectMapper().readerFor(CapabilitiesSubType.class).readValue(jsonNewE);
//       System.out.println("\nOBJECT NEW E\n\n" + capabilitiesEnew);
//       System.out.println(capabilitiesEnew.getClass());
//
//        CapabilitiesSubType capabilitiesFnew = new ObjectMapper().readerFor(CapabilitiesSubType.class).readValue(jsonNewF);
//       System.out.println("\nOBJECT NEW F\n\n" + capabilitiesFnew);
//       System.out.println(capabilitiesFnew.getClass());
//
//       System.out.println("-----------------------------------------");
//
//       System.out.println("\nOBJECT\n" + capabilitiesE);
//       System.out.println("\nTYPE\n" + capabilitiesE.type);
//        capabilitiesE.type = "ff";
//       System.out.println("\nTYPE\n" + capabilitiesE.type);
//
//
//       System.out.println("\nOBJECT\n" + capabilitiesF);
//       System.out.println("\nTYPE\n" + capabilitiesF.type);
//        capabilitiesF.type = "ff";
//       System.out.println("\nTYPE\n" + capabilitiesF.type);


    }

    public static String replaceAllSpace(String text) {
        log.info("TEXT " + text);

        return text.replaceAll(" ", "+++");
    }
}