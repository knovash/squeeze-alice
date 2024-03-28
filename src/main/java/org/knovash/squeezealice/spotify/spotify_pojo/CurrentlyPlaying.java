package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentlyPlaying {

    public long timestamp;
    public Context context;
    public int progress_ms;
    public Item item;
    public String currently_playing_type;
    public Actions actions;
    public boolean is_playing;

    public static class Actions {

        public Disallows disallows;
    }

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

    public static class Artist {

        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public String type;
        public String uri;
    }

    public static class Context {

        public ExternalUrls external_urls;
        public String href;
        public String type;
        public String uri;
    }

    public static class Disallows {

        public boolean resuming;
        public boolean skipping_prev;
    }

    public static class ExternalIds {

        public String isrc;
    }

    public static class ExternalUrls {

        public String spotify;
    }

    public static class Image {

        public int height;
        public String url;
        public int width;
    }

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
}
