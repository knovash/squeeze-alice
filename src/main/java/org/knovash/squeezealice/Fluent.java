package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.knovash.squeezealice.requests.ResponseFromLms;

import java.io.IOException;

import static org.knovash.squeezealice.Main.lmsServer;

@Log4j2
public class Fluent {

    public static String postGetStatus(String json) {
        log.info("REQUEST TO LMS: " + json);
        String status = null;
        try {
            status = Request.Post(lmsServer).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        return status;
    }

    public static ResponseFromLms postGetContent(String json) {
        log.info("REQUEST TO LMS: " + json);
        Content content = null;
        ResponseFromLms responseFromLms = null;
        try {
            content = Request.Post(lmsServer).bodyString(json, ContentType.APPLICATION_JSON)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        if (content != null) {
            responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        } else {
            log.info("ERROR");
        }
        return responseFromLms;
    }

    public static String postQueryGetStatus(String uri) {
        log.info("REQUEST TO LMS: " + uri);
        String status = null;
        try {
            status = Request.Get(uri)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnResponse()
                    .getStatusLine()
                    .toString();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        return status;
    }

    public static ResponseFromLms postSpotify(String json) {
        log.info("REQUEST TO LMS: " + json);
        String token = "0BQC4hI5jIakBtcWcIUrXSuIkE57vM73BsRer90_zLmhB1bQd6wZp3Scdt2g9y6ErtJf-U6XKn5K05ixZRFa4Xwg6UJViqji-ygRqvPD_S8X-bM9mlyikxurhZ7VT17w6XWWo_ADFl2qCWukfKHee6uDIaR0q1WoLB87bV4KoVlMDInAlOgMJG_ptT050HqdhtSvkpGfVOGpPfNsskgEhRekxF9qZ91jADC1wz6mpreKjVfeODfWLNHPc2dZxGnJN4pH0tQ4OpfBouyFGYgxSZAKj";
        String url = "https://api.spotify.com/v1/me/top/tracks?";
        String body = "time_range=short_term&limit=5";
        Content content = null;
        ResponseFromLms responseFromLms = null;

//        Request.Post()

        try {
            content = Request.Get("https://api.spotify.com/v1/me/top/tracks?time_range=short_term&limit=5")
//                    .bodyString(body,ContentType.DEFAULT_TEXT)
                    .setHeader(HttpHeaders.AUTHORIZATION, token)
                    .connectTimeout(1000)
                    .socketTimeout(1000)
                    .execute()
                    .returnContent();
        } catch (IOException e) {
            log.info("ERROR: " + e);
        }
        if (content != null) {
            responseFromLms = JsonUtils.jsonToPojo(content.asString(), ResponseFromLms.class);
        } else {
            log.info("ERROR");
        }
        return responseFromLms;
    }

//    async function getTopTracks(){
//        // Endpoint reference : https://developer.spotify.com/documentation/web-api/reference/get-users-top-artists-and-tracks
//        return (await fetchWebApi(
//                'v1/me/top/tracks?time_range=short_term&limit=5', 'GET'
//        )).items;
//    }


}