package org.knovash.squeezealice.provider.pojoQueryResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    public String request_id;
    public Payload payload;
}