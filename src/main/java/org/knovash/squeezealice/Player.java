package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.PlayerStatus;
import org.knovash.squeezealice.lms.RequestParameters;
import org.knovash.squeezealice.lms.Response;
import org.knovash.squeezealice.spotify.Spotify;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Utils;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    public String name;
    public String nameInQuery;
    public String room;
    public String deviceId;
    public String mac;
    public Integer volume_step;
    public Integer volume_low;
    public boolean playing;
    public Integer volume_high;
    public Integer delay;
    public boolean separate = false;
    public boolean connected = false;
    public Map<Integer, Integer> schedule;
    public String title;
    public String currentTrack;
    public String lastPath;
    public String lastPlayTime;
    public int lastChannel = 0; // для переключения канал. сохраняется в next и prev channel
    public static PlayerStatus playerStatus = new PlayerStatus();
    public String mode;
    public boolean sync;
    public String volume;
    public String lastTitle; // чтото непонятное выпилено из таскер
    private List<String> playlist = new ArrayList<>();
    public Integer currentChannel = 0;

    public Player(String name, String mac) {
        this.name = name;
        this.mac = mac;
        this.title = "непонятно";
        this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 100;
        this.delay = 10;
        this.schedule = new HashMap<>(Map.of(
                0, 10,
                7, 15,
                9, 20,
                20, 15,
                22, 10));
    }

// LMS получение данных ------------------------------------------------------------------------------------------------

    public static String count() {
//        log.info("COUNT START");
        Response response = Requests.postToLmsForResponse(RequestParameters.count().toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("COUNT PLAYERS IN LMS: " + response.result._count);
        return response.result._count;
    }

    public static String name(String index) {
        Response response = Requests.postToLmsForResponse(RequestParameters.name(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response = Requests.postToLmsForResponse(RequestParameters.id(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

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

    public String checkPlayerConnected() {
        this.playing = false;
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " CONNECTED");
        return response.result._mode;
    }

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracks() {
        Response response = Requests.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

//    public String playlist() {
//
//        this.status(20);
////        Response response = Requests.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
////        if (response == null) {
////            log.info("REQUEST ERROR " + this.name);
////            return "0";
////        }
////        log.info("PLAYER: " + this.name + " TRACKS: " + response.result.title);
//        return "PLAYLIST";
//    }

//    public List<String> favorites() {
//        String playerName = this.name;
//        Response response = Requests.postToLmsForResponse(RequestParameters.favorites(playerName, 10).toString());
//        List<String> playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
//        log.info("FAVORITES: " + playlist);
//        return playlist;
//    }

    public String playlistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " PLAYLISTNAME: " + response.result._name);
        return response.result._name;
    }

    public String playlistUrl() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlisturl(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " PLAYLISTURL: " + response.result._url);
        return response.result._url;
    }

    public String albumName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.albumname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " ALBUMNAME: " + response.result._album);
        return response.result._album;
    }

    public String trackname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.trackname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
        log.info("PLAYER: " + this.name + " TRACKNAME: " + response.result._title);
        return response.result._title;
    }

    public String artistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " ARTISTNAME: " + response.result._artist);
        return response.result._artist;
    }

    public String volumeGet() {
        Response response = Requests.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " GET VOLUME: " + response.result._volume);
        return response.result._volume;
    }

    public List<String> favorites() {
//        log.info("FAVORITES START " + this.name);
        List<String> favorites = null;
        try {
            Response response = Requests.postToLmsForResponse(RequestParameters.favorites("", 100).toString());
            favorites = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("ERROR " + e);
        }
        log.info("FAVORITES: " + favorites);
        return favorites;
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
        return this;
    }

    public Player playChannel(Integer channel) {
        this.currentChannel = channel;
        log.info("CHANNEL: " + channel + " PLAYER: " + this.name);
        Requests.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        Requests.postToLmsForStatus(RequestParameters.play(this.name, config.silence).toString());
        return this;
    }

    public Player ctrlPrevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        if (!this.tracks().equals("1"))
            Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public Player ctrlNextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        if (!this.tracks().equals("1"))
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
        Requests.postToLmsForStatus(RequestParameters.shuffleon(this.name).toString());
        return this;
    }

    public Player shuffleOff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        Requests.postToLmsForStatus(RequestParameters.shuffleoff(this.name).toString());
        return this;
    }

    public Player syncTo(String toPlayerName) {
        log.info("SYNC " + this.name + " TO " + toPlayerName);
        Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name);
        Requests.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        return this;
    }


