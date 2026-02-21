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
import org.knovash.squeezealice.yandex.Yandex;

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
    public Integer volume_high;
    public String volume;
    public Map<Integer, Integer> schedule;

    public boolean playing;
    public String mode;

    public String title;
    //    public String remoteMetaTitle; // определяется  playerStatus.result.remoteMeta.title
    public Integer currentChannel = 0;
    public String playlistName;
    public String playlistNameShort;

    public String lastPath;
    public String lastPlayTime;

    public Integer delay;
    public boolean separate = false;
    public boolean connected = false;
    public int lastChannel = 0; // для переключения канала в next и prev channel
    public PlayerStatus playerStatus;
    public boolean sync;

    public List<String> playlist = new ArrayList<>();
    public String playlistCurrentIndex = null;

    public Player(String name) {
        this.name = name;
        this.mac = mac;
        this.title = "unknown";
        this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 50;
        this.delay = 10;
        this.schedule = new HashMap<>(Map.of(
                0, 10,
                7, 15,
                9, 20,
                20, 15,
                22, 5));
    }

    public void clean() {
        log.debug("CLEAN " + this.name);
        this.connected = false;
        this.mode = "offline";
        this.playing = false;
        this.volume = "-";
        this.sync = false;
        this.title = "offline";
        this.playlistNameShort = "offline"; // clean
        this.playlistName = "offline";
    }

    public void requestPlayerStatus() {
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(this.name, 200).toString());
        if (json == null) {
            this.playerStatus = null;
            log.info("ERROR REQUEST PLAYER STATUS NULL " + this.name);
            return;
        }
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        this.playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) {
            log.info("ERROR PLAYER STATUS JSON INVALID " + this.name);
        }
    }


    public void status() {
        requestPlayerStatus();
        if (this.playerStatus == null) {
            this.clean();
            log.info(this);
            return;
        }
// PLAYLIST
        this.playlistCurrentIndex = this.playerStatus.result.playlist_cur_index;

        this.playlist = Optional.ofNullable(this.playerStatus.result.playlist_loop)
                .filter(list -> !list.isEmpty())
                .map(list -> list.stream()
                        .filter(Objects::nonNull)
                        .map(p -> p.title)
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
// CONNECTED
        if (this.playerStatus.result.player_connected == 1) this.connected = true; // status
        if (this.playerStatus.result.player_connected == 0) this.connected = false; // status
// VOLUME
        this.volume = String.valueOf(this.playerStatus.result.mixer_volume);
// MODE PLAYING
        if (this.playerStatus.result.mode.equals("play")) {
            this.playing = true;
            this.mode = "play"; // status
        } else {
            this.playing = false; // status
            this.mode = "stop"; // status
        }
// SYNC
        if (this.playerStatus.result.sync_slaves != null || this.playerStatus.result.sync_master != null) {
            this.sync = true;
            this.separate = false;
        } else {
            this.sync = false;
        }
        log.info(this);
    }


    public String requestTitle() {
        if (!this.connected) {
            this.clean();
            log.info("GET TITLE PLAYER: " + this.name + " ERROR - NOT CONNECTED");
            return "offline";
        }

        log.info("GET TITLE PLAYER: " + this.name);
        String title = null;

//        log.info("CURRENT TITLE     : " + currentTitle); // выбирается для di.fm Chill EDM - DI.FM Premium, Smooth Jazz - JAZZRADIO.com Premium
//        log.info("REMOTE META TITLE : " + remoteMetaTitle); // название трека, неоч нужно, Boogie Down
//        log.info("PLAYLIST NAME     : " + playlistName); // выбирается для di.fm Chill EDM, Smooth Jazz
//        log.info("ARTIST NAME       : " + artistName); // выбирается для Spotify SG Lewis
//        log.info("ALBUM NAME        : " + albumName); // возможен null
//        log.info("PATH              : " + path);

        String path = this.path();

        if (path != null && path.contains("spotify")) {
            title = this.artistName();
            if (title == null) title = this.albumName();
            if (title == null) title = "Spotify";
            return title;
        }

        title = this.requestPlaylistName();
        if (title == null && path != null) {
            if (path.contains("audioaddict.com/v1/di/")) title = "DI.FM";
            if (path.contains("audioaddict.com/v1/zenradio/")) title = "ZenRadio";
            if (path.contains("audioaddict.com/v1/jazzradio")) title = "JazzRadio";
            if (path.contains("soma")) title = "Soma";
        }

        if (title == null) title = "unknown";

        this.title = title;
        log.info("THIS TITLE: " + this.title);
        return title;
    }

    public String titleeee() {

        title = title
                .replaceAll(":.*", "")
                .replaceAll(" - .*", "")
                .replaceAll("\\(.*", "")
                .replaceAll("\\[.*", "")
                .replaceAll(",", " ")
                .replaceAll("  ", " ");
        if (title.length() > 30) title = title.substring(0, 30);
        log.info("TITLE: " + title);
        return "";
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

    public Boolean playing() {
        this.playing = false; // playing()
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return this.playing; // playing()
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode + " PLAYING: " + this.playing); // playing
        return this.playing; // playing()
    }

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
//        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracksRequest() { // TODO DEPRECATED

        Response response = Requests.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

    public String requestPlaylistName() {
// title() для названия плейлистов di.fm, soma, jazzradio
//        forTaskerPlayersList для играющих и не играющих получить // TODO тут надо использовать title
// для поиска названия плейлиста в избраном и определения индекса для переключения след и пред избранного
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " PLAYLISTNAME: " + response.result._name);
        this.playlistName = response.result._name;
        if (response.result._name != null) {
//            log.info("--- this.playlistNameShort = playlistName");
//            log.info("--- PLAYLIST NAME: " + playlistName);
            this.playlistNameShort = playlistName // playlistName
                    .replaceAll(":.*", "")
                    .replaceAll(" - .*", "")
                    .replaceAll("\\(.*", "")
                    .replaceAll("\\[.*", "")
                    .replaceAll(",", " ")
                    .replaceAll("  ", " ");
        } else this.playlistNameShort = null; // playlistName
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
        List<String> favorites;
        try {
            Response response = Requests.postToLmsForResponse(RequestParameters.favorites("", 100).toString());
            favorites = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        } catch (Exception e) {
            favorites = new ArrayList<>();
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
        Utils.sleep(1000);
        if (path.contains("spotify"))
            this.lastPath = path; // TODO при сохранении последнего пути спотифай не затирается
        return this;
    }

    public Player playChannel(Integer channel) {
        if (channel == null) return this;
        this.currentChannel = channel; // playChannel
        log.info("CHANNEL: " + channel + " PLAYER: " + this.name);
        Requests.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        Utils.sleep(1000);
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        Requests.postToLmsForStatus(RequestParameters.play(this.name, config.silence).toString());
        return this;
    }

    public Player ctrlPrevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
//        this.shuffleOff();
        this.repeatOff();
        int tracks = this.playerStatus.result.playlist_tracks;
        if (tracks > 1)
//        if (!this.tracks().equals("1"))
            Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public Player ctrlNextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
//        this.shuffleOff();
        this.repeatOff();
        if (this.playerStatus.result.playlist_tracks > 1)
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
        log.info("SYNC " + this.name + " TO " + toPlayerName + " SYNC=true");
        Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        this.sync = true;
        return this;
    }

    public Player syncToPlaying() {
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, false);
        if (playingPlayer == null) {
            log.info("NO PLAYING PLAYER");
        } else {
            this.syncTo(playingPlayer.name);
            this.sync = true;
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
//    selectRoomWithSpeaker - голосом выбрать комнату с колонкой и музыка заиграет
//    selectPlayerInRoom - голосом выбрать колонку в комнате и музыка заиграет
//    toggleMusic - для Таскера turnOnMusic/turnOffMusic
//    switchToHere - голосом или таскером переключи музыку сюда - эту включить, подключиться, остальные выключить
    public Player turnOnMusic(String volume) {

        log.info("\nTURN ON MUSIC PLAYER: " + this.name);
// узнать есть ли играющие плееры
        String playingPlayerName = null;
//        найти играющие исключая отделенные
        this.playingPlayersNamesNotInCurrentGroup(true); // turnOnMusic
        if (lmsPlayers.playingPlayersNames != null && lmsPlayers.playingPlayersNames.size() > 0)
            playingPlayerName = lmsPlayers.playingPlayersNames.get(0);

// 1) если уже играет и нет других играющих - ничего не делать
        if (this.mode.equals("play")) { // turnOnMusic
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
            this.syncTo(lmsPlayers.playingPlayersNamesNotInCurrentGrop.get(0)); // turnOnMusic
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
        log.info("\nTURN OFF PLAYER: " + this.name);
        this.unsync() // отключить от группы
                .pause(); // поставить на паузу
        return this;
    }

    // включить или выключить музыку. кнопка Play/Pause на пульте или виджет таскер
    public Player toggleMusic() {
        log.info("PLAYER: " + this.name + " TOGGLE MUSIC");
        if (this.playing) { // toggleMusic
            Yandex.sendDeviceState(this.deviceId, "on_off", "on", "false", null);
            this.turnOffMusic();
        } else {
            Yandex.sendDeviceState(this.deviceId, "on_off", "on", "true", null);
            this.turnOnMusic(null); // toggleMusic TASKER
        }
        return this;
    }

    public Player volumeSet(String value) {
//        Integer currentVolume = Integer.valueOf(this.volumeGet());
        Integer currentVolume = Integer.valueOf(this.volume);
        Integer newVolume = 0;
        if (value.contains("-")) newVolume = currentVolume - Integer.parseInt(value.replace("-", ""));
        if (value.contains("+")) newVolume = currentVolume + Integer.parseInt(value.replace("+", ""));
        if (!value.contains("+") && !value.contains("-")) newVolume = Integer.valueOf(value);
// ограничение максимальной громкости
        if (newVolume > this.volume_high) {
//            log.info("VOLUME LIMITED BY " + this.volume_high);
            newVolume = this.volume_high;
        }
// не понижать громкость меньше 1
        if (newVolume < 1) {
//            log.info("VOLUME MINIMUM 1");
            newVolume = 1;
        }
        value = String.valueOf(newVolume);
        String status = null;
        if (currentVolume != newVolume) // если громкость не меняется не делать запрос
            status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name + " STATUS: " + status);
            return this;
        } else this.volume = value;
        log.info("VOLUME CURRENT: " + currentVolume + " NEW VALUE: " + value + " LIMIT: " + this.volume_high + " SET VOLUME: " + this.volume + " STATUS: " + status);
        Yandex.sendDeviceState(this.deviceId, "range", "volume", String.valueOf(this.volume), null);
        return this;
    }

    public String volumeRelativeOrAbsoluteGetValue(String value, Boolean relative) {
        if (value == null) return null;
// из навыка может прийти абсолютное или относительное значение, к относительному надо добавлять + для lms
        if (relative != null && relative.equals(true) && !value.contains("-")) value = "+" + value;
        log.info("PLAYER: " + this.name + " VOLUME: " + value + " RELATIVE: " + relative);
        return value;
    }

    public String volumeRelativeOrAbsolute(String value, Boolean relative) {
// из навыка может прийти абсолютное или относительное значение, к относительному надо добавлять + для lms
        log.info("PLAYER: " + this.name + " VOLUME: " + value + " RELATIVE: " + relative);
        if (relative != null && relative.equals(true)) {
            if (value.contains("-")) this.volumeSet(value);
            else this.volumeSet("+" + value);
        }
        if (relative != null && relative.equals(false)) this.volumeSet(value);
        return this.name + "-" + this.volume;
    }

    public String playChannelRelativeOrAbsolute(String value, Boolean relative) {
        if (value == null) return "ERROR CHANNEL NULL";
        int channel = Integer.parseInt(value);
        int delta = Integer.parseInt(value);

        if (relative != null && relative.equals(true)) {
            channel = 1;
// определить канал сейчас попробовать найти его номер в избранном
            this.currentChannelIndexInFavorites(); // playChannelRelativeOrAbsolute
            if (lastChannel != 0) channel = lastChannel + delta; // playChannelRelativeOrAbsolute
            if (currentChannel != 0) channel = currentChannel + delta;
            log.info("RELATIVE: " + relative + " CURRENT CHANNEL: " + currentChannel + " SET CHANNEL: " + channel + " PLAYER: " + this.name);
        } else
            log.info("RELATIVE: " + relative + " NEW CHANNEL: " + value + " SET CHANNEL: " + channel + " PLAYER: " + this.name);
// включить канал
        this.playChannel(Integer.valueOf(channel)); // playChannelRelativeOrAbsolute
        lmsPlayers.lastChannel = channel;
        return "PLAY CHANNEL " + channel;
    }

    public Integer currentChannelIndexInFavorites() {
        String currentTitle = this.requestPlaylistName(); // currentChannelIndexInFavorites
        currentChannel = 0; // currentChannelIndexInFavorites
        List<String> favList = this.favorites(); // currentChannelIndexInFavorites
        if (favList.contains(currentTitle)) {
            currentChannel = favList.indexOf(currentTitle) + 1; // currentChannelIndexInFavorites
        }
        return currentChannel;
    }


    // выбор предыдущего канала через определение что сейчас играет или от последнего сохраненного канала
    public Player ctrlPrevChannel() {
        ifExpiredAndNotPlayingUnsyncWakeSet(null); // ctrlPrevChannel
        playChannelRelativeOrAbsolute("-1", true);
        return this;
    }

    // выбор предыдущего канала через определение что сейчас играет или от последнего сохраненного канала
    public Player ctrlNextChannel() {
        ifExpiredAndNotPlayingUnsyncWakeSet(null); // ctrlPrevChannel
        playChannelRelativeOrAbsolute("1", true);
        return this;
    }

    public Player ctrlNextChannelOrTrack() {
        log.info("PLAYER: " + this.name + " NEXT");
        if (this.playerStatus.result.playlist_tracks < 2) this.ctrlNextChannel(); // ctrlNextChannelOrTrack
        else this.ctrlNextTrack();
        return this;
    }

    public Player ctrlPrevChannelOrTrack() {
        log.info("PLAYER: " + this.name + " PREV");
//        int tracks = Integer.parseInt(this.tracks());
//        int tracks = Integer.parseInt(String.valueOf(this.playerStatus.result.playlist_tracks));
//        log.info("TRACKS IN PLAYLIST: " + tracks);
        if (this.playerStatus.result.playlist_tracks < 2) this.ctrlPrevChannel();
        else this.ctrlPrevTrack();
        return this;
    }

    public String favoritesAdd() {
        log.info("START ADD ON " + this.name);
//        this.status(); // favoritesAdd
        String playerName = this.name;
        String url = this.path();
        String title = "this.title"; // favoritesAdd
        log.info("NOW PATH: " + url);
        title = this.requestTitle();
//        if (url.contains("spotify")) {
//            url = Spotify.lastPath;
//
////            title = Spotify.lastTitle;
//            log.info("SPOTIFY LAST: " + url);
//        }
        int size = this.favorites().size() + 1;
        log.info("FAVORITES BEFORE SIZE: " + size);
        title = size + " " + title;
        favoritesAdd(url, title);
        log.info("FAVORITES ADD");
        return title;
    }

    public Player syncToAlt(String toPlayerName) {
// альтернативная синхронизация если глючит синхронизация di.fm
        String path = lmsPlayers.playerByCorrectName(toPlayerName).path();
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

    public Player ifExpiredAndNotPlayingUnsyncWakeSet(String volume) {
// если не играет и время просрочено - unsync, wake, set volume
        if (!this.playing && this.checkLastPlayTimeExpired()) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET, VOLUME " + volume);
            this.unsync().wakeAndSet(volume);
            return this;
        } else
// если играет и время не просрочено - set volume если пришла громкость
        {
            if (volume != null) {
                log.info("PLAYER " + this.name + " PLAY - SKIP WAKE, SET VOLUME BY PROVIDER: " + volume);
                this.volumeSet(volume);
            } else log.info("PLAYER " + this.name + " PLAY - SKIP WAKE, SKIP SET VOLUME BY PROVIDER");
        }
        return this;
    }

    public Player wakeAndSet(String volume) {
        log.info("\nWAKE START PLAYER: " + this.name + " WAIT: " + this.delay + " VOLUME: " + volume);
// если пришла громкость от провайдера то использовать ее вместо громкости по пресету
        if (volume == null) volume = String.valueOf(this.valueVolumeByTime());
        else log.info("VOLUME SET BY PROVIDER: " + volume + " " + this.name);
        this.volumeSet(volume);
        this.playSilence();
        Requests.postToLmsForStatus(RequestParameters.volume(this.name, "+1").toString());
        this.waitForWakeSeconds();
        Requests.postToLmsForStatus(RequestParameters.volume(this.name, "-1").toString());
        this.volumeSet(volume);
        this.pause();
        log.info("\nWAKE FINISH");
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
        log.info("THIS PATH: " + thisPath);
        log.info("THIS LAST PATH: " + thisLastPath);
        log.info("COMMON LAST PATH: " + commonLastPath);
        log.info("THIS OR COMMON SWITCH: " + lmsPlayers.lastThis);

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
// если ничего не подошло - играть избранное 1
        log.info("PLAY CHANNEL 1");
        this.playChannel(1); // playLast
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
//    public Player saveLastTimePathAutoremoteRequest() {
////        SwitchQueryCommand после каждой команды 9 раз
////        VoiceActions после каждой команды 5 раз
//
////        сохранить последнее время
////        saveLastTimePath(); // saveLastTimePathAutoremoteRequest
////        записать в файл lmsPlayers.json
////        запрос обновления виджетов
////        lmsPlayers.autoremoteRequest(); // saveLastTimePathAutoremoteRequest
//        return this;
//    }

    public Player saveLastTimePath() {
// сохранить последнее время
        if (this.playing) this.lastPlayTime = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
// охранить последний путь
        String path = this.path();
        if (this.lastPath != null && this.lastPath.contains("spotify") && path.contains("spotify")) {
            log.info("PATH IS SPOTIFY - SKIP SAVE LAST PATH : " + path);
            return this;
        }
        if (path != null && !path.equals(config.silence)) {
            this.lastPath = path;
            lmsPlayers.lastPath = this.lastPath;
        }
        log.info(this.name + " SAVE  LAST TIME: " + this.lastPlayTime + " SAVE LAST PATH: " + this.lastPath);
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
        log.info("SEPARATE ON");
        log.info("THIS SEPARATE: " + this.separate);
//        если уже отделен - ничего не делать // TODO если не играет то разбудить,включить последнее
        if (this.separate) {
            log.info("ALREADY SEPARATE ON ");
            if (!this.playing) this.ifExpiredAndNotPlayingUnsyncWakeSet(null).playLast();
            return this;
        }

//        если был не отделен

//        если этот играл - то после отсоединения снова включить
        String mode = this.mode();
        this
                .separateFlagTrue() // включить флаг отделен
                .unsync(); // отключить от группы
//        разбудить, включить последнее
        this.ifExpiredAndNotPlayingUnsyncWakeSet(null).playLast();
//        если этот играл - то включить
//        if (mode.equals("play")) this.ifExpiredAndNotPlayingUnsyncWakeSet(null).playLast();
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
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, true); // separateOff
        if (playingPlayer != null) this.syncTo(playingPlayer.name);
        return this;
    }

    // Алиса включи вместе - все сброисть флаг отключен, все играющие подключить к этому
