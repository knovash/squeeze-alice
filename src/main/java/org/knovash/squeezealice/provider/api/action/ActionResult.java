package org.knovash.squeezealice.provider.api.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResult {

    public String status = "DONE";
    public String error_code = "ERR CODE";
    public String error_message = "ERR MSG";
}


