package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    public String request_id;
    public Payload payload = new Payload();
}