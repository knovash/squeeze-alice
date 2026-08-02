package org.knovash.squeezealice.lms;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistTracksLoop {
    public Result result;
    public String id;
    public String method;

    @Data
    public static class Result {
        @JsonProperty("playlisttracks_loop")
        public List<Track> playlisttracksLoop;
        public int count;
        @JsonProperty("__playlistTitle")
        public String playlistTitle;
    }

    @Data
    public static class Track {
        @JsonProperty("playlist index")
        public String playlistIndex;
        public String id;
        public String title;
        public double duration;
    }
}