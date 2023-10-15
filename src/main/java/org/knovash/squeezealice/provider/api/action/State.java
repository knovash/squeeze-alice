package org.knovash.squeezealice.provider.api.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class State {

    public String instance = null;
    public int value;
}