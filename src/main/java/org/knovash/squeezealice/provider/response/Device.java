package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    public String id;
    public String name = "музыка";
    public String room;
    public String type;
    public List<Capability> capabilities = new ArrayList<>();
    public List<Property> properties = new ArrayList<>();
    public List<String> aliases = new ArrayList<>();
    public String external_id;
    public String skill_id;
    public String household_id;
    public List<String> groups;
    public ActionResult actionResult;
    public String error_code = null;
    public String error_message = null;

//    public YandexInfo.QuasarInfo quasar_info;

}