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
    //    public String nameInQuery;
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
    public boolean separate = false;
    public boolean connected = false;
    public String title;

    //    public String remoteMetaTitle; // определяется  playerStatus.result.remoteMeta.title
    public Integer currentChannelPlayer = 0;
    public String playlistName;
    public String playlistNameShort;
    public String playerLastSpotify;

    public String lastPathPlayer;
    public String lastPlayTimePlayer;
    public int lastChannelPlayer = 0; // для переключения канала в next и prev channel

    public String capVolume;
    public String capChannel;
    public String capOn;

    public Integer delay;
    public PlayerStatus playerStatus;
    public boolean sync;

    public List<String> playlist = new ArrayList<>();
    public String playlistCurrentIndex = null;
    private String requestStatus;


    public Player(String name) {
        this.name = name;
        this.mac = null;
        this.title = "unknown";
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

    public void resetPlayerStatus() {
        log.info("RESET STATUS " + name);
        connected = false;
        mode = "offline";
        playing = false;
        volume = "-";
        sync = false;
        title = "offline";
        playlistNameShort = "offline";
        playlistName = "offline";
        playlistCurrentIndex = null;
        playlist = new ArrayList<>();
        separate = false;
        playerStatus = null;
        currentChannelPlayer = null;
        lastPathPlayer = null;
        lastPlayTimePlayer = null;
        lastChannelPlayer = 0;
    }

    public PlayerStatus.Result status() {
        log.debug("REQUEST STATUS " + name);
        String json = Requests.postToLmsForJsonBody(RequestParameters.status(name, 200).toString());

        playerStatus = null;
        if (json == null) {
            resetPlayerStatus();
            return null;
        }
        // TODO использовать @SerializedName("mixer volume")
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3"); // для замены полей типа "mixer volume" на "mixer_volume"
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) {
            log.info("ERROR PLAYER STATUS JSON INVALID " + name);
            resetPlayerStatus();
            return null;
        }
        PlayerStatus.Result result = playerStatus.result;

        // Playlist используется только для Tasker виджет плейлист
//        playlistCurrentIndex = result.playlist_cur_index;
//        if (result.playlist_loop != null && !result.playlist_loop.isEmpty()) {
//            playlist = result.playlist_loop.stream()
//                    .filter(Objects::nonNull)
//                    .map(p -> p.title)          // null-элементы остаются как есть (сохранение исходного поведения)
//                    .collect(Collectors.toList());
//        } else {
//            playlist = new ArrayList<>();
//        }
        // Connected
        connected = (result.player_connected == 1);
        // Volume
        volume = String.valueOf(result.mixer_volume);
        // Mode playing
        playing = "play".equals(result.mode);
        mode = playing ? "play" : "stop";
        // Sync
        sync = (result.sync_slaves != null || result.sync_master != null);
        if (sync) {
            separate = false;      // separate сбрасывается только при sync = true (как в исходном коде)
        }
//        currentChannelPlayer = null;
//        lastPathPlayer = null;
//        lastPlayTimePlayer = null;
        log.info(this);
        return result;
    }

    public String title() {
        String title = null;
        if (title == null) title = this.requestPlaylistName();
        if (title == null) title = this.artistName();
        if (title == null) title = this.albumName();
        if (title == null) title = this.trackname();
        if (title == null) title = "unknown";
        log.info("TITLE FULL: " + title);
        title = titleCrop(title);
        log.info("TITLE CROP: " + title);
        this.title = title;
        return title;
    }

    public String titleCrop(String title) {
        if (title == null) return "";
        title = title
                .replaceAll(":.*", "")          // удалить всё после двоеточия
                .replaceAll(" - .*", "")         // удалить всё после " - "
                .replaceAll("\\(.*", "")         // удалить всё после (
                .replaceAll("\\[.*", "")         // удалить всё после [
                .replaceAll(",", " ")             // заменить запятые пробелами
                .replaceAll("\\s+", " ")          // схлопнуть любые пробелы в один
                .trim();                          // убрать пробелы в начале и конце
        if (title.length() > 30) title = title.substring(0, 30);
        return title;
    }


    // ------------- получение информации о том что играет

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
//        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String requestPlaylistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) return null;
        return response.result._name;
    }

    public String albumName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.albumname(this.name).toString());
        if (response == null) return null;
        return response.result._album;
    }

    public String trackname() {
        Response response = Requests.postToLmsForResponse(RequestParameters.trackname(this.name).toString());
        if (response == null) return null;
        return response.result._title;
    }

    public String artistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) return null;
        return response.result._artist;

    }

    // -------------------------------


