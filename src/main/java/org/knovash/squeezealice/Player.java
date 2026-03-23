package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.PlayerStatus;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.lms.ServerStatus;
import org.knovash.squeezealice.utils.JsonUtils;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    //  постоянные плеера
    public String mac;
    public String name;
    public String room;
    //  переменные настройки плеера
    public Integer volume_step;
    public Integer volume_low;
    public Integer volume_high;
    public Integer delay;
    public Map<Integer, Integer> schedule;
    //  длительные свойства
    public String lastPathPlayer;
    public String lastPlayTimePlayer;
    public int lastChannelPlayer = 0;
    public static int lastChannelCommon;
    public static String lastPathCommon;
    //  свойства плеера в моменте
    public PlayerStatus playerStatus; // результат запроса статуса плеера
    public boolean playing;
    public String mode; // play stop offline
    public boolean separate;
    public boolean connected;
    public String title;
    public String volume;
    public boolean sync;
    public String capVolume;
    public String capChannel;
    public String capOn;
//    public boolean result;

    public Player(String name) {
        this.name = name;
        this.room = null;
        this.volume_step = 5;
        this.volume_low = 1;
        this.volume_high = 50;
        this.delay = 10;
        this.schedule = new HashMap<>(Map.of(
                0, 10,
                7, 15,
                9, 20,
                20, 15,
                22, 5));
    }

    public void resetPlayerStatus() {
        log.info("RESET STATUS " + name);
        playerStatus = null;
        connected = false;
        mode = "offline";
        playing = false;
        volume = "0";
        sync = false;
        title = "unknown";
        separate = false;
        lastPathPlayer = null;
        lastPlayTimePlayer = null;
    }


//    public static void updatePlayer(ServerStatus.PlayersLoop p) {
//        Player player = lmsPlayers.playerByName(p.name);
//        if (player == null) {
//            player = new Player(p.name);
//            log.info("ADD NEW PLAYER: " + player.name + " ROOM: " + player.room + " " + player.getClass().getName());
//            lmsPlayers.players.add(player);
//        }
//        player.connected = false;
//        player.mode = "stop";
//        player.playing = false;
//        if (p.isplaying == 1) {
//            player.mode = "play";
//            player.playing = true;
//        }
//        if (p.connected == 1) player.connected = true;
//    }

    public void update(ServerStatus.PlayersLoop p) {
        this.connected = false;
        this.mode = "stop";
        this.playing = false;
        if (p.isplaying == 1) {
            this.mode = "play";
            this.playing = true;
            this.saveLastTime();

        }
        if (p.connected == 1) this.connected = true;
    }

    public void cleanPlayer() {
        this.connected = false;
        this.mode = "stop";
        this.playing = false;
        this.playerStatus = null;
        this.volume = null;
        this.title = null;
    }


    public void status() { // неиспользуется
        log.debug("REQUEST STATUS " + name);
        resetPlayerStatus();
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(name, 50).toString());
        if (json == null) return;
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3"); // TODO использовать @SerializedName("mixer volume") для замены полей типа "mixer volume" на "mixer_volume"
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) return;
        PlayerStatus.Result result = playerStatus.result;
        connected = (result.player_connected == 1);
        volume = String.valueOf(result.mixer_volume);
        playing = "play".equals(result.mode);
        mode = playing ? "play" : "stop";
        sync = (result.sync_slaves != null || result.sync_master != null);
        if (sync) separate = false;
        log.info(this);
        return;
    }

    public String title() {
//        String requestPlaylistName1 = this.requestPlaylistName();
//        String artistName = this.artistName();
//        String albumName = this.albumName();
//        String trackName = this.trackName();
//        log.info("requestPlaylistName: " + requestPlaylistName1);
//        log.info("artistName: " + artistName);
//        log.info("albumName: " + albumName);
//        log.info("trackname: " + trackName);


        String title = null;

        String requestPlaylistName = requestPlaylistName();
        if (requestPlaylistName != null) {
            title = titleCrop(requestPlaylistName);
            if (title != null && title.contains("://")) title = null;
        }

        if (title == null) {
            String artist = null;
            String album = null;
            String track = null;

            artist = artistName();
            log.info("artistName: " + artist);
            if (artist == null) {
                album = albumName();
                log.info("albumName: " + album);
                if (album == null) {
                    track = trackName();
                    log.info("trackname: " + track);
                }
            }

            if (artist != null && track != null) {
                title = titleCrop(artist) + ", " + titleCrop(track);
            } else if (artist != null) {
                title = titleCrop(artist);
            } else if (track != null) {
                title = track; // без обрезки, как в исходном коде
            }
        }

        if (title == null) {
            title = "unknown";
        }

        log.info("TITLE: {}", title);
        this.title = title;
        return title;
    }

    public String titleCrop(String title) {
        if (title == null) return null;
        title = title
                .replaceAll(":.*", "")    // удалить всё после двоеточия
                .replaceAll(" - .*", "")  // удалить всё после " - "
                .replaceAll("\\(.*", "")  // удалить всё после (
                .replaceAll("\\[.*", "")  // удалить всё после [
                .replaceAll(",.*", "")  // удалить всё после [
                .replaceAll(",", " ")     // заменить запятые пробелами
                .replaceAll("\\s+", " ")  // схлопнуть любые пробелы в один
                .trim();                                   // убрать пробелы в начале и конце
        if (title.length() > 30) title = title.substring(0, 30);
        return title;
    }


    // ------------- получение информации о том что играет

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) return "";
        return response.result._path;
    }

    public String requestPlaylistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) return null;
        return response.result._name;
    }

    public String albumName() {
        String jsonResponse = Requests.postToLmsForJsonBody(RequestParameters.albumname(this.name).toString());
        if (jsonResponse == null) return null;
        jsonResponse = JsonUtils.fixQuote(jsonResponse); // исправление " в названии
        Response response = JsonUtils.jsonToPojo(jsonResponse, Response.class);
//        Response response = Requests.postToLmsForResponse(RequestParameters.albumname(this.name).toString());
        if (response == null) return null;
        return response.result._album;
    }

    public String trackName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.trackname(this.name).toString());
        if (response == null) return null;
        return response.result._title;
    }

    public String artistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) return null;
        return response.result._artist;

    }


