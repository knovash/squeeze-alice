package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    public String type = null;
    public Boolean retrievable = true;
    public Boolean reportable = false;
    public State state = new State();
    public Parameters parameters = new Parameters();
}
