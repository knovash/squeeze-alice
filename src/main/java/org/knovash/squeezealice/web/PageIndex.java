package org.knovash.squeezealice.web;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Context;

import java.io.IOException;

import static org.knovash.squeezealice.utils.Utils.readFile;

@Log4j2
public class PageIndex {

    public static Context action(Context context) {
        String json = Html.index();
        context.json = json;
        context.code = 200;
        return context;

//        String json = null;
//        try {
//            json = readFile("src/main/resources/index.html");
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        context.json = json;
//        context.code = 200;
//        return context;
    }
}