package squeezealicetest.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Requests;
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
import static squeezealicetest.utils.MainTest.lmsPlayersTest;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTest {

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

    public PlayerTest(String name, String mac) {
        this.name = name;
        this.mac = mac;
        this.title = "непонятно";
        this.nameInQuery = this.name
                .replace(" ", "")
                .replace("_", "")
                .toLowerCase();
        this.volume_step = 5;
        this.volume_low = 10;
        this.volume_high = 25;
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
        log.info("COUNT START");
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.count().toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("COUNT PLAYERS IN LMS: " + response.result._count);
        return response.result._count;
    }

    public static String name(String index) {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.name(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("NAME: " + response.result._name);
        return response.result._name;
    }

    public static String id(String index) {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.id(index).toString());
        if (response == null) {
            log.info("REQUEST ERROR");
            return null;
        }
        log.info("ID: " + response.result._id);
        return response.result._id;
    }

    public String mode() {
        this.playing = false;
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "stop";
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode);
        return response.result._mode;
    }

    public String path() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "";
        }
        log.info("PLAYER: " + this.name + " PATH: " + response.result._path);
        return response.result._path;
    }

    public String tracks() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.tracks(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " TRACKS: " + response.result._tracks);
        return response.result._tracks;
    }

    public String playlistName() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " PLAYLISTNAME: " + response.result._name);
        return response.result._name;
    }

    public String artistName() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return null;
        }
        log.info("PLAYER: " + this.name + " ARTISTNAME: " + response.result._artist);
        return response.result._artist;
    }

    public String volumeGet() {
        Response response = RequestsTest.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return "0";
        }
        log.info("PLAYER: " + this.name + " GET VOLUME: " + response.result._volume);
        return response.result._volume;
    }

    public List<String> favorites() {
        log.info("FAVORITES START " + this.name);
        String playerName = this.name;
        List<String> playlist = null;
        try {
            Response response = RequestsTest.postToLmsForResponse(RequestParameters.favorites("", 10).toString());
            playlist = response.result.loop_loop.stream().map(loopLoop -> loopLoop.name).collect(Collectors.toList());
        } catch (Exception e) {
            log.info("ERROR " + e);
        }

        log.info("FAVORITES: " + playlist);
        return playlist;
    }

    public void favoritesAdd(String url, String title) {
        RequestsTest.postToLmsForResponse(RequestParameters.favoritesAdd(this.name, url, title).toString());
    }


// LMS короткие действия --------------------------------------------------------------------------------------------

    public PlayerTest play() {
        log.info("PLAYER: " + this.name + " PLAY");
        RequestsTest.postToLmsForStatus(RequestParameters.play(this.name).toString());
        return this;
    }

    public PlayerTest pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        RequestsTest.postToLmsForStatus(RequestParameters.pause(this.name).toString());
        return this;
    }

    public PlayerTest togglePlayPause() {
        log.info("PLAYER: " + this.name + " PLAY/PAUSE");
        RequestsTest.postToLmsForStatus(RequestParameters.togglePlayPause(this.name).toString());
        return this;
    }

    public PlayerTest playPath(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        RequestsTest.postToLmsForStatus(RequestParameters.play(this.name, path).toString());
        return this;
    }

    public PlayerTest playChannel(Integer channel) {
        this.currentChannel = channel;
        log.info("CHANNEL: " + channel + " PLAYER: " + this.name);
        RequestsTest.postToLmsForStatus(RequestParameters.play(this.name, channel - 1).toString());
        return this;
    }

    public PlayerTest playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        RequestsTest.postToLmsForStatus(RequestParameters.play(this.name, config.silence).toString());
        return this;
    }

    public PlayerTest ctrlPrevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        if (!this.tracks().equals("1"))
            RequestsTest.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        return this;
    }

    public PlayerTest ctrlNextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        if (!this.tracks().equals("1"))
            RequestsTest.postToLmsForStatus(RequestParameters.nexttrack(this.name).toString());
        return this;
    }

    public PlayerTest playTrackNumber(String track) {
        log.info("PLAYER: " + this.name + " TRACK NUMBER " + track);
        RequestsTest.postToLmsForStatus(RequestParameters.track(this.name, track).toString());
        return this;
    }

    public PlayerTest shuffleOn() {
        log.info("PLAYER: " + this.name + " SHUFFLE ON");
        RequestsTest.postToLmsForStatus(RequestParameters.shuffleon(this.name).toString());
        return this;
    }

    public PlayerTest shuffleOff() {
        log.info("PLAYER: " + this.name + " SHUFFLE OFF");
        RequestsTest.postToLmsForStatus(RequestParameters.shuffleoff(this.name).toString());
        return this;
    }

    public PlayerTest syncTo(String toPlayerName) {
        log.info("SYNC " + this.name + " TO " + toPlayerName);
        RequestsTest.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        return this;
    }

    public PlayerTest unsync() {
        log.info("PLAYER UNSYNC: " + this.name);
        RequestsTest.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        return this;
    }