// LMS получение данных ------------------------------------------------------------------------------------------------

    public String mode() {
        this.playing = false;
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "stop";
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public Boolean playing() {
        this.playing = false;
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return this.playing;
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode + " PLAYING: " + this.playing); // playing
        return this.playing; // playing()
    }

    public String volumeGet() {
        Response response = Requests.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response != null && response.result != null) {
            log.info("PLAYER: " + this.name + " VOLUME: " + response.result._volume);
            this.volume = response.result._volume;
            return response.result._volume;
        }
        return "0";
    }

    public List<String> favorites() {
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites("", "0").toString());
        log.debug("FAV  " + response);
        if (response != null && response.result != null)
            return response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());

        return new ArrayList<>();
    }

    public void favoritesAdd(String url, String title) {
        Requests.postToLmsForResponse(RequestParameters.favoritesAdd(this.name, url, title).toString());
    }


// LMS короткие действия --------------------------------------------------------------------------------------------


    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        Requests.postToLmsForStatus(RequestParameters.play(this.name).toString());
        return this;
    }

    public Player pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        Requests.postToLmsForStatus(RequestParameters.pause(this.name).toString());
        return this;
    }

    public Player togglePlayPause() {
        log.info("PLAYER: " + this.name + " PLAY/PAUSE");
        Requests.postToLmsForStatus(RequestParameters.togglePlayPause(this.name).toString());
        return this;
    }

    public Player playPath(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        Requests.postToLmsForStatus(RequestParameters.play(this.name, path).toString());
        this.lastPathPlayer = path;
        Player.lastPathCommon = path;
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        Requests.postToLmsForStatus(RequestParameters.play(this.name, config.silence).toString());
        return this;
    }

    public Player ctrlPrevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        this.repeatOff();
        Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public Player ctrlNextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        this.repeatOff();
        Requests.postToLmsForStatus(RequestParameters.nexttrack(this.name).toString());
        return this;
    }

    public Player playTrackNumber(String track) {
        log.info("PLAYER: " + this.name + " TRACK NUMBER " + track);
        Requests.postToLmsForStatus(RequestParameters.track(this.name, track).toString());
        return this;
    }

    public Player shuffleOn() {
        log.info("PLAYER: " + this.name + " SHUFFLE ON");
        Requests.postToLmsForStatus(RequestParameters.shuffleOn(this.name).toString());
        return this;
    }

    public Player shuffleOff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        Requests.postToLmsForStatus(RequestParameters.shuffleOff(this.name).toString());
        return this;
    }

    public Player repeatOn() {
        log.info("PLAYER: " + this.name + " REPEAT ON");
        Requests.postToLmsForStatus(RequestParameters.repeatOn(this.name).toString());
        return this;
    }

    public Player repeatOff() {
        log.info("PLAYER: " + this.name + " REPEAT OFF");
        Requests.postToLmsForStatus(RequestParameters.repeatOff(this.name).toString());
        return this;
    }

    public Player syncTo(String toPlayerName) {
        if (this.name.equals(toPlayerName)) {
            log.info("SKIP SYNC source and target player are the same " + this.name);
            return this;
        }
        log.info("SYNC " + this.name + " TO " + toPlayerName + " SYNC=true");
        Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        this.sync = true;
        return this;
    }

    public Player syncToPlayingOrPlayLast() {
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, true);
        if (playingPlayer != null && !playingPlayer.separate && !this.separate) {
            this.syncTo(playingPlayer.name);
            this.sync = true;
        } else {
            this.playLast();
        }
        return this;
    }

    public Player syncToPlayingAndStopPlayingPlayer() {
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, false);
        if (playingPlayer != null) {
            this.syncTo(playingPlayer.name);
            this.sync = true;
            playingPlayer.turnOffMusic();
        }
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name + " SYNC=false");
        Requests.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        this.sync = false;
        return this;
    }


