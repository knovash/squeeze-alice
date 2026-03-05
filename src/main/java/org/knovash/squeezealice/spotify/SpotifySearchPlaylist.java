package org.knovash.squeezealice.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifySearchPlaylist {
    public Playlists playlists;
    public static class Playlists {
        public List<Item> items;
    }
    public static class Item {
        public String uri;
        public String name;
        public Owner owner;
    }
    public static class Owner {
        public String display_name;
    }
}
