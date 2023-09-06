package org.knovash.squeezealice.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {

    public String id;
    public String method;
    public List<Object> params;

    public static Request create(String player, String[] params) {
        /** request sample {"id": 1, "method": "slim.request", "params":["HomePod", ["mixer", "volume", "?"]]} */
        Request request = new Request();
        request.setId("1");
        request.setMethod("slim.request");
        request.setParams(Arrays.asList(player, Arrays.asList(params)));
        return request;
    }

    /** не удалять. переводит json в строку */
    @Override
    public String toString() {
        String json;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}