// LMS сценарии --------------------------------------------------------------------------------------------------------


    // включить музыку
    public Player turnOnMusic(String volume) {
        log.info("PLAYER: " + this.name + " TURN ON MUSIC");
        this.ifExpiredAndNotPlayingUnsyncWakeSetVolume(volume); // если не играет разбудить и установить громкость
        if (!this.playing) this.unsync(); // если не играет отключить от группы если вдруг был подключен
        this.syncToPlayingOrPlayLast(); // попытка подключиться к уже играющему. неподключать если он отделен и неподключать к отделенным если игращего нет включить последнее игравшее
        return this;
    }

    public Player turnOffMusic() {
        log.info("PLAYER: " + this.name + " TURN OFF MUSIC");
        this.unsync().pause();
        return this;
    }

    public Player toggleMusic() {
        log.info("PLAYER: " + this.name + " TOGGLE MUSIC");
        if (this.playing) this.turnOffMusic();
        else this.turnOnMusic(null);
        return this;
    }

    public Player volume(String value) {
        log.info("PLAYER: " + this.name + " VOLUME: " + value);
        Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        return this;
    }

    public Player volumeNoLog(String value) {
        Requests.postToLmsForStatusNoLog(RequestParameters.volume(this.name, value).toString());
        return this;
    }

    public Player volumeSetLimited(String value) {
        Integer currentVolume = Integer.valueOf(this.volumeGet()); // получить громкость сейчас запросом в lms
//        Integer currentVolume = Integer.valueOf(this.volume); // взять громкость из данных после обновления
        Integer newVolume = 0;
        if (value.contains("-")) newVolume = currentVolume - Integer.parseInt(value.replace("-", ""));
        if (value.contains("+")) newVolume = currentVolume + Integer.parseInt(value.replace("+", ""));
        if (!value.contains("+") && !value.contains("-")) newVolume = Integer.valueOf(value);
        if (newVolume > this.volume_high) newVolume = this.volume_high; // ограничение максимальной громкости
        if (newVolume < 1) newVolume = 1; // не понижать громкость меньше 1
        if (currentVolume.equals(newVolume)) return this; // если громкость не меняется не делать запрос
        value = String.valueOf(newVolume);
        String status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (!(status == null || !status.contains("200"))) this.volume = value;
        log.info("VOLUME CURRENT: " + currentVolume + " NEW VALUE: " + value + " LIMIT: " + this.volume_high + " SET VOLUME: " + this.volume + " STATUS: " + status);
//        Yandex.sendDeviceState(this.deviceId, "range", "volume", String.valueOf(this.volume), null); // обновить громкость музыки в УДЯ
        return this;
    }

    public Player playChannel(String channel) {
        int id = 1;
        if (channel == null || channel.equals(0)) return this;

        log.info("CHANNEL: " + channel);
        if (channel.contains("-") || channel.contains("+")) {
            id = channelGetRelative(channel);
        } else {
            id = Integer.parseInt(channel) - 1; // id начинается с 0
        }
        log.info("CHANNEL PLAY: " + id);
        this.lastChannelPlayer = id; // сохранить последний канал этого плеера
        Player.lastChannelCommon = id; // сохранить последний канал для всех плееров
        Requests.postToLmsForStatus(RequestParameters.playFavoritesId(this.name, id).toString());
        return this;
    }

    public Integer channelGetRelative(String channel) {
        Integer delta = 0;
        if (channel.contains("-")) delta = Integer.valueOf(channel) * (-1);
        if (channel.contains("+")) delta = Integer.valueOf(channel);
        log.info("DELTA: " + delta);
        Integer currentChannel = this.currentChannelIndexInFavorites(); // попробовать найти индекс по названию в избранном
        log.info("CURRENT CHANNEL " + currentChannel);
        if (currentChannel == null || currentChannel == 0) {
            currentChannel = this.lastChannelPlayer;
            log.info("GET CURRENT FROM PLAYER LAST CHANNEL: " + currentChannel);
        }
        if (currentChannel == null || currentChannel == 0) {
            currentChannel = Player.lastChannelCommon;
            log.info("GET CURRENT FROM COMMON LAST CHANNEL: " + currentChannel);
        }
        if (currentChannel == null || currentChannel == 0) {
            log.info("RETURN DEFAULT CHANNEL: 0");
            return 0;
        }
        Integer channelNew = currentChannel + delta;
        log.info("CHANNEL NEW: " + currentChannel + " + " + delta + " = " + channelNew);
        return channelNew;
    }

    public Integer currentChannelIndexInFavorites() {
        String currentTitle = this.requestPlaylistName();
        log.info("CURRENT TITLE: " + currentTitle);
        Integer currentChannelIndex = 0;
        List<String> favList = this.favorites();
        if (favList.contains(currentTitle)) currentChannelIndex = favList.indexOf(currentTitle);
        return currentChannelIndex;
    }

    public Player ctrlPrevChannel() {
        log.info("PLAYER: " + this.name + " PREV CHANNEL");
        ifExpiredAndNotPlayingUnsyncWakeSetVolume(null); // ctrlPrevChannel
        playChannel("-1");
        return this;
    }

    public Player ctrlNextChannel() {
        log.info("PLAYER: " + this.name + " NEXT CHANNEL");
        ifExpiredAndNotPlayingUnsyncWakeSetVolume(null); // ctrlPrevChannel
        playChannel("+1");
        return this;
    }

    public Player ctrlNextChannelOrTrack() {
        log.info("PLAYLIST TRACKS: " + this.playerStatus.result.playlist_tracks);
        if (this.requestPlaylistTracks() < 2) this.ctrlNextChannel();
        else this.ctrlNextTrack();
        return this;
    }

    public Player ctrlPrevChannelOrTrack() {
        log.info("PLAYLIST TRACKS: " + this.playerStatus.result.playlist_tracks);
        if (this.requestPlaylistTracks() < 2) this.ctrlPrevChannel();
        else this.ctrlPrevTrack();
        return this;
    }

    public int requestPlaylistTracks() {
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(name, 50).toString());
        if (json == null) return 0;
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3"); // TODO использовать @SerializedName("mixer volume") для замены полей типа "mixer volume" на "mixer_volume"
        this.playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) return 0;
        log.info("PLAYLIST TRACKS: " + this.playerStatus.result.playlist_tracks);
        return this.playerStatus.result.playlist_tracks;
    }


    public String favoritesAdd() {
        log.info("START ADD ON " + this.name);
//        this.status(); // favoritesAdd
        String playerName = this.name;
        String url = this.path();
        String title = "this.title"; // favoritesAdd
        log.info("NOW PATH: " + url);
        this.title();
        title = this.title;
        int size = this.favorites().size() + 1;
        log.info("FAVORITES BEFORE SIZE: " + size);
        title = size + " " + title;
        Requests.postToLmsForResponse(RequestParameters.favoritesAdd(this.name, url, title).toString());
        log.info("FAVORITES ADD");
        return title;
    }

    public Player syncToAlt(String toPlayerName) {
// альтернативная синхронизация если глючит синхронизация di.fm
        String path = lmsPlayers.playerByName(toPlayerName).path();
        if (path.contains("di.fm") || path.contains("audioaddict")) {
            log.info("SYNC DI.FM " + this.name + " PLAY PATH FROM " + toPlayerName);
            this.playPath(path);
            return this;
        } else {
            log.info("SYNC " + this.name + " TO " + toPlayerName);
            Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        }
        return this;
    }

    public Player ifExpiredAndNotPlayingUnsyncWakeSetVolume(String volume) {
// если не играет и время просрочено - unsync, wake, set volume
        if (!this.playing && this.checkLastPlayTimeExpired()
        ) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET VOLUME " + volume);
            this
                    .unsync()
                    .wakeAndSetVolume(volume);
            return this;
        } else