// если плеер не играл - подключить к играющему
// если плеер не играл и нет играющего - включить последнее игравшее
// если плеер играл - подключить к нему остальные играющие
    public Player separateOffAll() {
        log.info("\nSEPARATE OFF ALL");
// найти играющий плеер (внутри обновление плееров ЛМС)
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, false); // separateOffAll
//        String playingPlayerName = null;
//        if (playingPlayer != null) playingPlayerName = playingPlayer.name;
// сбросить для каждого плеера флаг отделен
        lmsPlayers.players.stream().forEach(p -> p.separateFlagFalse());
// разбудить плеер если нужно
        log.info("\n" + this.name + " TRY WAKE");
        this.ifExpiredAndNotPlayingUnsyncWakeSet(null);
// если плеер не играет и есть играющий - подключить к играющему
        if (!this.playing && playingPlayer != null) {
            log.info("\n" + this.name + " NOT PLAY, SYNC TO PLAYING " + playingPlayer.name);
            this.syncTo(playingPlayer.name);
        }// separateOffAll
// если плеер не играет и нет играющего - включить последнее игравшее
        if (!this.playing && playingPlayer == null) {
            log.info("\n" + this.name + " NOT PLAY, NO PLAYING - PLAY LAST");
            this.playLast();
        } // separateOffAll
