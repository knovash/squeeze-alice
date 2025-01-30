package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifySearchTrack {

    public Tracks tracks;

    public static class Tracks {

        public String href;
        public int limit;
        public String next;
        public int offset;
        public Object previous;
        public int total;
        public ArrayList<Item> items;
    }

    public static class Album {

        public String album_type;
        public ArrayList<Artist> artists;
        public ArrayList<String> available_markets;
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public ArrayList<Image> images;
        public boolean is_playable;
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

    public static class ExternalIds {

        public String isrc;
    }

    public static class ExternalUrls {

        public String spotify;
    }

    public static class Image {

        public int height;
        public int width;
        public String url;
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
        public boolean is_playable;
        public String name;
        public int popularity;
        public Object preview_url;
        public int track_number;
        public String type;
        public String uri;
    }
}
