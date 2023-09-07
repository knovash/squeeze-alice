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
public class RequestToLms {

    public String id;
    public String method;
    public List<Object> params;

    public static RequestToLms create(String player, String[] params) {
        /** request sample {"id": 1, "method": "slim.request", "params":["HomePod", ["mixer", "volume", "?"]]} */
        RequestToLms requestToLms = new RequestToLms();
        requestToLms.setId("1");
        requestToLms.setMethod("slim.request");
        requestToLms.setParams(Arrays.asList(player, Arrays.asList(params)));
        return requestToLms;
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
