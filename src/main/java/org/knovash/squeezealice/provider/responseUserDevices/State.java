package org.knovash.squeezealice.provider.responseUserDevices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class State {

    public String instance = null;
    public int value;
    public boolean relative = false;
    public ActionResult action_result = new ActionResult();

}