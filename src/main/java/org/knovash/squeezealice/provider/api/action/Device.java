package org.knovash.squeezealice.provider.api.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    public String id = "0";
    public List<Capability> capabilities = new ArrayList<>();
}