// подключить к нему остальные играющие
        log.info("\n SYNC OTHER PLAYING TO " + this.name);

        lmsPlayers.players.stream()
                .filter(p -> !p.name.equals(this.name)) // выбрать все кроме этого
                .filter(p -> !p.name.equals(playingPlayer)) // выбрать все кроме этого
                .filter(p -> p.playing)  // separateOffAll// выбрать играющие
                .forEach(p -> CompletableFuture.runAsync(() -> p
                        .unsync() // отключить
                        .syncTo(this.name) // подключить к этому
                ));
        return this;
    }

    // для востановления после отделения и для спотифай
    public Player syncAllOtherPlayingToThis() {
        log.info("SYNC OTHER PLAYING TO THIS " + this.name);
        if (this.separate) {
            log.info("PLAYER IS SEPARETE. STOP SYNC");
            return this;
        }
        log.info("SEARCH PLAYING PLAYERS...");
        List<Player> playingPlayers = lmsPlayers.playingPlayers(this.name, true);
        if (playingPlayers == null) return this;
        log.info("UNSYNC ALL PLAYING PLAYERS...");
// все играющие плееры поочереди отключить от их группы
        playingPlayers.forEach(p -> p.unsync());
        log.info("SYNC ALL NOW PLAYING PLAYERS");
// все играющие плееры поочереди подключить в новую группу
        playingPlayers.forEach(p -> p.syncTo(this.name));
        return this;
    }

    public boolean checkLastPlayTimeExpired() {
// 10 минут до сброса громкости на значаение по пресету когда не играет
        long delayExpire = lmsPlayers.delayExpire;
        if (this.lastPlayTime == null) return true;
        LocalTime playerTime = LocalTime.parse(this.lastPlayTime).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(MINUTES);
        long diff = playerTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        log.info("LAST PLAY TIME: " + this.lastPlayTime + " NOW: " + nowTime + " DELAY MINUTES: " + delayExpire + " DIFF: " + diff + " EXPIRED: " + expired);
        return expired;
    }

    public Player switchToHereAsync(boolean spotifyPlaying) {
//      Spotify.currentlyPlaying(); выполняется в VoiceActions.syncSwitchToHere
        CompletableFuture.runAsync(() -> {

            log.info("Spotify check if playing");
            if (spotifyPlaying) {
                log.info("Spotify is playing. Transferring to player: {}", this.name);
                Spotify.transfer(this);
            } else {
                log.info("Spotify is not playing. Syncing to playing player and stopping others.");
                this.syncToPlaying();
                this.stopAllOther();
                if (!this.mode().equals("play")) {
                    log.info("No player is playing. Turning on music for player: {}", this.name);
                    this.turnOnMusic(null);
                }
            }
        }).thenRunAsync(() -> lmsPlayers.afterAll());
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

//    @Override
//    public String toString() {
//        return ""
//                + "CON:" + this.connected
//                + " SEP:" + this.separate
//                + " SYNC:" + this.sync
//                + " VOL:" + this.volume
//                + " MODE:" + this.mode
//                + " PLAY:" + this.playing
//                + " TIME:" + this.lastPlayTime
//                + " NAME:" + this.name
//                + " ROOM:" + this.room;
//    }

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
                lastPlayTime
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

    public String forTaskerPlaylist(String lines) {
// получить плейлист плеера из result.playlist_loop.stream() должен быть выполнен status
        List<String> list = this.playlist;
        if (list.isEmpty()) return null;
        if (list.size() == 1) return null;
//        {
//            LmsPlayers.nowPlaying = this.playlistNameShort;
//            log.info("PLAY LIST: " + LmsPlayers.nowPlaying);
//            return LmsPlayers.nowPlaying;
//        }

// удалить из названий символы , потому что в таскере , разделитель строк
        list.replaceAll(t -> t.replaceAll(",", " "));
        Integer index = Integer.parseInt(this.playlistCurrentIndex);
        log.info("PLAYLIST INDEX: " + index);
// Заменяем элемент по конкретному индексу
        list.set(index, ">" + list.get(index));
// нумерация плейлиста
        for (int i = 0; i < list.size(); i++) list.set(i, (i + 1) + ". " + list.get(i));
// показывать только часть плейлиста вокруг играющего
        list = Utils.linesFromList(list, index, Integer.parseInt(lines));
        String result = String.join(", ", list);
        log.info("PLAYLIST: " + list);
        return result;
    }


    public void replay() {
        log.info("CHECK PLAY");
        // TODO проверить если не играет включить канал 1
//        Utils.sleep(3000);
//        String mode = this.mode();
//        if (!mode.equals("play")) {
//            log.info("CHECK PLAY FAILED. PLAY CHANNEL 1");
//            this.playChannel(1); // replay
//        }
    }


}