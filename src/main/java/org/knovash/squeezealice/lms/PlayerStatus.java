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

        @Override
        public String toString() {
            return "PlaylistLoop{" +
                    "playlist_index=" + playlist_index +
                    ", id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
    }

    public static class RemoteMeta {

        public String id;
        public String title;

        @Override
        public String toString() {
            return "RemoteMeta{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    '}';
        }
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
        public String sync_slaves;
        public String sync_master;

        @Override
        public String toString() {
            return "Result{" +
                    "mixer_volume=" + mixer_volume +
                    ", playlist_timestamp=" + playlist_timestamp +
                    ", seq_no=" + seq_no +
                    ", player_ip='" + player_ip + '\'' +
                    ", playlist_cur_index='" + playlist_cur_index + '\'' +
                    ", replay_gain='" + replay_gain + '\'' +
                    ", playlist_loop=" + playlist_loop +
                    ", current_title='" + current_title + '\'' +
                    ", rate=" + rate +
                    ", player_connected=" + player_connected +
                    ", playlist_shuffle=" + playlist_shuffle +
                    ", playlist_mode='" + playlist_mode + '\'' +
                    ", mode='" + mode + '\'' +
                    ", power=" + power +
                    ", duration=" + duration +
                    ", playlist_repeat=" + playlist_repeat +
                    ", digital_volume_control=" + digital_volume_control +
                    ", player_name='" + player_name + '\'' +
                    ", remoteMeta=" + remoteMeta +
                    ", signalstrength=" + signalstrength +
                    ", can_seek=" + can_seek +
                    ", playlist_tracks=" + playlist_tracks +
                    ", time=" + time +
                    ", remote=" + remote +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PlayerStatus{" +
                "params=" + params +
                ", method='" + method + '\'' +
                ", id=" + id +
                ", result=" + result +
                '}';
    }
}