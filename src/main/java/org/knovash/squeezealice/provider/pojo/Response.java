package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    public String request_id = null;
    public Payload payload = new Payload();
}