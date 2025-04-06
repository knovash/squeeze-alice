package org.knovash.squeezealice.provider;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

@Log4j2
public class ProviderCheck {

    public static Context providerCheckRun(Context context) {
        log.info("200 OK");
        context.bodyResponse = "OK";
        context.code = 200;
        return context;
    }
}