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
        public int firmware;
        public int isplayer;
        public int canpoweroff;

//        @Override
//        public String toString() {
//            return "PlayersLoop{" +
//                    "ip='" + ip + '\'' +
//                    ", uuid=" + uuid +
//                    ", connected=" + connected +
//                    ", modelname=" + modelname + '\'' +
//                    ", name=" + name + '\'' +
//                    ", model=" + model + '\'' +
//                    ", displaytype=" + displaytype + '\'' +
//                    ", power=" + power +
//                    ", playerindex=" + playerindex +
//                    ", seq_no=" + seq_no +
//                    ", isplaying=" + isplaying +
//                    ", mac=" + playerid + '\'' +
//                    ", firmware=" + firmware +
//                    ", isplayer=" + isplayer +
//                    ", canpoweroff=" + canpoweroff +
//                    '}';
//        }
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
        public String  current_title;
        public int info_total_albums;
        public int progresstotal;
        public int info_total_songs;
        public String version;
        public String ip;
        public ArrayList<PlayersLoop> players_loop;
        public String uuid;
        public int info_total_artists;

//        @Override
//        public String toString() {
//            return "Result{" +
//                    "mac='" + mac + '\'' +
//                    ", lastscan='" + lastscan + '\'' +
//                    ", info_total_duration=" + info_total_duration +
//                    ", newplugins='" + newplugins + '\'' +
//                    ", httpport='" + httpport + '\'' +
//                    ", info_total_genres=" + info_total_genres +
//                    ", other_player_count=" + other_player_count +
//                    ", player_count=" + player_count +
//                    ", current_title='" + current_title + '\'' +
//                    ", info_total_albums=" + info_total_albums +
//                    ", progresstotal=" + progresstotal +
//                    ", info_total_songs=" + info_total_songs +
//                    ", version='" + version + '\'' +
//                    ", ip='" + ip + '\'' +
//                    ", players_loop=" + players_loop +
//                    ", uuid='" + uuid + '\'' +
//                    ", info_total_artists=" + info_total_artists +
//                    '}';
//        }
    }
}