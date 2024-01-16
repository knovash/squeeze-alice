package org.knovash.squeezealice.pojo.pojoActions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload {
    public ArrayList<Device> devices;
    public String user_id;
}
