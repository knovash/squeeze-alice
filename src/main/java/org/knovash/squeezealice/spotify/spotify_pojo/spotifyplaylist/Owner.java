package org.knovash.squeezealice.spotify.spotify_pojo.spotifyplaylist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Owner {
    public String display_name;
    public ExternalUrls external_urls;
    public String href;
    public String id;
    public String type;
    public String uri;
}
