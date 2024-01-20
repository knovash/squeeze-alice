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

    public String value;
    public String instance;
//    public boolean relative = false;
//    public ActionResult action_result = new ActionResult();
//    private static ObjectMapper objectMapper = new ObjectMapper();
//    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());
}