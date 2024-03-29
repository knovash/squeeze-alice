package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload {

    public List<Device> devices = new ArrayList<>();
    public String user_id;
}
