package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.pojo.Payload;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseQuery {
    public String request_id;
    public Payload payload;
}