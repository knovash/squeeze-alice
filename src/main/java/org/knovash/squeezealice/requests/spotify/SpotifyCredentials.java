package org.knovash.squeezealice.requests.spotify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyCredentials {

    String clientId;
    String clientSecret;

}
