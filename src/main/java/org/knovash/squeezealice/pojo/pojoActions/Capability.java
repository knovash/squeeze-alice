package org.knovash.squeezealice.pojo.pojoActions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capability {
    public String type;
    public State state;
}
