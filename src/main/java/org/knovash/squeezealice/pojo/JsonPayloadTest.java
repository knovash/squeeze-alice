package org.knovash.squeezealice.pojo;

import java.util.ArrayList;


//    public class Root{
//        public Payload payload;
//    }

public class JsonPayloadTest {
    public Payload payload;

    public class Capability{
        public String type;
        public State state;
    }

    public class Device{
        public String id;
        public ArrayList<Capability> capabilities;
    }

    public class Payload{
        public ArrayList<Device> devices;
    }

    public class State{
        public String instance;
        public boolean value;
    }

}


