package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.Home;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    public String id;
    public String name;
    public String type;
    public String description;
    public String room;
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
    public CustomData customData = new CustomData();

    public Device(String deviceName) {
        this.name = deviceName;
    }

    public static Integer create(String deviceName) {
        int availableDevices = Home.devices.size();
        Device device = new Device();
        device.name = deviceName;
        device.id = String.valueOf(availableDevices);
        Home.devices.add(device);
        return Integer.valueOf(device.id);
    }

    public Integer addToYandex() {
        int id = Home.devices.size();
        this.id = String.valueOf(id);
        Home.devices.add(this);
        return id;
    }
}