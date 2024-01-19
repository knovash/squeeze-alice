package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.Context;

@Log4j2
public class ActionYandex {

    public static Context action(Context context) {
        String json = Html.formYandexLogin();
        context.json = json;
        context.code = 200;
        return context;
    }
}

