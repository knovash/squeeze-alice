package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.SmartHome;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
public class Device {

    public String id;
    public String name;
    public List<String> aliases = new ArrayList<>();
    public String type;
    public String description;
    public String room;
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
    public CustomData customData = new CustomData();

    public Device(String deviceName) {
        this.name = deviceName;
    }

    public Integer addToHome(Device device) {
        int id = 0;
        if (SmartHome.devices.size() != 0) id = Integer.parseInt(SmartHome.devices.getLast().id) + 1;
        log.info(SmartHome.devices.stream().map(d -> d.room).collect(Collectors.toList()));
        device.id = String.valueOf(id);
        SmartHome.devices.add(device);
        return id;
    }
}