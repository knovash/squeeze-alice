package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumioQueueResponse {
    private List<QueueItem> queue;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QueueItem {
        private String uri;
        private String title;
        private String artist;
        private String album;
        private String trackType;
        private int duration;
    }
}