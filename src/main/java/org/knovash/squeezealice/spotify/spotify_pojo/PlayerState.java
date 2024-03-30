package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerState {
    public Device device;
    public boolean shuffle_state;
    public boolean smart_shuffle;
    public String repeat_state;
    public long timestamp;
    public Context context;
    public int progress_ms;
    public Item item;
    public String currently_playing_type;
    public Actions actions;
    public boolean is_playing;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        public Album album;
        public ArrayList<Artist> artists;
        public ArrayList<String> available_markets;
        public int disc_number;
        public int duration_ms;
        public boolean explicit;
        public ExternalIds external_ids;
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public boolean is_local;
        public String name;
        public int popularity;
        public String preview_url;
        public int track_number;
        public String type;
        public String uri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Actions {
        public Disallows disallows;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Album {
        public String album_type;
        public ArrayList<Artist> artists;
        public ArrayList<String> available_markets;
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public ArrayList<Image> images;
        public String name;
        public String release_date;
        public String release_date_precision;
        public int total_tracks;
        public String type;
        public String uri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public String type;
        public String uri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Context {
        public ExternalUrls external_urls;
        public String href;
        public String type;
        public String uri;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Device {
        public String id;
        public boolean is_active;
        public boolean is_private_session;
        public boolean is_restricted;
        public String name;
        public boolean supports_volume;
        public String type;
        public int volume_percent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Disallows {
        public boolean resuming;
        public boolean skipping_prev;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalIds {
        public String isrc;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalUrls {
        public String spotify;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        public int height;
        public String url;
        public int width;
    }
}