// LMS сценарии --------------------------------------------------------------------------------------------------------


    // включить музыку
    public Player turnOnMusic(String volume) {
        log.info("TURN ON MUSIC PLAYER: " + this.name);
// узнать состояние плеера
        this.status(); // далее запускается в методе экспайред
// узнать есть ли играющие плееры
        String playingPlayerName = null;
//        найти играющие исключая отделенные
        this.playingPlayersNamesNotInCurrentGroup(true);
        if (lmsPlayers.playingPlayersNames != null && lmsPlayers.playingPlayersNames.size() > 0)
            playingPlayerName = lmsPlayers.playingPlayersNames.get(0);

// 1) если уже играет и нет других играющих - ничего не делать
        if (this.mode.equals("play")) {
//            if (ProviderAction.volumeByProvider != null) {
//                log.info("SET VILUME BY PROVIDER");
//                this.volumeSet(ProviderAction.volumeByProvider);
//            }
            if (volume != null) this.volumeSet(volume);
            log.info("PLAYING NOT IN GROUP: " + lmsPlayers.playingPlayersNamesNotInCurrentGrop);
            if (lmsPlayers.playingPlayersNamesNotInCurrentGrop == null ||
                    lmsPlayers.playingPlayersNamesNotInCurrentGrop.size() == 0) {
                log.info("PLAYER ALREADY PLAYING. NO OTHER PLAYING. SKIP TURN ON MUSIC");
                return this;
            }
        }

// отключить от группы на случай если не играет но был включен в группу
        this.unsync();
// если давно не играл - разбудить, установить громкость по пресету
        this.ifExpiredAndNotPlayingUnsyncWakeSet(volume); // лишняя проверка статуса и проверка что не играет

// 2) если не играет, отделён - играть последнее
        if (this.separate) {
            log.info("PLAYER IS SEPARATED - PLAY LAST");
            this.playLast();
// TODO проверить если не играет включить канал 1
            return this;
        }
// 3) если не играет, не отделен и нет играющего - включить последнее
        if (!this.separate && playingPlayerName == null) {
            log.info("NO PLAYING. PLAY LAST");
            this.playLast();
// TODO проверить если не играет включить канал 1
            return this;
        }
// 4) если играет или не играет, не отделен и есть играющий не в группе - подключиться к нему в группу
        if (!this.separate &&
                lmsPlayers.playingPlayersNamesNotInCurrentGrop != null &&
                lmsPlayers.playingPlayersNamesNotInCurrentGrop.size() > 0) {
            log.info("SYNC TO PLAYING " + playingPlayerName);
            this.syncTo(lmsPlayers.playingPlayersNamesNotInCurrentGrop.get(0));
            return this;
        }

// сохранить последнее время и путь
// обновить виджеты таскер
// перенесено после runInstance всех устройств
// Requests.autoRemoteRefresh();

// сохранение времени и пути должно выполняться для каждого плеера после выполнения действий


        log.info("WARNING. SKIPED TURN ON MUSIC");
        return this;
    }

    // выключить музыку
    public Player turnOffMusic() {
        log.info("PLAYER: " + this.name + " TURN OFF");
        this.unsync() // отключить от группы
                .pause(); // поставить на паузу
        return this;
    }

    // включить или выключить музыку. кнопка Play/Pause на пульте или виджет таскер
    public Player toggleMusic() {
        log.info("PLAYER: " + this.name + " TOGGLE MUSIC");
        this.status();
        if (this.playing) this.turnOffMusic();
        else this.turnOnMusic(null);
        return this;
    }


    public Player volumeSet(String value) {


        Integer currentVolume = Integer.valueOf(this.volumeGet());
//        log.info("VOLUME CURRENT: " + currentVolume + " NEW VALUE: " + value);
//        log.info("LIMIT: " + this.volume_high);

        Integer valueInt = 0;
        Integer newVolume = 0;

        if (value.contains("-")) {
            valueInt = Integer.valueOf(value.replace("-", ""));
            newVolume = currentVolume - valueInt;
        }
        if (value.contains("+")) {
            valueInt = Integer.valueOf(value.replace("+", ""));
            newVolume = currentVolume + valueInt;
        }
        if (!value.contains("+") && !value.contains("-")) {
            valueInt = Integer.valueOf(value.replace("+", ""));
            newVolume = valueInt;
        }

//        log.info("VALUE INT: " + valueInt);
//        log.info("NEW VOLUME: " + newVolume);

        if (newVolume > this.volume_high) {
            log.info("VOLUME LIMITED BY " + this.volume_high);
            value = String.valueOf(this.volume_high);
        }


        String status = null;
        log.info("PLAYER: " + this.name + " SET VOLUME: " + value);
        status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name);
            return this;
        }
        // TODO
        this.statusLite();

