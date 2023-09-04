package org.knovash.squeezealice.requests;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    public List<Object> params;
    public Result result;
    public String id;
    public String method;
}
