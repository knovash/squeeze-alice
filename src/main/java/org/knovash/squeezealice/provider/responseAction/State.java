package org.knovash.squeezealice.provider.responseAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class State {
    public String instance;
    public String value;
    public boolean relative;
    public ActionResult action_result;
}
