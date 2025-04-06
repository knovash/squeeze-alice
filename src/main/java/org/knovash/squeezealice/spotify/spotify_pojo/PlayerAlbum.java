package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAlbum{
    public String album_type;
    public int total_tracks;
    public ArrayList<String> available_markets;
    public ExternalUrls external_urls;
    public String href;
    public String id;
    public ArrayList<Image> images;
    public String name;
    public String release_date;
    public String release_date_precision;
    public String type;
    public String uri;
    public ArrayList<Artist> artists;
    public Tracks tracks;
    public ArrayList<Copyright> copyrights;
    public ExternalIds external_ids;
    public ArrayList<Object> genres;
    public String label;
    public int popularity;

    public static class Artist{
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public String type;
        public String uri;
    }

    public static class Copyright{
        public String text;
        public String type;
    }

    public static class ExternalIds{
        public String upc;
    }

    public static class ExternalUrls{
        public String spotify;
    }

    public static class Image{
        public String url;
        public int height;
        public int width;
    }

    public static class Item{
        public ArrayList<Artist> artists;
        public ArrayList<String> available_markets;
        public int disc_number;
        public int duration_ms;
        public boolean explicit;
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public String preview_url;
        public int track_number;
        public String type;
        public String uri;
        public boolean is_local;
    }



    public static class Tracks{
        public String href;
        public int limit;
        public Object next;
        public int offset;
        public Object previous;
        public int total;
        public ArrayList<Item> items;
    }
}



