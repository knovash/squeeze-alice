package org.knovash.squeezealice;

import java.util.List;

public class Widgets {

    public List<Play> playList;
    public List<Stop> widgetsData;
    public String playlistWidgetText;
    public String playerslistWidgetText;

    public List<Play> getPlayList() {
        return playList;
    }

    public void setPlayList(List<Play> playList) {
        this.playList = playList;
    }

    public List<Stop> getWidgetsData() {
        return widgetsData;
    }

    public void setWidgetsData(List<Stop> widgetsData) {
        this.widgetsData = widgetsData;
    }

    public static class Play {

        String player = null;
        String volume;
        String mode;
        String title;

        public String getPlayer() {
            return player;
        }

        public void setPlayer(String player) {
            this.player = player;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "{" + '\n' +
                    "player='" + player + '\n' +
                    ", volume='" + volume + '\n' +
                    ", mode='" + mode + '\n' +
                    ", title='" + title + '\n' +
                    '}';
        }
    }

    public static class Stop {

        String player;
        String volume;
        String mode;
        String title;

        public String getPlayer() {
            return player;
        }

        public void setPlayer(String player) {
            this.player = player;
        }

        public String getVolume() {
            return volume;
        }

        public void setVolume(String volume) {
            this.volume = volume;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return "{" + '\n' +
                    "player='" + player + '\n' +
                    ", volume='" + volume + '\n' +
                    ", mode='" + mode + '\n' +
                    ", title='" + title + '\n' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Widgets{" +
                "playList=" + playList + '\n' +
                ", stopList=" + widgetsData + '\n' +
                '}';
    }
}


