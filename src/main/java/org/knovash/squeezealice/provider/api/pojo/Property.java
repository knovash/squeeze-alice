package org.knovash.squeezealice.provider.pojo.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.pojo.device.Parameters;
import org.knovash.squeezealice.provider.pojo.device.State;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

//    public String id = null;
    public String type = null;
    public Boolean retrievable = true;
    public Boolean reportable = false;
    public State state = new State();
    public Parameters parameters = new Parameters();
}