//  не понижать громкость ниже 1
        if (playerStatus.result.mixer_volume < 1) {
            status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, "1").toString());
            if (status == null || !status.contains("200")) {
                log.info("REQUEST ERROR " + this.name);
                return this;
            }
        }

        if (!playerStatus.result.mode.equals("play")) {
            if (!value.contains("+") && !value.contains("-")) {
                this.lastPlayTime = null;
                return this;
            }
            if (value.contains("+")) value = value.replace("+", "-");
            else value = value.replace("-", "+");
            Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        }
        return this;
    }


    public String volumeRelativeOrAbsolute(String value, Boolean relative) {
        log.info("PLAYER: " + this.name + " VOLUME: " + value + " RELATIVE: " + relative);
// if (Spotify.ifPlaying()) {
// Spotify.volumeGeneral(value, relative);
// return;
// }
        if (relative != null && relative.equals(true)) {
            if (value.contains("-")) {
                this.volumeSet(value);
            } else {
                this.volumeSet("+" + value);
            }
        }
        if (relative != null && relative.equals(false)) {
            this.volumeSet(value);
        }
        log.info("VOLUME " + this.volume);
        return this.name + "-" + this.volume;
    }

    public Boolean status() {
        return status(1);
    }


    public Boolean status(Integer tracks) {
//        log.info("STATUS PLAYER " + this.name);
        if (tracks == null) tracks = 0;
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(this.name, tracks).toString());
        if (json == null) {
            log.info("REQUEST ERROR request null");
            return false;
        }
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);

        if (playerStatus == null || playerStatus.result == null) {
            log.info("REQUEST ERROR {}",
                    playerStatus == null ? "json invalid" : "result null");
            return false;
        }

// сохранить плэйлист плеера
        this.playlist = Optional.ofNullable(playerStatus.result.playlist_loop)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .map(p -> p.title)
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
// сохранить что сейчас играет
        if (playerStatus.result.remoteMeta != null) this.currentTrack = playerStatus.result.remoteMeta.title;
        this.title();
// сохранить текущую громкость плеера
        this.volume = String.valueOf(playerStatus.result.mixer_volume);
        if (playerStatus.result.mode.equals("play")) {
            this.playing = true;
            this.mode = "play";
        } else {
            this.playing = false;
            this.mode = "stop";
        }
// сохранить синхронизирован ли плеер с другими
        this.sync = false;
        if (playerStatus.result.sync_slaves != null || playerStatus.result.sync_master != null) {
            this.sync = true;
        }
// статут плеера успешно обновлен
        return true;
    }

    public Boolean statusLite() {
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(this.name, 0).toString());
        if (json == null) {
            log.info("REQUEST ERROR request null");
            return false;
        }
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null) {
            log.info("REQUEST ERROR json invalid");
            return false;
        }

//        if (playerStatus.result.playlist_loop != null) {
//            this.playlist = playerStatus.result.playlist_loop.stream()
//                    .map(p -> p.title)
//                    .collect(Collectors.toList());
//        } else this.playlist = new ArrayList<>();

