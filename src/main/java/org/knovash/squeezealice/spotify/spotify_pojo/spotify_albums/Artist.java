package org.knovash.squeezealice.spotify.spotify_pojo.spotify_albums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artist {

    public ExternalUrls external_urls;
    public String href;
    public String id;
    public String name;
    public String type;
    public String uri;
}