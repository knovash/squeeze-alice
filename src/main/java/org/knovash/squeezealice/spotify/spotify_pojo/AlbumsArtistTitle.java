package org.knovash.squeezealice.spotify.spotify_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumsArtistTitle {

    public ArrayList<Artist> artists;
    public String name;

    public static class Artist {

        public String name;
    }
}
