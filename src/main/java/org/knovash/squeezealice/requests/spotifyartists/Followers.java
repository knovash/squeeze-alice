package org.knovash.squeezealice.requests.spotifyartists;

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
