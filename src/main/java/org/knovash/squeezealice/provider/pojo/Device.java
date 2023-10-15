package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.Yandex;

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
        int availableDevices = Yandex.devices.size();
        Device device = new Device();
        device.name = deviceName;
        device.id = String.valueOf(availableDevices);
        Yandex.devices.add(device);
        return Integer.valueOf(device.id);
    }

    public Integer addToYandex() {
        int id = Yandex.devices.size();
        this.id = String.valueOf(id);
        Yandex.devices.add(this);
        return id;
    }
}