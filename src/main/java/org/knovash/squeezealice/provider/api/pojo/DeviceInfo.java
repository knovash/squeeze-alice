package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    public String manufacturer = null;
    public String model = null;
    public String hw_version = null;
    public String sw_version = null;
}
