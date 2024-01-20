package org.knovash.squeezealice;

import com.sun.net.httpserver.Headers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class Context {

    public String json;
    public int code;
    public String path;
    public Headers headers;
    public String body;
    public String xRequestId;
    public String query;
    public HashMap<String,String> queryMap;
}