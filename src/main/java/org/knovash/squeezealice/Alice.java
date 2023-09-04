package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alice {

    List<AliceSpeaker> aliceSpeakers = new ArrayList<>();


    public static void create(){
        AliceSpeaker aliceSpeaker1 = new AliceSpeaker("1", "livingroom", 5);
        AliceSpeaker aliceSpeaker2 = new AliceSpeaker("2", "bedroom", 5);

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AliceSpeaker {

        String id;
        String room;
        Integer volume;

    }
}
