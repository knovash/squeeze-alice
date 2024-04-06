package org.knovash.squeezealice.provider.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CapabilitiesZ.CapabilitiesString.class, name = "devices.capabilities.range"),
        @JsonSubTypes.Type(value = CapabilitiesZ.CapabilitiesBoolean.class, name = "devices.capabilities.on_off")
})
public class CapabilitiesZ {

    public String type;
    public Boolean retrievable = true;
    public Boolean reportable = false;
    public Parameters parameters = new Parameters();

    @Data
    public static class CapabilitiesString extends CapabilitiesZ {

        StateXString stateX = new StateXString();
    }

    @Data
    public static class CapabilitiesBoolean extends CapabilitiesZ {

        StateXBoolean stateX = new StateXBoolean();
    }

    public static class StateX {

        public String instance = "INSTANCE";
        public boolean relative = true;
        public ActionResult action_result = new ActionResult();
    }

    public static class StateXString extends StateX {

        public String value = "VALUE";
    }

    public static class StateXBoolean extends StateX {

        public Boolean value = true;
    }
}
