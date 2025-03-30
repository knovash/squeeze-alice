package org.knovash.squeezealice.web;

import org.knovash.squeezealice.Context;

import java.io.InputStream;

public class Icon {

    public static Context getIcon(Context context){

//        InputStream iconStream = Icon.class.getResourceAsStream("/public/favicon.ico");
//        context.getResponse().setContentType("image/x-icon");
//        iconStream.transferTo(context.getResponse().getOutputStream());
        return context;
    }

}
