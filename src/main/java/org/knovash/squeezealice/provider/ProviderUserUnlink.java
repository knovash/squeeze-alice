package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class ProviderUserUnlink {

    public static Context providerUserUnlinkRun(Context context) {
        log.info("");

        String xRequestId = context.headers.getFirst("X-request-id");
        log.info("XREQUESTID: " + xRequestId);

//        String xRequestId = context.xRequestId;
        String json = "{\"request_id\":\"" + xRequestId + "\"}";
        log.info("RESPONSE: " + json);
        context.bodyResponse = json;
        context.code = 200;
        return context;
    }
}