// LMS сценарии --------------------------------------------------------------------------------------------------------


    // включить музыку
    public PlayerTest turnOnMusic(String volume) {
        log.info("TURN ON MUSIC PLAYER: " + this.name);
// узнать состояние плеера
        this.status(); // далее запускается в методе экспайред
// узнать есть ли играющие плееры
        String playingPlayerName = null;
//        найти играющие исключая отделенные
        this.playingPlayersNamesNotInCurrentGroup(true);
        if (lmsPlayersTest.playingPlayersNames != null && lmsPlayersTest.playingPlayersNames.size() > 0)
            playingPlayerName = lmsPlayersTest.playingPlayersNames.get(0);

// 1) если уже играет и нет других играющих - ничего не делать
        if (this.mode.equals("play")) {
//            if (ProviderAction.volumeByProvider != null) {
//                log.info("SET VILUME BY PROVIDER");
//                this.volumeSet(ProviderAction.volumeByProvider);
//            }
            if (volume != null) this.volumeSet(volume);
            log.info("PLAYING NOT IN GROUP: " + lmsPlayersTest.playingPlayersNamesNotInCurrentGrop);
            if (lmsPlayersTest.playingPlayersNamesNotInCurrentGrop == null ||
                    lmsPlayersTest.playingPlayersNamesNotInCurrentGrop.size() == 0) {
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
            return this;
        }
// 3) если не играет, не отделен и нет играющего - включить последнее
        if (!this.separate && playingPlayerName == null) {
            log.info("NO PLAYING. PLAY LAST");
            this.playLast();
            return this;
        }
// 4) если играет или не играет, не отделен и есть играющий не в группе - подключиться к нему в группу
        if (!this.separate &&
                lmsPlayersTest.playingPlayersNamesNotInCurrentGrop != null &&
                lmsPlayersTest.playingPlayersNamesNotInCurrentGrop.size() > 0) {
            log.info("SYNC TO PLAYING " + playingPlayerName);
            this.syncTo(lmsPlayersTest.playingPlayersNamesNotInCurrentGrop.get(0));
            return this;
        }

// сохранить последнее время и путь
// обновить виджеты таскер
// перенесено после runInstance всех устройств
// RequestsTest.autoRemoteRefresh();

// сохранение времени и пути должно выполняться для каждого плеера после выполнения действий


        log.info("WARNING. SKIPED TURN ON MUSIC");
        return this;
    }

    // выключить музыку
    public PlayerTest turnOffMusic() {
        log.info("PLAYER: " + this.name + " TURN OFF");
        this.unsync() // отключить от группы
                .pause(); // поставить на паузу
        return this;
    }

    // включить или выключить музыку. кнопка Play/Pause на пульте или виджет таскер
    public PlayerTest toggleMusic() {
        log.info("PLAYER: " + this.name + " TOGGLE MUSIC");
        this.status();
        if (this.playing) this.turnOffMusic();
        else this.turnOnMusic(null);
        return this;
    }


    public PlayerTest volumeSet(String value) {
        String status = null;
        log.info("PLAYER: " + this.name + " SET VOLUME: " + value);
        status = RequestsTest.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (status == null || !status.contains("200")) {
            log.info("REQUEST ERROR " + this.name);
            return this;
        }
        this.status(null);
        if (playerStatus.result.mixer_volume < 1) {
            status = RequestsTest.postToLmsForStatus(RequestParameters.volume(this.name, "1").toString());
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
            RequestsTest.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
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
// log.info("STATUS PLAYER " + this.name);
        if (tracks == null) tracks = 0;
        String json = RequestsTest.postToLmsForJsonBody(RequestParameters.status(this.name, tracks).toString());
//        log.info("JSON: " + json);
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

        if (playerStatus.result.playlist_loop != null) {
            this.playlist = playerStatus.result.playlist_loop.stream()
                    .map(p -> p.title)
                    .collect(Collectors.toList());
//            log.info("PLAYLIST LOOP:" + this.playlist);
        } else this.playlist = new ArrayList<>();

        if (playerStatus.result.remoteMeta != null)
            this.currentTrack = playerStatus.result.remoteMeta.title;

        this.title();
        this.volume = String.valueOf(playerStatus.result.mixer_volume);
        if (playerStatus.result.mode.equals("play")) {
            this.playing = true;
            this.mode = "play";
        } else {
            this.playing = false;
            this.mode = "stop";
        }

//        log.info("SYNCSLAVE: " + playerStatus.result.sync_slaves);
//        log.info("SYNCMASTER: " + playerStatus.result.sync_master);
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
        lmsPlayersTest.lastChannel = channel;
        log.info("PLAYER: " + this.name + " PLAY CHANNEL FINISH");
        return "PLAY CHANNEL " + channel;
    }

    // выбор предыдущего канала через определение что сейчас играет или от последнего сохраненного канала
    public PlayerTest ctrlPrevChannel() {
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
        else channel = lmsPlayersTest.lastChannel - 1;
        if (channel < 1) channel = favSize;
        log.info("PLAY PREV CHANNEL: " + channel + " LAST CHANNEL: " + lmsPlayersTest.lastChannel);
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
    public PlayerTest ctrlNextChannel() {
        log.info("LAST CHANNEL THIS: " + this.lastChannel + " COMMON: " + lmsPlayersTest.lastChannel);
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
            channel = lmsPlayersTest.lastChannel + 1;
            log.info("CHANNEL COMMON+1: " + channel);
        }
        if (channel > favSize) channel = 1;
        log.info("PLAY CHANNEL: " + channel);
        this.ifExpiredAndNotPlayingUnsyncWakeSet(null);
        this.playChannel(channel); // prevChannel
        return this;
    }

    public PlayerTest ctrlNextChannelOrTrack() {
        log.info("PLAYER: " + this.name + " NEXT");
        int tracks = Integer.parseInt(this.tracks());
        log.info("TRACKS IN PLAYLIST: " + tracks);
        if (tracks < 2) this.ctrlNextChannel();
        else this.ctrlNextTrack();
        return this;
    }

    public PlayerTest ctrlPrevChannelOrTrack() {
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
//        String path = lmsPlayersTest.playerByCorrectName(toPlayerName).path();
//        if (path.contains("di.fm") || path.contains("audioaddict")) {
//            log.info("SYNC DI.FM " + this.name + " PLAY PATH FROM " + toPlayerName);
//            this.playPath(path);
//            return this;
//        } else {
//            log.info("SYNC " + this.name + " TO " + toPlayerName);
//            RequestsTest.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
//        }
//        return this;
//    }

    public PlayerTest ifExpiredAndNotPlayingUnsyncWakeSet(String volume) {
        log.info("CHECK IF EXPIRED PLAYER: " + this.name + " SET VOLUME BY PROVIDER: " + volume);
        if (!this.status(0)) {
            log.info("ERROR STATUS NULL !!!");
            return this;
        }
        if (!this.playing && this.ifTimeExpired(null)) {
            log.info("PLAYER " + this.name + " NOT PLAY - UNSYNC, WAKE, SET");
            this.unsync().wakeAndSet(volume);
            return this;
        } else {
            log.info("SKIP WAKE " + this.name + " VOLUME BY PROVIDER: " + volume);
            if (volume != null) {
                log.info("SET VOLUME BY PROVIDER: " + volume + " PLAYER: " + this.name);
                this.volumeSet(volume);
            } else log.info("SKIP SET " + this.name + " VOLUME BY PROVIDER: " + volume);

        }
//        if (this.playing && this.ifTimeExpired(60)) {
//            log.info("PLAYER " + this.name + " PLAY TOO LONG - UNSYNC, WAKE, SET");
//            this.unsync().wakeAndSet();
//            return this;
//        }
        return this;
    }

    public PlayerTest wakeAndSet(String volume) {
        log.info("");
        log.info("WAKE START >>> PLAYER: " + this.name + " WAIT: " + this.delay + " VOLUME: " + volume);
// если пришла громкость от провайдера то использовать ее вместо громкости по пресету
        if (volume == null) {
            log.info("VOLUME SET BY PRESET " + this.name);
            this
                    .playSilence()
                    .volumeSet("+1")
                    .setVolumeByTime()
                    .waitForWakeSeconds()
                    .volumeSet("-1")
                    .setVolumeByTime()
                    .pause();
        } else {
            log.info("VOLUME SET BY PROVIDER: " + volume + " " + this.name);
            this
                    .playSilence()
                    .volumeSet("+1")
                    .volumeSet(volume)
                    .waitForWakeSeconds()
                    .volumeSet("-1")
                    .volumeSet(volume)
                    .pause();
        }
        log.info("WAKE FINISH <<<");
        log.info("");
        return this;
    }

    public PlayerTest stopAllOther() {
        lmsPlayersTest.players.stream()
                .filter(p -> !p.name.equals(this.name))
                .forEach(player -> player.unsync().pause());
        return this;
    }

    public PlayerTest playLast() {
        log.info("PLAY LAST");
        String thisPath = this.path();
        String thisLastPath = this.lastPath;
        String commonLastPath = lmsPlayersTest.lastPath;
        String lastPath;
// log.info("THIS PATH: " + thisPath);
// log.info("THIS LAST PATH: " + thisLastPath);
// log.info("TRY GLOBAL LAST PATH: " + commonLastPath);
// log.info("SILENCE PATH: " + silence);
// log.info("LAST THIS STATE: " + lmsPlayersTest.lastThis);

//        if (lmsPlayersTest.lastThis) lastPath = thisLastPath;
//        else lastPath = commonLastPath;

// если у этого плеера есть путь и он не тишина - играть
        if (thisPath != null && !thisPath.equals(config.silence) && !thisPath.equals("")) {
            log.info("PUSH PLAY BUTTON: " + thisPath);
            this.play();
            return this;
        }
// если у этого плеера нет пути играть последний сохраненый этого
        if (thisLastPath != null && !thisLastPath.equals(config.silence) && !thisLastPath.equals("") && lmsPlayersTest.lastThis) {
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


        log.info("PLAY CHANNEL 1");
        this
                .playChannel(1); // playLast
        return this;
    }

    public PlayerTest setVolumeByTime() {
        LocalTime timeNow = LocalTime.now(zoneId);
// log.info("VOLUME BY TIME: " + timeNow + " OF: " + this.schedule);
        Map.Entry<Integer, Integer> e =
                schedule.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("TIME: " + timeNow.truncatedTo(MINUTES) + " VOLUME: " + e.getValue() + " BY TIME: " + e.getKey() + " OF: " + this.schedule);
        this.volumeSet(String.valueOf(e.getValue()));
        return this;
    }

    public PlayerTest waitForWakeSeconds() {
        log.info("WAIT " + delay + " . . . . .");
        try {
            Thread.sleep(this.delay * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public PlayerTest waitFor(int msec) {
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
    public PlayerTest saveLastTimePathAutoremoteRequest() {
//        сохранить последнее время
        saveLastTimePath();
//        записать в файл lmsPlayersTest.json
//        запрос обновления виджетов
        lmsPlayersTest.autoremoteRequest();
        return this;
    }

    public PlayerTest saveLastTimePath() {
//        сохранить последнее время
        this.lastPlayTime = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
        log.info("SAVE LAST TIME: " + this.lastPlayTime);
//        сохранить последний путь
        String path = this.path();
        if (path != null && !path.equals(config.silence)) {
            this.lastPath = path;
            lmsPlayersTest.lastPath = this.lastPath;
            log.info("SAVE LAST PATH: " + this.lastPath);
        }
//        сохранить последний тайтл
        this.status();
        this.lastTitle = this.title;
        lmsPlayersTest.lastTitle = this.title;
        log.info("SAVE LAST TITLE: " + this.title);
        return this;
    }


//    public Player saveLastChannel(int channel) {
//        log.info("SAVE LAST CHANNEL: " + channel);
//        lmsPlayersTest.lastChannel = channel;
//        this.lastChannel = channel;
//        this.status();
////        this.lastTitle = this.title;
////        lmsPlayersTest.lastTitle = this.title;
//        return this;
//    }

    public void remove() {
        lmsPlayersTest.players.remove(this);
    }

    public PlayerTest separateFlagTrue() {
        this.separate = true;
        log.info("SEPARTE FLAG: " + this.name + " " + this.separate);
        return this;
    }

    public PlayerTest separateFlagFalse() {
        this.separate = false;
        log.info("SEPARTE FLAG: " + this.name + " " + this.separate);
        return this;
    }

    // Алиса включи отдельно - для плеера включить флаг отделен, отключить от группы, если играл то продолжить
    public PlayerTest separateOn() {
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
//    public PlayerTest separateOff() {
//        log.info("SEPARATE OFF");
//        log.info("THIS SEPARATE: " + this.separate);
////        если уже не отделен - ничего не делать
//        if (!this.separate) {
//            log.info("ALREADY SEPARATE OFF");
//            return this;
//        }
//        this.separateFlagFalse(); // выключить флаг отделен
////        if (playingPlayer != null) this.syncTo(playingPlayer.name);
//        String mode = this.mode();
////        если этот не играл то выход
//        if (!mode.equals("play")) return this;
////        если этот играет и есть играющий то подключиться к играющему
//        PlayerTest playingPlayer = lmsPlayersTest.playingPlayer(this.name, true);
//        if (playingPlayer != null) this.syncTo(playingPlayer.name);
//        return this;
//    }

    // Алиса включи вместе - все сброисть флаг отключен, все играющие подключить к этому
// если плеер не играл - подключить к играющему
// если плеер не играл и нет играющего - включить последнее игравшее
// если плеер играл - подключить к нему остальные играющие
    public PlayerTest separateOffAll() {
        log.info("SEPARATE OFF ALL");
// найти играющий плеер (внутри обновление плееров ЛМС)
        PlayerTest playingPlayer = lmsPlayersTest.playingPlayer(this.name, true);
// сбросить для каждого плеера флаг отделен
        lmsPlayersTest.players.stream().forEach(p -> p.separateFlagFalse());
// если плеер не играет и есть играющий - подключить к играющему
        if (!this.playing && playingPlayer != null) this.syncTo(playingPlayer.name);
// если плеер не играет и нет играющего - включить последнее игравшее
        if (!this.playing && playingPlayer == null) this.playLast();
// подключить к нему остальные играющие
        lmsPlayersTest.players.stream()
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
// log.info("PLAYER: " + this.name + " TITLE: " + this.title);
        String title = this.playerStatus.result.current_title;
// log.info("CURRENT TITLE: " + this.title);
        if ((title != null) && (title != "")) {
            if (title.contains(": ")) title = title.replaceAll(":.*", "");
            if (title.contains(" - ")) title = title.replaceAll(" - .*", "");
        }
        if ((title == null) || (title == "")) title = this.artistName();
        if (title == null) title = "непонятно";
        if (title.length() > 20) title = title.substring(0, 20);
        title = title.replaceAll(",", " ");
        title = title.replaceAll(":", " ");
        title = title.replaceAll("  ", " ");
        this.title = title;
//        log.info("TITLE: " + this.title);
    }

    // для востановления после отделения и для спотифай
    public PlayerTest syncAllOtherPlayingToThis() {
        log.info("SYNC OTHER PLAYING TO THIS START " + this.name);
//        if (!lmsPlayersTest.syncAlt) {
//            log.info("ALT SYNC OFF");
//            return this;
//        }
        if (this.separate) {
            log.info("PLAYER IS SEPARETE. STOP ALT SYNC");
            return this;
        }
        log.info("SEARCH PLAYING PLAYERS...");
        List<PlayerTest> playingPlayers = lmsPlayersTest.playingPlayers(this.name, true);
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
        long delayExpire = lmsPlayersTest.delayExpire;
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

//    public PlayerTest switchToHere() {
//        log.info("TRY TRANSFER IF SPOTIFY PLAY");
//        if (Spotify.transfer(this)) {
//            log.info("SPOTIFY TRANSFER TO " + this.name + " FINISHED OK");
//            return this;
//        }
//        if (this.mode().equals("play")) {
//            log.info("PLAYER PLAYING. STOP ALL OTHER");
//            this.stopAllOther();
//            return this;
//        }
//        log.info("PLAYER NOT PLAY. SYNC TO PLAYING OR PLAY LAST. STOP ALL OTHER");
//        this.turnOnMusic(null).stopAllOther();
//        return this;
//    }

    public List<String> playingPlayersNamesNotInCurrentGroup(Boolean exceptSeparated) {
        log.info("START");
// найти плееры которые играют кроме этого
        lmsPlayersTest.playingPlayersNames = lmsPlayersTest.playingPlayersNames(this.name, exceptSeparated);
        log.info("PLAYING PLAYERS ALL: " + lmsPlayersTest.playingPlayersNames);
// найти группы плееров
        List<List<String>> groups = lmsPlayersTest.syncgroups();
        log.info("GROUPS: " + groups);
// найти плееры которые в одной группе с этим
        if (groups != null)
            lmsPlayersTest.playersNamesInCurrentGroup = groups.stream()
                    .filter(list -> list.contains(this.name)) // Ищем подсписок с этим плеером
                    .findFirst() // Берем первый найденный подсписок
                    .map(list -> list.stream()
                            .filter(e -> !e.equals(this.name)) // Удаляем этот плеер из подсписка
                            .collect(Collectors.toList()))
                    .orElse(Collections.emptyList()); // Если ничего не найдено, вернем пустой список
        else lmsPlayersTest.playersNamesInCurrentGroup = new ArrayList<>();
        log.info("OTHER PLAYING PLAYERS NAMES: " + lmsPlayersTest.playingPlayersNames);
        log.info("PLAYERS IN CURRENT GROP: " + lmsPlayersTest.playersNamesInCurrentGroup);
//        находим плееры которые играют не в этой группе
        lmsPlayersTest.playingPlayersNamesNotInCurrentGrop = lmsPlayersTest.playingPlayersNames;
        lmsPlayersTest.playingPlayersNamesNotInCurrentGrop.removeAll(lmsPlayersTest.playersNamesInCurrentGroup);
        log.info("PLAYERS PLAYING NOT IN CURRENT GROP: " + lmsPlayersTest.playingPlayersNamesNotInCurrentGrop);
        log.info("FINISH");
        return lmsPlayersTest.playingPlayersNamesNotInCurrentGrop;
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
        if (!(o instanceof PlayerTest)) return false;
        PlayerTest player = (PlayerTest) o;
        return Objects.equals(getName(), player.getName()) && Objects.equals(getMac(), player.getMac());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getMac());
    }

    public List<String> playlistList() {
        this.status(250);
        this.tracks();
        return this.playlist;
    }

    public String forTaskerPlaylist(String value) {
        log.info("FOR PLAYLIST START >>> ----------------");
        List<String> list = playlistList();
// если в плейлисте только один трэк (радио)
        if (list.size() == 1) {
            log.info("ONE TRACK TITLE: " + this.title);
//            log.info("ONE TRACK");
            String currentTrack = this.currentTrack;
            if (currentTrack.length() > 20) currentTrack = currentTrack.substring(0, 20);
            String result = this.title + " - " + currentTrack;
            log.info("RESULT: " + result);
            return result;
        }
//        найти номер играющего трека в плейлисте

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
//        log.info("PLAYLIST 1 > : " + list);
//       показывать только часть плейлиста
//        log.info("LINES : " + value);
        list = Utils.linesFromList(list, index, Integer.parseInt(value));

//        log.info("PLAYLIST: " + list);
        String result = String.join(", ", list);
        log.info("FOR PLAYLIST FINISH <<< ----------------");
        return result;
    }


}