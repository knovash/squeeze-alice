package org.knovash.squeezealice.provider.responseAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Range {

    public int max = 100;
    public int min = 1;
    public int precision = 1;
}