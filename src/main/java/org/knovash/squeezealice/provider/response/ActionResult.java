package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResult {

    public String status = "DONE";
    public String error_code;
    public String error_message;
}


