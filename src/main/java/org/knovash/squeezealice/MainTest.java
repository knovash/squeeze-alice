package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.requests.Type;
import org.knovash.squeezealice.requests.spotifyalbums.SpotifyResponseAlbums;
import org.knovash.squeezealice.requests.spotifyartists.SpotifyResponseArtists;
import org.knovash.squeezealice.requests.spotifyplaylist.SpotifyResponsePlaylists;
import org.knovash.squeezealice.requests.spotifytracks.SpotifyResponseTracks;

@Log4j2
public class MainTest {

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        log.info("  ---+++===[ START ]===+++---");
        String clientId = "f45a18e2bcfe456dbd9e7b73e74514af";
        String clientSecret = "5c3321b4ae7e43ab93a2ce4ec1b4cf48";
        Spotify.getBearerToken(clientId, clientSecret);
        log.info("  ---+++===[ ALBUM ]===+++---");

//        https://api.spotify.com/v1/search?q=depeche mode&type=album&limit=5

//"https://api.spotify.com/v1/search?q=$1+$2                &type=album     &limit=5"
//"https://api.spotify.com/v1/search?q=track%3A$1+$2+$3     &type=track     &limit=5"
//"https://api.spotify.com/v1/search?q=%22$1%22             &type=artist    &limit=5"
//"https://api.spotify.com/v1/search?q=%22$1%22             &type=playlist  &limit=$range"

        String link;

        link = Spotify.search("kraftwerk", Type.album);
        log.info("LINK " + link);

        link = Spotify.search("kraftwerk", Type.playlist);
        log.info("LINK " + link);

        link = Spotify.search("kraftwerk", Type.track);
        log.info("LINK " + link);

        link = Spotify.search("kraftwerk", Type.album);
        log.info("LINK " + link);

    }
}