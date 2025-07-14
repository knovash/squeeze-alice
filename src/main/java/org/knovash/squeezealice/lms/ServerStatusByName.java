package org.knovash.squeezealice.lms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerStatusByName {

    public Result result;
    public int id;
    public String method;
    public ArrayList<Object> params;

    @Override
    public String toString() {
        return "ServerStatusName{" +
                "result=" + result +
                ", id=" + id +
                ", method='" + method + '\'' +
                ", params=" + params +
                '}';
    }

    public static class PlayersLoop {

        public String ip;
        public Object uuid;
        public int connected;
        public String modelname;
        public String name;
        public String model;
        public String displaytype;
        public int power;
        public Object playerindex;
        public int seq_no;
        public int isplaying;
        public String playerid;
//        public int firmware; // TODO String и на облакке
        public int isplayer;
        public int canpoweroff;
    }

    public static class Result {

        public String mac;
        public String lastscan;
        public double info_total_duration;
        public String newplugins;
        public String httpport;
        public int info_total_genres;
        //        public String newversion;
        public int other_player_count;
        public int player_count;
        public String current_title;
        public int info_total_albums;
        public int progresstotal;
        public int info_total_songs;
        public String version;
        public String ip;
        public ArrayList<PlayersLoop> players_loop;
        public String uuid;
        public int info_total_artists;
    }
}