package org.knovash.squeezealice.provider.api.pojo;

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

    public String id = "0";
    public String name = "название";
    public String type = "тип";
    public String description = "описание";
    public String room = "комната";
    public List<String> aliases = new ArrayList<>();
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
//    public CustomData custom_data = new CustomData();
//    public DeviceInfo device_info = new DeviceInfo();
    //    public String external_id = null;
//    public String skill_id = null;
//    public String household_id = null;
//    public List<String> groups = new ArrayList<>();


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