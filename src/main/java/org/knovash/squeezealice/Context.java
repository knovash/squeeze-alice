package org.knovash.squeezealice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.HandlerUtils;

import java.util.HashMap;
@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Context {

    public String json;
    public int code;
    public String path;
    public Headers headers;
    public String body;
    public String xRequestId;
    public String query;
    public HashMap<String, String> queryMap;



    @Override
    public String toString() {
        return "Context{" +
                "json='" + json + '\'' +
                ", code=" + code +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                ", xRequestId='" + xRequestId + '\'' +
                ", query='" + query + '\'' +
                ", queryMap=" + queryMap +
                '}';
    }
}