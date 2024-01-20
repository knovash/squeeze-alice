package org.knovash.squeezealice.voice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliceVoiceResponsePojo {
    public ResponseAlice response;
    public String version;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseAlice {
        public String text;
        public boolean end_session;
    }
}