//        if (playerStatus.result.remoteMeta != null)
//            this.currentTrack = playerStatus.result.remoteMeta.title;

        this.volume = String.valueOf(playerStatus.result.mixer_volume);
        if (playerStatus.result.mode.equals("play")) {
            this.playing = true;
            this.mode = "play";
        } else {
            this.playing = false;
            this.mode = "stop";
        }
        this.sync = false;
        if (playerStatus.result.sync_slaves != null || playerStatus.result.sync_master != null) {
            this.sync = true;
        }
        return true;
    }


    public String playChannelRelativeOrAbsolute(String value, Boolean relative, String volume) {
        log.info("CHANNEL: " + value + " RELATIVE: " + relative + " VOLUME: " + volume + " PLAYER: " + this.name);
        int channel = Integer.parseInt(value);
        int delta = Integer.parseInt(value);

        if (relative != null && relative.equals(true)) {
            channel = 1;
            // определить канал сейчас попробовать найти его номер в избранном
            this.currentChannelIndexInFavorites();
            if (lastChannel != 0) channel = lastChannel + delta;
            if (currentChannel != 0) channel = currentChannel + delta;
            log.info("RELATIVE: " + relative +
                    " GET CURRENT CHANNEL: " + currentChannel +
                    " SET CHANNEL: " + channel +
                    " PLAYER: " + this.name);
        }

        log.info("PLAYER: " + this.name + " CHANNEL: " + channel + " RELATIVE: " + relative);
// проверить если долго не играл - разбудить, установить громкость по пресету
        this.ifExpiredAndNotPlayingUnsyncWakeSet(volume);

// включить канал, также и на другом плеере если был играющий
        this.playChannel(Integer.valueOf(channel)); // playChannelRelativeOrAbsolute
// пауза для включения канала на плеере и определения пути
        this.waitFor(2000);
        lmsPlayers.lastChannel = channel;
        log.info("PLAYER: " + this.name + " PLAY CHANNEL FINISH");
        return "PLAY CHANNEL " + channel;
    }

    // выбор предыдущего канала через определение что сейчас играет или от последнего сохраненного канала
    public Player ctrlPrevChannel() {
        int channel = 1;
        List<String> favList = this.favorites();
        int favSize = favList.size();
        String playlistName = this.playlistName();
        log.info("CURRENT PLAYLIST NAME " + playlistName + " CONTAINS IN FAVORIRES " + favList.contains(playlistName));
        Integer currentIndex = 0;
        Integer nextIndex = 0;
        if (favList.contains(playlistName)) {
            currentIndex = favList.indexOf(playlistName) + 1;
            currentChannel = currentIndex;
            nextIndex = currentIndex;
            log.info("CURRENT INDEX IN FAVORITES " + currentIndex + " PLAY NEXT " + nextIndex);
            this.lastChannel = currentIndex;
        }
        if (this.lastChannel != 0) channel = this.lastChannel - 1;
        else channel = lmsPlayers.lastChannel - 1;
        if (channel < 1) channel = favSize;
        log.info("PLAY PREV CHANNEL: " + channel + " LAST CHANNEL: " + lmsPlayers.lastChannel);
        this.ifExpiredAndNotPlayingUnsyncWakeSet(null);
        this.playChannel(channel); // prevChannel
        return this;
    }

    public Integer currentChannelIndexInFavorites() {
        String currentTitle = this.playlistName();
        currentChannel = 0;
        List<String> favList = this.favorites();
        if (favList.contains(currentTitle)) {
            currentChannel = favList.indexOf(currentTitle) + 1;
        }
        return currentChannel;
    }


    // выбор предыдущего канала через определение что сейчас играет или от последнего сохраненного канала
    public Player ctrlNextChannel() {
        log.info("LAST CHANNEL THIS: " + this.lastChannel + " COMMON: " + lmsPlayers.lastChannel);
        int channel = 1;
        List<String> favList = this.favorites();
        int favSize = favList.size();
        String playlistName = this.playlistName();
        log.info("CURRENT PLAYLIST NAME " + playlistName + " CONTAINS IN FAVORIRES " + favList.contains(playlistName));
        Integer currentIndex = 0;
        Integer nextIndex = 0;
        if (favList.contains(playlistName)) {
            currentIndex = favList.indexOf(playlistName) + 1;
            nextIndex = currentIndex + 2;
            log.info("CURRENT INDEX IN FAVORITES " + currentIndex + " PLAY NEXT " + nextIndex);
            this.lastChannel = currentIndex;
        }
        if (this.lastChannel != 0) {
            channel = this.lastChannel + 1;
            log.info("CHANNEL THIS+1: " + channel);
        } else {
            channel = lmsPlayers.lastChannel + 1;
            log.info("CHANNEL COMMON+1: " + channel);
        }
        if (channel > favSize) channel = 1;
        log.info("PLAY CHANNEL: " + channel);
        this.ifExpiredAndNotPlayingUnsyncWakeSet(null);
        this.playChannel(channel); // prevChannel
        return this;
    }

    public Player ctrlNextChannelOrTrack() {
        log.info("PLAYER: " + this.name + " NEXT");
        int tracks = Integer.parseInt(this.tracks());
        log.info("TRACKS IN PLAYLIST: " + tracks);
        if (tracks < 2) this.ctrlNextChannel();
        else this.ctrlNextTrack();
        return this;
    }

    public Player ctrlPrevChannelOrTrack() {
        log.info("PLAYER: " + this.name + " PREV");
        int tracks = Integer.parseInt(this.tracks());
        log.info("TRACKS IN PLAYLIST: " + tracks);
        if (tracks < 2) this.ctrlPrevChannel();
        else this.ctrlPrevTrack();
        return this;
    }

    public String favoritesAdd() {
        log.info("START ADD ON " + this.name);
        this.status();
        String playerName = this.name;
        String url = this.path();
        String title = this.title;
        log.info("NOW PATH: " + url);
        if (url.contains("spotify")) {
            url = Spotify.lastPath;
            title = Spotify.lastTitle;
            log.info("SPOTIFY LAST: " + url);
        }
        int size = this.favorites().size() + 1;
        log.info("FAVORITES BEFORE SIZE: " + size);
        title = size + " " + title;
        favoritesAdd(url, title);
        log.info("FAVORITES ADD");
        return title;
    }

