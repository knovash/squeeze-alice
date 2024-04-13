package org.knovash.squeezealice.provider.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CapabilitiesSubType.CapabilitiesString.class, name = "devices.capabilities.range"),
        @JsonSubTypes.Type(value = CapabilitiesSubType.CapabilitiesBoolean.class, name = "devices.capabilities.on_off")
})


public class CapabilitiesSubType {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface CapabilitiesZSubType {
        String value();
    }

    public String type;
    public Boolean retrievable = true;
    public Boolean reportable = false;
    public Parameters parameters = new Parameters();
    // getters & setters


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRetrievable() {
        return retrievable;
    }

    public void setRetrievable(Boolean retrievable) {
        this.retrievable = retrievable;
    }

    public Boolean getReportable() {
        return reportable;
    }

    public void setReportable(Boolean reportable) {
        this.reportable = reportable;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @CapabilitiesZSubType("devices.capabilities.range")
    public static class CapabilitiesString extends CapabilitiesSubType {

        StateString state = new StateString();
        String val = "vvvv";
        // getters & setters

        public StateString getState() {
            return state;
        }

        public void setState(StateString state) {
            this.state = state;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }
    }

    @CapabilitiesZSubType("devices.capabilities.on_off")
    public static class CapabilitiesBoolean extends CapabilitiesSubType {

        StateBoolean state = new StateBoolean();
        Boolean val = true;
        // getters & setters

        public StateBoolean getState() {
            return state;
        }

        public void setState(StateBoolean state) {
            this.state = state;
        }

        public Boolean getVal() {
            return val;
        }

        public void setVal(Boolean val) {
            this.val = val;
        }
    }


    public static class State {

        public String instance = "INSTANCE";
        public boolean relative = true;
        public ActionResult action_result = new ActionResult();
        // getters & setters

        public String getInstance() {
            return instance;
        }

        public void setInstance(String instance) {
            this.instance = instance;
        }

        public boolean isRelative() {
            return relative;
        }

        public void setRelative(boolean relative) {
            this.relative = relative;
        }

        public ActionResult getAction_result() {
            return action_result;
        }

        public void setAction_result(ActionResult action_result) {
            this.action_result = action_result;
        }
    }

    public static class StateString extends State {

        public String value = "VALUE";
        // getters & setters

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class StateBoolean extends State {

        public Boolean value = true;

        // getters & setters
        public Boolean getValue() {
            return value;
        }

        public void setValue(Boolean value) {
            this.value = value;
        }
    }
}
