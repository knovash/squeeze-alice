package org.knovash.squeezealice.provider;

import org.knovash.squeezealice.spotify.spotify_pojo.PlayerArtist;

import java.util.ArrayList;

public class YandexInfo {

    public String status;
    public String request_id;
    public ArrayList<Room> rooms;
    public ArrayList<Group> groups;
    public ArrayList<Device> devices;
    public ArrayList<Scenario> scenarios;
    public ArrayList<Household> households;


    public static class Capability {

        public boolean retrievable;
        public String type;
        public Parameters parameters;
        public State state;
        public boolean reportable;
        public double last_updated;
    }

    public static class Device {

        public String id;
        public String name;
        public ArrayList<String> aliases;
        public String type;
        public String external_id;
        public String skill_id;
        public String household_id;
        public String room;
        public ArrayList<String> groups;
        public ArrayList<Capability> capabilities;
        public ArrayList<Property> properties;
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