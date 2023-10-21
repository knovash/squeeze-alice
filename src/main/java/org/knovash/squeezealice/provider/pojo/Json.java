package org.knovash.squeezealice.provider.pojo;

public class Json {


    public static String update(String request_id) {

        String json = "{\n" +
                "  \"request_id\" : \""+request_id+"\",\n" +
                "  \"payload\" : {\n" +
                "    \"devices\" : [ {\n" +
                "      \"id\" : \"0\",\n" +
                "      \"name\" : \"колонка\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"description\" : \"JBL black\",\n" +
                "      \"room\" : \"Спальня\",\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : false,\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"relative\" : false,\n" +
                "          \"value\" : 0,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\",\n" +
                "            \"error_code\" : null,\n" +
                "            \"error_message\" : null\n" +
                "          }\n" +
                "        },\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"modes\" : [ ],\n" +
                "          \"random_access\" : true,\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"split\" : false\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : false,\n" +
                "        \"reportable\" : false,\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"relative\" : false,\n" +
                "          \"value\" : 0,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\",\n" +
                "            \"error_code\" : null,\n" +
                "            \"error_message\" : null\n" +
                "          }\n" +
                "        },\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"modes\" : [ ],\n" +
                "          \"random_access\" : true,\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"split\" : false\n" +
                "        }\n" +
                "      } " +

                ///
                "," +
                " {\n" +
                "                        \"type\": \"devices.capabilities.toggle\",\n" +
                "                        \"state\": {\n" +
                "                            \"instance\": \"pause\",\n" +
                "                            \"value\": true\n" +
                "                        }\n" +
                "                    }" +

                /////

                "],\n" +
                "      \"properties\" : [ ],\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"JBL black\"\n" +
                "      }\n" +
                "    } ],\n" +
                "    \"user_id\" : \"konstantin\"\n" +
                "  }\n" +
                "}";
        return json;
    }


    public static String updateToggle = "{\n" +
            "  \"request_id\" : \"e6c8e6b4-0313-4ddc-8148-122bfeda3bae\",\n" +
            "  \"payload\" : {\n" +
            "    \"devices\" : [ {\n" +
            "      \"id\" : \"0\",\n" +
            "      \"name\" : \"колонка\",\n" +
            "      \"type\" : \"devices.types.media_device.receiver\",\n" +
            "      \"description\" : \"JBL black\",\n" +
            "      \"room\" : \"Спальня\",\n" +
            "      \"capabilities\" : [ {\n" +
            "        \"type\" : \"devices.capabilities.range\",\n" +
            "        \"retrievable\" : true,\n" +
            "        \"reportable\" : false,\n" +
            "        \"state\" : {\n" +
            "          \"instance\" : \"volume\",\n" +
            "          \"relative\" : false,\n" +
            "          \"value\" : 0,\n" +
            "          \"action_result\" : {\n" +
            "            \"status\" : \"DONE\",\n" +
            "            \"error_code\" : null,\n" +
            "            \"error_message\" : null\n" +
            "          }\n" +
            "        },\n" +
            "        \"parameters\" : {\n" +
            "          \"instance\" : \"volume\",\n" +
            "          \"modes\" : [ ],\n" +
            "          \"random_access\" : true,\n" +
            "          \"range\" : {\n" +
            "            \"max\" : 100,\n" +
            "            \"min\" : 1,\n" +
            "            \"precision\" : 1\n" +
            "          },\n" +
            "          \"split\" : false\n" +
            "        }\n" +
            "      }, {\n" +
            "        \"type\" : \"devices.capabilities.range\",\n" +
            "        \"retrievable\" : false,\n" +
            "        \"reportable\" : false,\n" +
            "        \"state\" : {\n" +
            "          \"instance\" : \"channel\",\n" +
            "          \"relative\" : false,\n" +
            "          \"value\" : 0,\n" +
            "          \"action_result\" : {\n" +
            "            \"status\" : \"DONE\",\n" +
            "            \"error_code\" : null,\n" +
            "            \"error_message\" : null\n" +
            "          }\n" +
            "        },\n" +
            "        \"parameters\" : {\n" +
            "          \"instance\" : \"channel\",\n" +
            "          \"modes\" : [ ],\n" +
            "          \"random_access\" : true,\n" +
            "          \"range\" : {\n" +
            "            \"max\" : 9,\n" +
            "            \"min\" : 1,\n" +
            "            \"precision\" : 1\n" +
            "          },\n" +
            "          \"split\" : false\n" +
            "        }\n" +
            "      } ],\n" +
            "      \"properties\" : [ ],\n" +
            "      \"customData\" : {\n" +
            "        \"lmsName\" : \"JBL black\"\n" +
            "      }\n" +
            "    } ],\n" +
            "    \"user_id\" : \"konstantin\"\n" +
            "  }\n" +
            "}";




}
