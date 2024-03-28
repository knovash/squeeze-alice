package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerArtist {

    public ExternalUrls external_urls;
    public Followers followers;
    public ArrayList<String> genres;
    public String href;
    public String id;
    public ArrayList<Image> images;
    public String name;
    public int popularity;
    public String type;
    public String uri;

    public static class ExternalUrls {

        public String spotify;
    }

    public static class Followers {

        public Object href;
        public int total;
    }

    public static class Image {

        public String url;
        public int height;
        public int width;
    }
}
