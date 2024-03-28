package org.knovash.squeezealice.spotify.spotify_pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerPlaylist {
    public boolean collaborative;
    public String description;
    public ExternalUrls external_urls;
    public Followers followers;
    public String href;
    public String id;
    public ArrayList<Image> images;
    public String name;
    public Owner owner;
    public String primary_color;
    @JsonProperty("public")
    public boolean mypublic;
    public String snapshot_id;
    public Tracks tracks;
    public String type;
    public String uri;


    public static class AddedBy{
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String type;
        public String uri;
    }

    public static class Album{
        public ArrayList<String> available_markets;
        public String type;
        public String album_type;
        public String href;
        public String id;
        public ArrayList<Image> images;
        public String name;
        public String release_date;
        public String release_date_precision;
        public String uri;
        public ArrayList<Artist> artists;
        public ExternalUrls external_urls;
        public int total_tracks;
    }

    public static class Artist{
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public String type;
        public String uri;
    }

    public static class ExternalIds{
        public String isrc;
    }

    public static class ExternalUrls{
        public String spotify;
    }

    public static class Followers{
        public Object href;
        public int total;
    }

    public static class Image{
        public Object height;
        public String url;
        public Object width;
    }

    public static class Item{
        public Date added_at;
        public AddedBy added_by;
        public boolean is_local;
        public Object primary_color;
        public Track track;
        public VideoThumbnail video_thumbnail;
    }

    public static class Owner{
        public String display_name;
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String type;
        public String uri;
    }

    public static class Track{
        public String preview_url;
        public ArrayList<String> available_markets;
        public boolean explicit;
        public String type;
        public boolean episode;
        public boolean track;
        public Album album;
        public ArrayList<Artist> artists;
        public int disc_number;
        public int track_number;
        public int duration_ms;
        public ExternalIds external_ids;
        public ExternalUrls external_urls;
        public String href;
        public String id;
        public String name;
        public int popularity;
        public String uri;
        public boolean is_local;
    }

    public static class Tracks{
        public String href;
        public ArrayList<Item> items;
        public int limit;
        public Object next;
        public int offset;
        public Object previous;
        public int total;
    }

    public static class VideoThumbnail{
        public Object url;
    }


}
