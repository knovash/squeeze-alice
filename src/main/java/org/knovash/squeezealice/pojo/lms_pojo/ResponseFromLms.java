package org.knovash.squeezealice.pojo.lms_pojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseFromLms {
    public List<Object> params;
    public Result result;
    public String id;
    public String method;
}
