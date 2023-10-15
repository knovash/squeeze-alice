package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.HttpUtils;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class MainTess {


    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");
        log.info("READ CONFIG FROM PROPERTIES");


        Map<String, String> q = new HashMap<>();
        q.put("secret", "767");
        q.put("name", "user");
        q.put("fff", "ggg");

        log.info("MMM " + q);
        String qqq = HttpUtils.createQuery(q);
        log.info(qqq);


    }
}



//{"request_id":"ff36a3cc-ec34-11e6-b1a0-64510650abcf","payload":{"devices":[{
//"id":"abc-123",
//"capabilities":[{
//"type":"devices.capabilities.color_setting",
//"state":{"instance":"volume","value":3}}]}]}}