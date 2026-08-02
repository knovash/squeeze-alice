package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {

    public String instance;
    public Range range = null;
    public List<Mode> modes = new ArrayList<>();
    public boolean random_access;
}