//    public Player syncToAlt(String toPlayerName) {
//// альтернативная синхронизация если глючит синхронизация di.fm
//        String path = lmsPlayers.playerByCorrectName(toPlayerName).path();
//        if (path.contains("di.fm") || path.contains("audioaddict")) {
//            log.info("SYNC DI.FM " + this.name + " PLAY PATH FROM " + toPlayerName);
//            this.playPath(path);
//            return this;
//        } else {
//            log.info("SYNC " + this.name + " TO " + toPlayerName);
//            Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
//        }
//        return this;
//    }

    public Player ifExpiredAndNotPlayingUnsyncWakeSet(String volume) {
//        log.info("CHECK IF EXPIRED PLAYER: " + this.name + " SET VOLUME BY PROVIDER: " + volume);
        if (!this.status(0)) {
            log.info("ERROR STATUS NULL !!!");
            return this;
        }
        if (!this.playing && this.ifTimeExpired(null)) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET, VOLUME " + volume);
            this.unsync().wakeAndSet(volume);
            return this;
        } else {
//            log.info("SKIP WAKE " + this.name + " VOLUME BY PROVIDER: " + volume);
            if (volume != null) {
                log.info("SKIP WAKE, SET VOLUME BY PROVIDER: " + volume + " PLAYER: " + this.name);
                this.volumeSet(volume);
            } else log.info("SKIP WAKE, SKIP SET " + this.name + " VOLUME BY PROVIDER: " + volume);

        }
