package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifySearchAlbum {

    public Albums albums;

    public static class Albums {

        public String href;
        public int limit;
        public String next;
        public int offset;
        public Object previous;
        public int total;
        public ArrayList<Item> items;
    }

    public static class Artist {

        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public String type;
        public String uri;
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
    }
}
