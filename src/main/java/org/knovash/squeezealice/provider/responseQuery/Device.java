package org.knovash.squeezealice.provider.responseQuery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    public String id;
    public List<Capability> capabilities = new ArrayList<>();
}