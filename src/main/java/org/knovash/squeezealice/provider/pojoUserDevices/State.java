package org.knovash.squeezealice.provider.pojoUserDevices;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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
    public boolean relative = false;
    public int value;
    public ActionResult action_result = new ActionResult();

//    private static ObjectMapper objectMapper = new ObjectMapper();
//    private static ObjectWriter objectWriter = objectMapper.writer(new DefaultPrettyPrinter());

}