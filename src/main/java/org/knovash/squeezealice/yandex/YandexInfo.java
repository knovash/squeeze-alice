package org.knovash.squeezealice.yandex;

import java.util.ArrayList;
import java.util.List;

public class YandexInfo {

    public String status;
    public String request_id;
    public List<Room> rooms;
    public List<Group> groups;
    public List<Device> devices;
    public List<Scenario> scenarios;
    public List<Household> households;


    public static class Capability {

        public String type;
        public State state;
        public boolean retrievable;
        public boolean reportable;
        public Parameters parameters;
        public double last_updated;
    }

    public static class Device {

        public String id;
        public String name;
        public String room;
        public String type;
        public List<Capability> capabilities;
        public List<Property> properties;
        public List<String> aliases;
        public String external_id;
        public String skill_id;
        public String household_id;
        public List<String> groups;
        public QuasarInfo quasar_info;
    }

    public static class Event {

        public String value;
        public String name;
    }

    public static class Group {

        public String id;
        public String name;
        public ArrayList<Object> aliases;
        public String household_id;
        public String type;
        public ArrayList<String> devices;
        public ArrayList<Capability> capabilities;
    }

    public static class Household {

        public String id;
        public String name;
        public String type;
    }

    public static class Parameters {

        public String instance;
        public String unit;
        public boolean random_access;
        public boolean looped;
        public Range range;
        public TemperatureK temperature_k;
        public boolean split;
        public String color_model;
        public ArrayList<Event> events;
    }

    public static class Property {

        public String type;
        public boolean reportable;
        public boolean retrievable;
        public Parameters parameters;
        public State state;
        public double state_changed_at;
        public double last_updated;
    }

    public static class QuasarInfo {

        public String device_id;
        public String platform;
    }

    public static class Range {

        public int min;
        public int max;
        public int precision;
    }

    public static class Room {

        public String id;
        public String name;
        public String household_id;
        public ArrayList<String> devices;
    }

    public static class Scenario {

        public String id;
        public String name;
        public boolean is_active;
    }

    public static class State {

        public String instance;
        public Object value;
    }

    public static class TemperatureK {

        public int min;
        public int max;
    }
}