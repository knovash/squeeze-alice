package org.knovash.squeezealice.provider.pojoUserDevices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResult {

    public String status = "DONE";
    public String error_code = null;
    public String error_message = null;
}


