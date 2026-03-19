package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumioListResponse {
    private List<Navigation> navigation;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Navigation {
        private List<Item> lists;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String service;
        private String type;
        private String title;
        private String artist;
        private String album;
        private String uri;
        private String icon;
    }
}