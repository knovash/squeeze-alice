package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.pojo.Device;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload {
    public Device[] devices;

    public String user_id;
//    public ArrayList<Object> devices;
}
