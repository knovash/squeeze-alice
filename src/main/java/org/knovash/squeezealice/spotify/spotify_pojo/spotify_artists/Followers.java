package org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Followers {
    public Object href;
    public int total;
}
