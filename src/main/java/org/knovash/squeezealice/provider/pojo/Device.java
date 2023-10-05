package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    public String id;
    public ArrayList<Object> capabilities;
    public ArrayList<Object> properties;
    public String error_code;
    public String error_message;

    public String name;
    public String description;
    public String room;
    public String type;
    public String custom_data;
    public DeviceInfo device_info;
}