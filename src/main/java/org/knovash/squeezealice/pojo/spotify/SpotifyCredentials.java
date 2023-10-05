package org.knovash.squeezealice.pojo.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyCredentials {

    public String clientId;
    public String clientSecret;
}