//        if (this.playing && this.ifTimeExpired(60)) {
//            log.info("PLAYER " + this.name + " PLAY TOO LONG - UNSYNC, WAKE, SET");
//            this.unsync().wakeAndSet();
//            return this;
//        }
        return this;
    }

    public Player wakeAndSet(String volume) {
        log.info("");
        log.info("WAKE START PLAYER: " + this.name + " WAIT: " + this.delay + " VOLUME: " + volume);
// если пришла громкость от провайдера то использовать ее вместо громкости по пресету
        if (volume == null) volume = String.valueOf(this.valueVolumeByTime());
        else log.info("VOLUME SET BY PROVIDER: " + volume + " " + this.name);
        this
                .playSilence()
                .volumeSet("+1")
                .volumeSet(volume)
                .waitForWakeSeconds()
                .volumeSet("-1")
                .volumeSet(volume)
                .pause();
        log.info("WAKE FINISH");
        log.info("");
        return this;
    }

    public Player stopAllOther() {
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name))
                .forEach(player -> player.unsync().pause());
        return this;
    }

    public Player playLast() {
        log.info("PLAY LAST");
        String thisPath = this.path();
        String thisLastPath = this.lastPath;
        String commonLastPath = lmsPlayers.lastPath;
        String lastPath;
// log.info("THIS PATH: " + thisPath);
// log.info("THIS LAST PATH: " + thisLastPath);
// log.info("TRY GLOBAL LAST PATH: " + commonLastPath);
// log.info("SILENCE PATH: " + silence);
// log.info("LAST THIS STATE: " + lmsPlayers.lastThis);

//        if (lmsPlayers.lastThis) lastPath = thisLastPath;
//        else lastPath = commonLastPath;

// если у этого плеера есть путь и он не тишина - играть
        if (thisPath != null && !thisPath.equals(config.silence) && !thisPath.equals("")) {
            log.info("PUSH PLAY BUTTON: " + thisPath);
            this.play();

            // TODO проверить если не играет включить канал 1
            replay();

            return this;
        }
// если у этого плеера нет пути играть последний сохраненый этого
        if (thisLastPath != null && !thisLastPath.equals(config.silence) && !thisLastPath.equals("") && lmsPlayers.lastThis) {
            log.info("PLAY THIS LAST PATH: " + thisLastPath);
            this.playPath(thisLastPath);

            // TODO проверить если не играет включить канал 1
            replay();

            return this;
        }
// если у этого плеера нет пути и нет последнего этого то играть последний сохраненный общий
        if (commonLastPath != null && !commonLastPath.equals(config.silence) && !commonLastPath.equals("")) {
            log.info("PLAY COMMON LAST PATH: " + commonLastPath);
            this.playPath(commonLastPath);

            // TODO проверить если не играет включить канал 1
            replay();

            return this;
        }


        log.info("PLAY CHANNEL 1");
        this
                .playChannel(1); // playLast
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
        this.volumeSet(String.valueOf(e.getValue()));
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

    public Player waitForWakeSeconds() {
        log.info("WAIT " + delay + " . . . . .");
        try {
            Thread.sleep(this.delay * 1000);
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

    //   должно выполняться после действия с плеером
    public Player saveLastTimePathAutoremoteRequest() {
//        сохранить последнее время
        saveLastTimePath();
//        записать в файл lmsPlayers.json
//        запрос обновления виджетов
        lmsPlayers.autoremoteRequest();
        return this;
    }

    public Player saveLastTimePath() {
//        сохранить последнее время
        this.lastPlayTime = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
        log.info("SAVE LAST TIME: " + this.lastPlayTime);
//        сохранить последний путь
        String path = this.path();
        if (path != null && !path.equals(config.silence)) {
            this.lastPath = path;
            lmsPlayers.lastPath = this.lastPath;
            log.info("SAVE LAST PATH: " + this.lastPath);
        }
//        сохранить последний тайтл
        this.status();
        this.lastTitle = this.title;
        lmsPlayers.lastTitle = this.title;
        log.info("SAVE LAST TITLE: " + this.title);
        return this;
    }


//    public Player saveLastChannel(int channel) {
//        log.info("SAVE LAST CHANNEL: " + channel);
//        lmsPlayers.lastChannel = channel;
//        this.lastChannel = channel;
//        this.status();
////        this.lastTitle = this.title;
////        lmsPlayers.lastTitle = this.title;
//        return this;
//    }

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
        log.info("SEPARATE ON");
        log.info("THIS SEPARATE: " + this.separate);
//        если уже отделен - ничего не делать
        if (this.separate) {
            log.info("ALREADY SEPARATE ON ");
            return this;
        }
//        если этот играл - то после отсоединения снова включить
        String mode = this.mode();
        this
                .separateFlagTrue() // включить флаг отделен
                .unsync(); // отключить от группы
//        если этот играл - то включить
        if (mode.equals("play")) this.playLast();
        return this;
    }

    // Таскер вместе - выключить флаг отделен. если играл то подключить к играющему если нет то ничего не делать
    public Player separateOff() {
        log.info("SEPARATE OFF");
        log.info("THIS SEPARATE: " + this.separate);
//        если уже не отделен - ничего не делать
        if (!this.separate) {
            log.info("ALREADY SEPARATE OFF");
            return this;
        }
        this.separateFlagFalse(); // выключить флаг отделен
//        if (playingPlayer != null) this.syncTo(playingPlayer.name);
        String mode = this.mode();
//        если этот не играл то выход
        if (!mode.equals("play")) return this;
//        если этот играет и есть играющий то подключиться к играющему
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, true);
        if (playingPlayer != null) this.syncTo(playingPlayer.name);
        return this;
    }

    // Алиса включи вместе - все сброисть флаг отключен, все играющие подключить к этому
// если плеер не играл - подключить к играющему
// если плеер не играл и нет играющего - включить последнее игравшее
// если плеер играл - подключить к нему остальные играющие
    public Player separateOffAll() {
        log.info("SEPARATE OFF ALL");
// найти играющий плеер (внутри обновление плееров ЛМС)
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, true);
// сбросить для каждого плеера флаг отделен
        lmsPlayers.players.stream().forEach(p -> p.separateFlagFalse());
// если плеер не играет и есть играющий - подключить к играющему
        if (!this.playing && playingPlayer != null) this.syncTo(playingPlayer.name);
// если плеер не играет и нет играющего - включить последнее игравшее
        if (!this.playing && playingPlayer == null) this.playLast();
// подключить к нему остальные играющие
        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name)) // выбрать все кроме этого
                .filter(p -> p.playing) // выбрать играющие
                .forEach(p -> CompletableFuture.runAsync(() -> p
                        .unsync() // отключить
                        .syncTo(this.name) // подключить к этому
                ));
        return this;
    }


    public void title() {
// запрашивать "playlist", "name" для Soma и Di. для Spoty нету. запросить "artist"
        log.info("PLAYER: " + this.name + " TITLE: " + this.title);
        String title = this.playerStatus.result.current_title;
        String currentTitle = this.playerStatus.result.current_title;
        String remoteMetaTitle = this.playerStatus.result.remoteMeta.title;

        title = remoteMetaTitle;
        if (currentTitle.contains("DI.FM")) {
            title = currentTitle;
            if (title.contains(": ")) title = title.replaceAll(":.*", "");
            if (title.contains(" - ")) title = title.replaceAll(" - .*", "");
            this.title = title;
            log.info("PLAYER: " + this.name + " TITLE: " + this.title);
            return;
        }


// log.info("CURRENT TITLE: " + this.title);
        if ((title != null) && (title != "")) {
            if (title.contains(": ")) title = title.replaceAll(":.*", "");
            if (title.contains(" - ")) title = title.replaceAll(" - .*", "");
        }
        if ((title == null) || (title == "")) title = this.artistName();
        if (title == null) title = "непонятно";
        if (title.length() > 50) title = title.substring(0, 20);
        title = title.replaceAll(",", " ");
        title = title.replaceAll(":", " ");
        title = title.replaceAll("  ", " ");
        this.title = title;
//        log.info("TITLE: " + this.title);
    }

    // для востановления после отделения и для спотифай
    public Player syncAllOtherPlayingToThis() {
        log.info("SYNC OTHER PLAYING TO THIS START " + this.name);
//        if (!lmsPlayers.syncAlt) {
//            log.info("ALT SYNC OFF");
//            return this;
//        }
        if (this.separate) {
            log.info("PLAYER IS SEPARETE. STOP ALT SYNC");
            return this;
        }
        log.info("SEARCH PLAYING PLAYERS...");
        List<Player> playingPlayers = lmsPlayers.playingPlayers(this.name, true);
        log.info("PLAYING PLAYERS: " + playingPlayers);
        if (playingPlayers == null) return this;
        log.info("UNSYNC ALL PLAYING PLAYERS...");
// все играющие плееры поочереди отключить от их группы
        playingPlayers.forEach(p -> p.unsync());
        log.info("SYNC ALL NOW PLAYING PLAYERS");
// все играющие плееры поочереди подключить в новую группу
        playingPlayers.forEach(p -> p.syncTo(this.name));
        log.info("FINISH <<<");
        return this;
    }

    public boolean ifTimeExpired(Integer delayMinutes) {
        long delayExpire = lmsPlayers.delayExpire;
        if (delayMinutes != null) delayExpire = delayMinutes;
        if (this.lastPlayTime == null) return true;
        LocalTime playerTime = LocalTime.parse(this.lastPlayTime).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(MINUTES);
        long diff = playerTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        log.info("LAST PLAY TIME: " + this.lastPlayTime + " DELAY MINUTES: " + delayMinutes + " DIFF: " + diff + " EXPIRED: " + expired);
//        log.info("EXPIRED: " + expired + " DIFF:" + diff + " DELAY:" + delayExpire);
        return expired;
    }

    public Player switchToHere() {
        log.info("TRY TRANSFER IF SPOTIFY PLAY");
        if (Spotify.transfer(this)) {
            log.info("SPOTIFY TRANSFER TO " + this.name + " FINISHED OK");
            return this;
        }
        if (this.mode().equals("play")) {
            log.info("PLAYER PLAYING. STOP ALL OTHER");
            this.stopAllOther();
            return this;
        }
        log.info("PLAYER NOT PLAY. SYNC TO PLAYING OR PLAY LAST. STOP ALL OTHER");
        this.turnOnMusic(null).stopAllOther();
        return this;
    }

    public List<String> playingPlayersNamesNotInCurrentGroup(Boolean exceptSeparated) {
        log.info("SEARCH FOR PLAYERS PLAYING NOT IN CURRENT GROUP");
// найти плееры которые играют кроме этого
        lmsPlayers.playingPlayersNames = lmsPlayers.playingPlayersNames(this.name, exceptSeparated);
//        log.info("PLAYING PLAYERS ALL: " + lmsPlayers.playingPlayersNames);
// найти группы плееров
        List<List<String>> groups = lmsPlayers.syncgroups();
//        log.info("GROUPS: " + groups);
// найти плееры которые в одной группе с этим
        if (groups != null)
            lmsPlayers.playersNamesInCurrentGroup = groups.stream()
                    .filter(list -> list.contains(this.name)) // Ищем подсписок с этим плеером
                    .findFirst() // Берем первый найденный подсписок
                    .map(list -> list.stream()
                            .filter(e -> !e.equals(this.name)) // Удаляем этот плеер из подсписка
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList()); // Если ничего не найдено, вернем пустой список
        else lmsPlayers.playersNamesInCurrentGroup = new ArrayList<>();
//        находим плееры которые играют не в этой группе
        lmsPlayers.playingPlayersNamesNotInCurrentGrop = lmsPlayers.playingPlayersNames;
        lmsPlayers.playingPlayersNamesNotInCurrentGrop.removeAll(lmsPlayers.playersNamesInCurrentGroup);


//        log.info("OTHER PLAYING PLAYERS NAMES: " + lmsPlayers.playingPlayersNames);
//        log.info("PLAYERS IN CURRENT GROP: " + lmsPlayers.playersNamesInCurrentGroup);
//        log.info("PLAYERS PLAYING NOT IN CURRENT GROP: " + lmsPlayers.playingPlayersNamesNotInCurrentGrop);

        log.info("IN CURRENT GROP: " + lmsPlayers.playersNamesInCurrentGroup + " NOT IN CURRENT GROP: " + lmsPlayers.playingPlayersNamesNotInCurrentGrop);

//        log.info("FINISH");
        return lmsPlayers.playingPlayersNamesNotInCurrentGrop;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", room='" + room + '\'' +
                ", mode='" + mode + '\'' +
                ", sync='" + sync + '\'' +
                ", deviceid='" + deviceId + '\'' +
                ", title='" + title + '\'' +
                '}';
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

    public List<String> playlistList() {
        log.info("111");
//        получить плейлист плеера из result.playlist_loop.stream()
        this.status(250);
        log.info("222");
//        this.tracks();
//        log.info("333");
        log.info(this.playlist);

        return this.playlist;
    }

    public String forTaskerPlaylist(String lines) {
        log.info("FOR PLAYLIST START >>> ----------------");
        List<String> list = playlistList();
        log.info(list);
        if (list == null) return "---";

        if (list.get(0) == null) return "---";

//        удалить из названий символы , потому что в таскере , разделитель строк
        list.replaceAll(t -> t.replaceAll(",", " "));


        LmsPlayers.nowPlaying = this.title; // для виджета одной иконкой для телефона где неработает плагин

// если в плейлисте только один трэк (радио)
        if (list.size() == 1) {
            log.info("ONE TRACK TITLE: " + this.title);
//            log.info("ONE TRACK");
            String currentTrack = this.currentTrack;
//            if (currentTrack.length() > 20) currentTrack = currentTrack.substring(0, 20);
            String result = this.title;
            log.info("RESULT: " + result);
            return result;
        }
// найти номер играющего трека в плейлисте
//        log.info("CURRENT TRACK: " + this.currentTrack);
        int index = list.indexOf(this.currentTrack);
//        log.info("INDEX: " + index);
// добавление > к сейчас играющему треку
        list.replaceAll(l -> l.equals(this.currentTrack) ? ">" + l : "  " + l);
//        log.info("PLAYLIST > : " + list);
        // нумерация плейлиста
        for (int i = 0; i < list.size(); i++) {
            String numbered = (i + 1) + ". " + list.get(i);
            list.set(i, numbered);
        }
//       показывать только часть плейлиста
        list = Utils.linesFromList(list, index, Integer.parseInt(lines));
        String result = String.join(", ", list);
        log.info("FOR PLAYLIST FINISH <<< ----------------");
        return result;
    }


    public void replay() {
        // TODO проверить если не играет включить канал 1
        Utils.sleep(3000);
        log.info("CHECK PLAY");
        String mode = this.mode();
        if (!mode.equals("play")) {
            log.info("CHECK PLAY FAILED. PLAY CHANNEL 1");
            this.playChannel(1);
        }
    }


}