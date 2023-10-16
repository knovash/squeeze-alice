package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateToggle {

    public String instance = null;
    public boolean relative = false;
    public boolean value;
    public ActionResult action_result = new ActionResult();
}