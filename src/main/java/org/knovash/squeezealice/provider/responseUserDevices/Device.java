package org.knovash.squeezealice.provider.responseUserDevices;

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
    public String name;
    public String description;
    public String room;
    public String type;
    public CustomData customData = new CustomData();
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
    public List<String> aliases = new ArrayList<>();

    public Device(String deviceName) {
        this.name = deviceName;
    }
}