// если играет и время не просрочено - set volume если пришла громкость
        {
            log.info("PLAYER " + this.name + " PLAYING - SKIP WAKE, SET VOLUME " + volume);
            if (volume != null) {
                this.volumeSetLimited(volume);
            }
        }
        return this;
    }

    private Player wakeAndSetVolume(String volume) {
        log.info("WAKE START PLAYER: " + this.name + " WAIT: " + this.delay + " VOLUME: " + volume);
//        this.saveLastPathThis();
        if (volume == null) volume = String.valueOf(this.valueVolumeByTime());
        if (delay == null) delay = 10;
        this.playSilence();
        for (int i = 0; i < delay; i++) {
            this.volumeNoLog("+1");
            this.volumeNoLog("-1");
            this.volumeNoLog(volume);
            log.info("WAIT " + i);
            this.waitSeconds(1);
        }
        this.volumeNoLog("+1");
        this.volumeNoLog("-1");
        this.volume(volume);
        this.pause();
        this.saveLastTime();
        log.info("WAKE FINISH");
        return this;
    }

    public Player onlyHere() {
        log.info("ONLY HERE. PLAYER: " + this.name);
        this.turnOnMusic(null).stopOther();
        return this;
    }

    public Player stopOther() {
        log.info("STOP ALL except " + this.name);
        lmsPlayers.checkUpdated(); // TODO DEBUG
        lmsPlayers.players.parallelStream()
                .filter(p -> !p.name.equals(this.name))
                .filter(p -> p.connected)
                .forEach(player -> player.turnOffMusic());
        return this;
    }

    public Player playLast() {
        log.info("PLAY LAST");
        String thisPath = this.path();
        String thisLastPath = this.lastPathPlayer;
        String commonLastPath = Player.lastPathCommon;
        log.info("THIS PATH: " + thisPath);
        log.info("THIS LAST PATH: " + thisLastPath);
        log.info("COMMON LAST PATH: " + commonLastPath);
        log.info("LAST THIS PRIORITY: " + lmsPlayers.lastThis);

// если у этого плеера есть путь и он не тишина - играть
        if (thisPath != null && !thisPath.equals(config.silence) && !thisPath.equals("")) {
            log.info("PUSH PLAY BUTTON: " + thisPath);
            this.play();
            return this;
        }
// если у этого плеера нет пути играть последний сохраненый этого
        if (thisLastPath != null && !thisLastPath.equals(config.silence) && !thisLastPath.equals("") && lmsPlayers.lastThis) {
            log.info("PLAY THIS LAST PATH: " + thisLastPath);
            this.playPath(thisLastPath);
            return this;
        }
// если у этого плеера нет пути и нет последнего этого то играть последний сохраненный общий
        if (commonLastPath != null && !commonLastPath.equals(config.silence) && !commonLastPath.equals("")) {
            log.info("PLAY COMMON LAST PATH: " + commonLastPath);
            this.playPath(commonLastPath);
            return this;
        }
// если ничего не подошло - играть избранное 1
        log.info("PLAY CHANNEL 1");
        this.playChannel("1"); // playLast
        return this;
    }

    public Player setVolumeByTime() {
        LocalTime timeNow = LocalTime.now(zoneId);
// log.info("VOLUME BY TIME: " + timeNow + " OF: " + this.schedule);
        Map.Entry<Integer, Integer> e =
                schedule.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("TIME: " + timeNow.truncatedTo(MINUTES) + " VOLUME: " + e.getValue() + " BY TIME: " + e.getKey() + " PRESETS: " + this.schedule);
        this.volume(String.valueOf(e.getValue()));
        return this;
    }

    public Integer valueVolumeByTime() {
        LocalTime timeNow = LocalTime.now(zoneId);
// log.info("VOLUME BY TIME: " + timeNow + " OF: " + this.schedule);
        Map.Entry<Integer, Integer> e =
                schedule.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("TIME: " + timeNow.truncatedTo(MINUTES) + " VOLUME: " + e.getValue() + " BY TIME: " + e.getKey() + " PRESETS: " + this.schedule);
        return e.getValue();
    }

    public Player waitSeconds(Integer delay) {
//        log.info("WAIT " + delay + " . . . . .");
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Player waitFor(int msec) {
        log.info("WAIT " + msec + " START");
        try {
            Thread.sleep(msec);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("WAIT " + msec + " FINISH");
        return this;
    }

    public Player saveLastTime() {
// сохранить последнее время
        this.lastPlayTimePlayer = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
        log.info(this.name + " LAST TIME: " + this.lastPlayTimePlayer + " LAST PATH: " + this.lastPathPlayer);
        lmsPlayers.write();
        return this;
    }

    public Player saveLastPathThis() {
// охранить последний путь
        String path = this.path();
        if (path != null && !path.equals(config.silence)) {
            this.lastPathPlayer = path;
        }
        log.info(this.name + " LAST PATH THIS: " + this.lastPathPlayer);
        lmsPlayers.write();
        return this;
    }

    public Player saveLastTimePath() {
// сохранить последнее время
        if (this.playing) this.lastPlayTimePlayer = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
// охранить последний путь
        String path = this.path();
        if (path != null && !path.equals(config.silence)) {
            this.lastPathPlayer = path;
            Player.lastPathCommon = path;
        }
        log.info(this.name + " LAST TIME: " + this.lastPlayTimePlayer + " LAST PATH: " + this.lastPathPlayer);
        lmsPlayers.write();
        return this;
    }

    public void remove() {
        lmsPlayers.players.remove(this);
    }

    public Player separateFlagTrue() {
        this.separate = true;
        log.info("SEPARTE FLAG: " + this.name + " " + this.separate);
        return this;
    }

    public Player separateFlagFalse() {
        this.separate = false;
        log.info("SEPARTE FLAG: " + this.name + " " + this.separate);
        return this;
    }

    // Алиса включи отдельно - для плеера включить флаг отделен, отключить от группы, если играл то продолжить
    public Player separateOn() {
        log.info("SEPARATE ON START. PLAYER: " + this);
//        если уже отделен - ничего не делать, если не играет то разбудить,включить последнее
        if (this.separate) {
            log.info("ALREADY SEPARATE ON, UNSYNC, WAKE, PLAY LAST ");
            if (!this.playing) this.turnOnMusic(null);
            return this;
        }
//        если был не отделен
        log.info("SEPARATE ON, UNSYNC, WAKE, PLAY LAST ");
        this
                .separateFlagTrue() // включить флаг отделен
                .unsync(); // отключить от группы
//        разбудить, включить последнее
        if (!this.playing) this.turnOnMusic(null);
        else
            this.play();
        log.info("SEPAREATE ON FINISH. PLAYER: " + this);
        return this;
    }

    public Player separateOff() { // включи вместе
        log.info("SEPARATE OFF ALL. SYNC ALL PLAYING TO HERE");
        this.separateFlagFalse(); // сбросить флаг отделен у этото плеера
        if (!this.playing) { // если плеер не играл - подключить к играющему
            this.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null)
                    .unsync()
                    .syncToPlayingOrPlayLast(); // если плеер не играл и нет играющего - включить последнее игравшее
        }
        lmsPlayers.players.forEach(player -> player.separateFlagFalse()); // сбросить у всех флаг что отделен
// подключить к нему остальные играющие
        lmsPlayers.playingPlayers(this.name, false) // найти все играющие включая отделенные
                .stream().filter(Objects::nonNull)
                .forEach(player -> player
                        .unsync()
                        .syncTo(this.name));
        return this;
    }

    public Player syncOtherPlayingNotInGroupToThis() {
        log.info("SYNC OTHER PLAYING TO THIS " + this.name);
        if (this.separate) {
            log.info("PLAYER IS SEPARETE. STOP SYNC");
            return this;
        }
        Map<String, List<String>> gpoups = this.playingPlayersNameGroups(true);
        List<String> playingPlayersNamesNotInCurrentGrop = gpoups.get("notInGroup");

        playingPlayersNamesNotInCurrentGrop.stream()
                .map(name -> lmsPlayers.playerByName(name))
                .filter(player -> !player.separate)
                .forEach(player -> player
                        .unsync()
                        .syncTo(this.name));
        return this;
    }

    public boolean checkLastPlayTimeExpired() {
// 10 минут до сброса громкости на значаение по пресету когда не играет
        long delayExpire = lmsPlayers.delayExpire;
        if (this.lastPlayTimePlayer == null) {
            log.info("EXPIRED " + this.name + " LAST TIME " + this.lastPlayTimePlayer);
            return true;
        }
        LocalTime playerTime = LocalTime.parse(this.lastPlayTimePlayer).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(MINUTES);
        long diff = playerTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        log.info("LAST PLAY TIME: " + this.lastPlayTimePlayer + " NOW: " + nowTime + " DELAY MINUTES: " + delayExpire + " DIFF: " + diff + " EXPIRED: " + expired);
        return expired;
    }

    public Map<String, List<String>> playingPlayersNameGroups(Boolean exceptSeparated) {
        log.info("SEARCH FOR PLAYERS PLAYING IN CURRENT GROUP AND OTHER GROUP");
        // Получить имена играющих плееров
        List<String> playingPlayersNames = lmsPlayers.playingPlayersNames(this.name, exceptSeparated);
        // Получить группы плееров
        List<List<String>> groups = lmsPlayers.syncgroups();
        // Определить множество игроков в текущей группе (исключая текущего)
        Set<String> playersInCurrentGroup;
        if (groups != null) {
            playersInCurrentGroup = groups.stream()
                    .filter(list -> list.contains(this.name))
                    .findFirst()
                    .map(list -> list.stream()
                            .filter(e -> !e.equals(this.name))
                            .collect(Collectors.toSet()))
                    .orElse(Collections.emptySet());
        } else {
            playersInCurrentGroup = Collections.emptySet();
        }
        // Разделить играющих игроков на две категории
        List<String> inGroup = new ArrayList<>();
        List<String> notInGroup = new ArrayList<>();
        for (String playerName : playingPlayersNames) {
            if (playersInCurrentGroup.contains(playerName)) {
                inGroup.add(playerName);
            } else {
                notInGroup.add(playerName);
            }
        }
        log.info("IN CURRENT GROUP: " + inGroup + " NOT IN CURRENT GROUP: " + notInGroup);
        Map<String, List<String>> result = new HashMap<>();
        result.put("inGroup", inGroup);
        result.put("notInGroup", notInGroup);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "ROOM:%-10s" +
                        "NAME:%-15s " +
                        "CONNECTED:%-5b " +
                        "SEPARATED:%-5b " +
                        "SYNC:%-5b " +
                        "VOL:%-3s " +
                        "MODE:%-7s " +
                        "PLAY:%-5b " +
                        "TIME:%-5s ",
                room,
                name,
                connected,
                separate,
                sync,
                volume,
                mode,
                playing,
                lastPlayTimePlayer
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(getName(), player.getName()) && Objects.equals(getMac(), player.getMac());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMac());
    }

}