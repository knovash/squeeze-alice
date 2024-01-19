package org.knovash.squeezealice.handler;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Switch;
import org.knovash.squeezealice.provider.Context;

@Log4j2
public class ActionKuzja {

    public static Context action(Context context) {
        log.info("START -------------------------------");
        String query = context.query;
        query = query.replaceAll("\\+", " ");
        log.info("QUERY: " + query);
        String response = Switch.action(query);
        context.json = response;
        context.code = 200;
        return context;
    }
}