// LMS получение данных ------------------------------------------------------------------------------------------------

    public static String count() {
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


    // ------------------------


    public String volumeGet() {
        Response response = Requests.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response != null && response.result != null) {
            log.info("PLAYER: " + this.name + " VOLUME: " + response.result._volume);
            return response.result._volume;
        }
        return "0";
    }

    public List<String> favorites() {
        Response response = Requests.postToLmsForResponse(RequestParameters.favorites("", "0").toString());
        log.debug("FAV  " + response);
        if (response != null && response.result != null) {
            return response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

//    public void favoritesAdd(String url, String title) {
//        Requests.postToLmsForResponse(RequestParameters.favoritesAdd(this.name, url, title).toString());
//    }


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
            this.lastPathPlayer = path; // TODO при сохранении последнего пути спотифай не затирается
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
//        this.status(); // для получения плейлиста и количества треков в нем
//        if (this.playerStatus.result.playlist_tracks > 1) //TODO это надо? будет ошибка если трек один?
        Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public Player ctrlNextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
//        this.shuffleOff();
        this.repeatOff();
//        this.status(); // для получения плейлиста и количества треков в нем
//        if (this.playerStatus.result.playlist_tracks > 1) //TODO это надо? будет ошибка если трек один?
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
        if (this.name.equals(toPlayerName)) return this;
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
        Map<String, List<String>> pp = this.playingPlayersNameGroups(false);
//        List<String> playersNamesInCurrentGroup = pp.get("inGroup");
        List<String> playingPlayersNamesNotInCurrentGrop = pp.get("notInGroup");

        List<String> playingPlayersNames = lmsPlayers.playingPlayersNames(this.name, true);

        if (playingPlayersNames != null && playingPlayersNames.size() > 0)
            playingPlayerName = playingPlayersNames.get(0);

// 1) если уже играет и нет других играющих - ничего не делать
        if (this.mode.equals("play")) { // turnOnMusic
//            if (ProviderAction.volumeByProvider != null) {
//                log.info("SET VILUME BY PROVIDER");
//                this.volumeSet(ProviderAction.volumeByProvider);
//            }
            if (volume != null) this.volume(volume);
            log.info("PLAYING NOT IN GROUP: " + playingPlayersNamesNotInCurrentGrop);
            if (playingPlayersNamesNotInCurrentGrop == null ||
                    playingPlayersNamesNotInCurrentGrop.size() == 0) {
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
                playingPlayersNamesNotInCurrentGrop != null &&
                playingPlayersNamesNotInCurrentGrop.size() > 0) {
            log.info("SYNC TO PLAYING " + playingPlayerName);
            this.syncTo(playingPlayersNamesNotInCurrentGrop.get(0)); // turnOnMusic
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
        log.info("TURN OFF PLAYER: " + this.name);
        this.unsync() // отключить от группы
                .pause(); // поставить на паузу
        return this;
    }

    // включить или выключить музыку. кнопка Play/Pause на пульте или виджет таскер
    public Player toggleMusic() {
        log.info("PLAYER: " + this.name + " TOGGLE MUSIC");
        if (this.playing) { // toggleMusic
//            Yandex.sendDeviceState(this.deviceId, "on_off", "on", "false", null);
            // TODO
            this.turnOffMusic();
        } else {
//            Yandex.sendDeviceState(this.deviceId, "on_off", "on", "true", null);
            // TODO
            this.turnOnMusic(null); // toggleMusic TASKER
        }
        return this;
    }

    public Player volume(String value) {
        this.requestStatus = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        return this;
    }

    public Player volumeSetLimited(String value) {
        Integer currentVolume = Integer.valueOf(this.volumeGet());
//        Integer currentVolume = Integer.valueOf(this.volume);
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
        if (channel.contains("-") || channel.contains("+")) {
            log.info("CHANNEL RELATIVE " + channel);
            id = channelGetRelative(channel);
            log.info("GET CHANNEL RELATIVE " + channel);
        } else {
            log.info("CHANNEL ABSOLUTE " + channel);
            id = Integer.parseInt(channel) - 1; // id начинается с 0
        }
        log.info("CHANNEL ID " + id);
        Requests.postToLmsForStatus(RequestParameters.playFavoritesId(this.name, id).toString());
        return this;
    }

    public Integer channelGetRelative(String channel) {
        Integer delta = 0;
        if (channel.contains("-")) delta = Integer.valueOf(channel) * (-1);
        if (channel.contains("+")) delta = Integer.valueOf(channel);
        log.info("DELTA: " + delta);
        Integer currentChannel = this.currentChannelIndexInFavorites();
        log.info("CURRENT " + currentChannel);
        if (currentChannel == null || currentChannel == 0) currentChannel = this.lastChannelPlayer;
        if (currentChannel == null || currentChannel == 0) currentChannel = lmsPlayers.lastChannelCommon;
        if (currentChannel == null || currentChannel == 0) return 1;
        return currentChannel + delta;
    }

    public Integer currentChannelIndexInFavorites() {
        String currentTitle = this.requestPlaylistName();
        Integer currentChannelIndex = 1;
        List<String> favList = this.favorites();
        if (favList.contains(currentTitle)) currentChannelIndex = favList.indexOf(currentTitle);
        return currentChannelIndex;
    }

    public Player ctrlPrevChannel() {
        log.info("PLAYER: " + this.name + " PREV CHANNEL");
        ifExpiredAndNotPlayingUnsyncWakeSet(null); // ctrlPrevChannel
        playChannel("-1");
        return this;
    }

    public Player ctrlNextChannel() {
        log.info("PLAYER: " + this.name + " NEXT CHANNEL");
        ifExpiredAndNotPlayingUnsyncWakeSet(null); // ctrlPrevChannel
        playChannel("+1");
        return this;
    }

    public Player ctrlNextChannelOrTrack() {
        this.status();
        log.info("PLAYLIST TRACKS: " + this.playerStatus.result.playlist_tracks);
        if (this.playerStatus.result.playlist_tracks < 2) this.ctrlNextChannel();
        else this.ctrlNextTrack();
        return this;
    }

    public Player ctrlPrevChannelOrTrack() {
        this.status();
        log.info("PLAYLIST TRACKS: " + this.playerStatus.result.playlist_tracks);
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

    public Player ifExpiredAndNotPlayingUnsyncWakeSet(String volume) {
// если не играет и время просрочено - unsync, wake, set volume
        if (!this.playing && this.checkLastPlayTimeExpired()
        ) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET, VOLUME " + volume);
            this.unsync().wakeAndSet(volume);
            return this;
        } else
// если играет и время не просрочено - set volume если пришла громкость
        {
            if (volume != null) {
                log.info("PLAYER " + this.name + " PLAY - SKIP WAKE, SET VOLUME BY PROVIDER: " + volume);
                this.volumeSetLimited(volume);
            } else log.info("PLAYER " + this.name + " PLAY - SKIP WAKE, SKIP SET VOLUME BY PROVIDER");
        }
        return this;
    }

    private Player wakeAndSet(String volume) {
        log.info("WAKE START PLAYER: " + this.name + " WAIT: " + this.delay + " VOLUME: " + volume);
        if (volume == null) volume = String.valueOf(this.valueVolumeByTime());
        if (delay == null) delay = 10;
        for (int i = 0; i < delay; i++) {
            this.volume(volume);
            this.waitSeconds(1);
        }
        this.volume("+1");
        this.volume("-1");
        this.pause();
        log.info("WAKE FINISH");
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
        String thisLastPath = this.lastPathPlayer;
        String commonLastPath = lmsPlayers.lastPathCommon;
        log.info("THIS PATH: " + thisPath);
        log.info("THIS LAST PATH: " + thisLastPath);
        log.info("COMMON LAST PATH: " + commonLastPath);
        log.info("THIS OR COMMON SWITCH: " + lmsPlayers.lastThis);

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
        log.info("WAIT " + delay + " . . . . .");
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
        if (this.playing) this.lastPlayTimePlayer = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
// охранить последний путь
        String path = this.path();
        if (path != null && !path.equals(config.silence)) {
            this.lastPathPlayer = path;
            lmsPlayers.lastPathCommon = this.lastPathPlayer;
        }
        log.info(this.name + " LAST TIME: " + this.lastPlayTimePlayer + " LAST PATH: " + this.lastPathPlayer);
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
        if (this.lastPlayTimePlayer == null) return true;
        LocalTime playerTime = LocalTime.parse(this.lastPlayTimePlayer).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(MINUTES);
        long diff = playerTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        log.info("LAST PLAY TIME: " + this.lastPlayTimePlayer + " NOW: " + nowTime + " DELAY MINUTES: " + delayExpire + " DIFF: " + diff + " EXPIRED: " + expired);
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