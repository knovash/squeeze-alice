package org.knovash.squeezealice.provider.responseQuery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResult {
    public String status;
    public String error_code;
    public String error_message;
}

