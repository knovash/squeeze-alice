package org.knovash.squeezealice.lms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatus {

    public ArrayList<Object> params;
    public String method;
    public int id;
    public Result result;

    public static class PlaylistLoop {

        public int playlist_index;
        public String id;
        public String title;
    }

    public static class RemoteMeta {

        public String id;
        public String title;
    }

    public static class Result {

        public int mixer_volume;
        public double playlist_timestamp;
        public int seq_no;
        public String player_ip;
        public String playlist_cur_index;
        public String replay_gain;
        public ArrayList<PlaylistLoop> playlist_loop;
        public String current_title;
        public int rate;
        public int player_connected;
        public int playlist_shuffle;
        public String playlist_mode;
        public String mode;
        public int power;
        public double duration;
        public int playlist_repeat;
        public int digital_volume_control;
        public String player_name;
        public RemoteMeta remoteMeta;
        public int signalstrength;
        public int can_seek;
        public int playlist_tracks;
        public double time;
        public int remote;
    }
}