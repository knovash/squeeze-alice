package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;
import org.knovash.squeezealice.SmartHome;
import org.knovash.squeezealice.provider.response.Payload;
import org.knovash.squeezealice.provider.response.ResponseYandex;
import org.knovash.squeezealice.utils.JsonUtils;

@Log4j2
public class ProviderUserDevices {

    public static Context action(Context context) {
        log.info("");
        String xRequestId = context.xRequestId;
        log.info("XREQUESTID: " + xRequestId);
        ResponseYandex responseYandex = new ResponseYandex();
        responseYandex.request_id = xRequestId;
        responseYandex.payload = new Payload();
        responseYandex.payload.user_id = SmartHome.user_id;
        responseYandex.payload.devices = SmartHome.devices;
        String json = JsonUtils.pojoToJson(responseYandex);

        json = "{\n" +
                "  \"request_id\" : \"cdde2055-6607-4d28-b2ea-ae2fa17b1e40\",\n" +
                "  \"payload\" : {\n" +
                "    \"devices\" : [ {\n" +
                "      \"id\" : \"1\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Веранда\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"Radiotechnika\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"value\" : \"10\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"value\" : \"2\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"value\" : \"false\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"id\" : \"2\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Спальня\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"JBL black\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"value\" : \"16\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"value\" : \"2\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"value\" : \"false\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"id\" : \"3\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Душ\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"HomePod2\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"value\" : \"5\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"value\" : \"2\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"value\" : \"false\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"id\" : \"4\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Гостиная\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"HomePod\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"id\" : \"6\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Улица\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"ggmm\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"id\" : \"7\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Гараж\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"JBL white\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"value\" : \"22\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"value\" : \"2\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"state\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"value\" : \"true\",\n" +
                "          \"relative\" : false,\n" +
                "          \"action_result\" : {\n" +
                "            \"status\" : \"DONE\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    }, {\n" +
                "      \"id\" : \"8\",\n" +
                "      \"name\" : \"музыка\",\n" +
                "      \"room\" : \"Комната\",\n" +
                "      \"type\" : \"devices.types.media_device.receiver\",\n" +
                "      \"customData\" : {\n" +
                "        \"lmsName\" : \"Mi Box\"\n" +
                "      },\n" +
                "      \"capabilities\" : [ {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"volume\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 100,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.range\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"channel\",\n" +
                "          \"range\" : {\n" +
                "            \"max\" : 9,\n" +
                "            \"min\" : 1,\n" +
                "            \"precision\" : 1\n" +
                "          },\n" +
                "          \"random_access\" : true\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"type\" : \"devices.capabilities.on_off\",\n" +
                "        \"retrievable\" : true,\n" +
                "        \"reportable\" : true,\n" +
                "        \"parameters\" : {\n" +
                "          \"instance\" : \"on\",\n" +
                "          \"random_access\" : false\n" +
                "        }\n" +
                "      } ]\n" +
                "    } ],\n" +
                "    \"user_id\" : \"konstantin\"\n" +
                "  }\n" +
                "}";

        json = JsonUtils.pojoToJson(responseYandex);

        context.json = json;
        context.code = 200;
        return context;
    }
}


