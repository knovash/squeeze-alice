package org.knovash.squeezealice.requests.spotify.spotifyplaylist;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Item {
    public boolean collaborative;
    public String description;
    public ExternalUrls external_urls;
    public String href;
    public String id;
    public ArrayList<Image> images;
    public String name;
    public Owner owner;
    public Object primary_color;
    @JsonProperty("public")
    public Object mypublic;
    public String snapshot_id;
    public Tracks tracks;
    public String type;
    public String uri;
}
