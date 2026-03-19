package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumioStateResponse {
    public String status;      // "play", "pause", "stop"
    public String title;
    public String artist;
    public String album;
    public String trackType;
    public int volume;
    public int seek;
    public int duration;
    public int position;
    public String service;
    public String uri;
}