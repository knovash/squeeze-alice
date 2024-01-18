package org.knovash.squeezealice.provider.pojoQueryResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class Device {

    public String id;
    public List<Capability> capabilities = new ArrayList<>();
}