package org.knovash.squeezealice.provider.responseQuery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class State {

    public String instance;
    public String value;
    public boolean relative;
    public ActionResult action_result = new ActionResult();
}