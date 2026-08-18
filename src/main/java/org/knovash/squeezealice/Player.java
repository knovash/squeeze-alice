package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.lms.*;
import org.knovash.squeezealice.player.ActionsAsync;
import org.knovash.squeezealice.player.ActionsSync;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.yandex.GoogleTTS;
import org.knovash.squeezealice.yandex.YandexTTS;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.knovash.squeezealice.Main.*;
import static org.knovash.squeezealice.web.Settings.KeyString.STRING_DEFAULT_CHANNEL;
import static org.knovash.squeezealice.web.Settings.KeyString.STRING_DEFAULT_LINK;
import static org.knovash.squeezealice.web.Settings.KeyToggle.*;
import static org.knovash.squeezealice.web.SettingsVoice.KeyToggle.*;
import static org.knovash.squeezealice.web.SettingsVoice.KeyValue.*;
import static org.knovash.squeezealice.yandex.YandexTTS.waitForPlaybackCompletion;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    // постоянные плеера
    public String mac;
    public String name;
    public String room;
    // переменные настройки плеера
    public Integer volume_step;
    public Integer volume_low;
    public Integer volume_high;
    public Integer delay;
    public Map<Integer, Integer> schedule;
    // длительные свойства
    public String lastPathPlayer;
    public String lastPlayTimePlayer;
    public int lastChannelPlayer = 0;
    public static int lastChannelCommon;
    public static String lastPathCommon;
    public static String savedPlaylistNameCommon;
    // свойства плеера в моменте
    public PlayerStatus playerStatus; // результат запроса статуса плеера
    public boolean playing;
    public String mode; // play stop offline
    public boolean separate;
    public boolean connected;
    public String title;
    public String volume;
    public boolean sync;
    public boolean shuffle;
    public boolean repeat;
    public String capVolume;
    public String capChannel;
    public String capOn;
    // сохранные параметры восстановления плейлиста
    public String savedPlaylistIndex;
    public double savedPlaylistTime;
    public String savedPlaylistMode;
    public String savedPlaylistName;      // единое поле для имени последнего сохранённого плейлиста
    public String savedPlaylistVolume;
    public Boolean savedPlaylistOk;
    public List<String> savedGroup;
    public String trackName = null;

    public Player(String name) {
        this.name = name;
        this.room = null;
        this.volume_step = 5;
        this.volume_low = 1;
        this.volume_high = 70;
        this.delay = 3;
        this.schedule = new HashMap<>(Map.of(
                0, 10,
                7, 15,
                9, 20,
                20, 15,
                22, 5));
    }

    // ========================================================================
    // Базовые команды управления плеером
    // ========================================================================

    public Player play() {
        log.info("PLAYER: " + this.name + " PLAY");
        Requests.postToLmsForStatus(RequestParameters.play(this.name).toString());
        this.saveLastTime();
        return this;
    }

    public Player pause() {
        log.info("PLAYER: " + this.name + " PAUSE");
        Requests.postToLmsForStatus(RequestParameters.pause(this.name).toString());
        return this;
    }

    public Player stop() {
        log.info("PLAYER: " + this.name + " STOP");
        Requests.postToLmsForStatus(RequestParameters.stop(this.name).toString());
        return this;
    }

    public Player togglePlayPause() {
        log.info("PLAYER: " + this.name + " PLAY/PAUSE");
        Requests.postToLmsForStatus(RequestParameters.togglePlayPause(this.name).toString());
        this.saveLastTime();
        return this;
    }

    public Player playPath(String path) {
        log.info("PLAYER: " + this.name + " PLAY PATH: " + path);
        Requests.postToLmsForStatus(RequestParameters.play(this.name, path).toString());
        this.lastPathPlayer = path;
        Player.lastPathCommon = path;
        this.saveLastTime();
        return this;
    }

    public Player playFile(String file) {
        log.info("PLAYER: " + this.name + " PLAY FILE: " + file);
        Requests.postToLmsForStatus(RequestParameters.playFile(this.name, file).toString());
        this.saveLastTime();
        return this;
    }

    public Player playSilence() {
        log.info("PLAYER: " + this.name + " PLAY SILENCE");
        Requests.postToLmsForStatus(RequestParameters.play(this.name, config.silence).toString());
        this.saveLastTime();
        return this;
    }

    // ========================================================================
    // Управление громкостью
    // ========================================================================

    public String volumeGet() {
        Response response = Requests.postToLmsForResponse(RequestParameters.volume(this.name).toString());
        if (response != null && response.result != null) {
            log.info("PLAYER: " + this.name + " VOLUME: " + response.result._volume);
            this.volume = response.result._volume;
            return response.result._volume;
        }
        return "0";
    }

    public Player volumeSet(String value) {
//        log.info("PLAYER: " + this.name + " VOLUME: " + value);
        Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        return this;
    }

    public Player volumeWakeSet(String value) {
//        log.info("PLAYER: " + this.name + " VOLUME: " + value);
        this.playSilence();
        this.waitMilSeconds(2000);
        Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        this.pause();
        this.playlistClear();
        return this;
    }

    public Player volumeNoLog(String value) {
        Requests.postToLmsForStatusNoLog(RequestParameters.volume(this.name, value).toString());
        return this;
    }

    public Player volumeSetLimited(String value) {
        Integer currentVolume = Integer.valueOf(this.volumeGet());
        Integer newVolume = 0;
        if (value.contains("-")) newVolume = currentVolume - Integer.parseInt(value.replace("-", ""));
        if (value.contains("+")) newVolume = currentVolume + Integer.parseInt(value.replace("+", ""));
        if (!value.contains("+") && !value.contains("-")) newVolume = Integer.valueOf(value);
        if (newVolume > this.volume_high) newVolume = this.volume_high;
        if (newVolume < 1) newVolume = 1;
        if (currentVolume.equals(newVolume)) return this;
        value = String.valueOf(newVolume);
        String status = Requests.postToLmsForStatus(RequestParameters.volume(this.name, value).toString());
        if (!(status == null || !status.contains("200"))) this.volume = value;
        log.info("VOLUME CURRENT: " + currentVolume + " NEW VALUE: " + value + " LIMIT: " + this.volume_high + " SET VOLUME: " + this.volume + " STATUS: " + status);
        return this;
    }

    public Integer valueVolumeByTime() {
        LocalTime timeNow = LocalTime.now(zoneId);
        Map.Entry<Integer, Integer> e =
                schedule.entrySet()
                        .stream()
                        .filter(entry -> LocalTime.of(entry.getKey(), 0).isBefore(timeNow))
                        .max(Comparator.comparing(Map.Entry::getKey))
                        .get();
        log.info("TIME: " + timeNow.truncatedTo(MINUTES) + " VOLUME: " + e.getValue() + " BY TIME: " + e.getKey() + " PRESETS: " + this.schedule);
        return e.getValue();
    }

    public Player volumeByTimeSet() {
        this.volumeSet(String.valueOf(this.valueVolumeByTime()));
        return this;
    }

    // ========================================================================
    // Управление плейлистами и сценариями
    // ========================================================================

    public Player playlistClear() {
        Requests.postToLmsForStatus(RequestParameters.playlistClear(this.name).toString());
        return this;
    }

    public Player savePlaylistWithCheck(String playlistName) {
        log.info(start);

        //this.savedPlaylistOk = false;
        if (playlistName == null) return null;
        // проверить если сейчас плейлист пустой - не сохранять
        Integer tracksCount = this.playlistTracksCurrentCount();
        if (tracksCount == null || tracksCount == 0) {
            log.info("PLAYLIST CURRENT BAD. SKIP SAVE");
            return null;
        } else
            //log.info("PLAYLIST CURRENT LOOP: " + this.playerStatus.result.playlist_loop);
            log.info("PLAYLIST CURRENT CHECK - OK");
        // TODO добавить проверку содержимого плейлиста
//        сохранение плейлиста
        this.savedPlaylistName = playlistName;
        this.savedPlaylistOk = true;
        Player.savedPlaylistNameCommon = playlistName;
        Requests.postToLmsForStatus(RequestParameters.playlistSave(this.name, playlistName).toString());
        log.info(finish);
        return this;
    }

    public Player savePlaylist(String playlistName) {
        log.info(start);
        if (playlistName == null) return null;
        this.savedPlaylistOk = true;
        this.savedPlaylistName = playlistName;
        Player.savedPlaylistNameCommon = playlistName;
        log.info("PLAYLIST SAVE THIS: " + this.savedPlaylistName);
        log.info("PLAYLIST SAVE COMMON: " + Player.savedPlaylistNameCommon);
        Requests.postToLmsForStatus(RequestParameters.playlistSave(this.name, playlistName).toString());
        log.info(finish);
        return this;
    }

    public Player playlistRestore(String playlistName) {
        log.info(start);
        if (playlistName == null) return null;
        String res = Requests.postToLmsForStatus(RequestParameters.playlistRestore(this.name, playlistName).toString());
        //String res = Requests.postToLmsForStatus(RequestParameters.playlistRestoreById(this.name, playlistId).toString());
        log.info("RESTORE RESULT: " + res);
        if (res == null) return null;
        log.info(finish);
        return this;
    }

    public Player playlistRename(String nameOld, String nameNew) {
        Requests.postToLmsForStatus(RequestParameters.playlistRename("", nameOld, nameNew).toString());
        return this;
    }

    public Player playlistIndexSet(String index) {
        log.info("PLAYER: " + this.name + " INDEX: " + index);
        if ("0".equals(index)) return this;
        Requests.postToLmsForStatus(RequestParameters.playlistIndexSet(this.name, index).toString());
        return this;
    }

    public Player playlistTimeSet(double time) {
        log.info("PLAYER: " + this.name + " TIME: " + time);
        Requests.postToLmsForStatus(RequestParameters.playlistTimeSet(this.name, String.valueOf(time)).toString());
        return this;
    }


    public void saveSyncScript() {
        log.info(start);
        // Определить группу, если был в группе
        List<String> currentGroup = null;
        List<List<String>> groups = lmsPlayers.syncgroups();
        if (groups != null && !groups.isEmpty()) {
            for (List<String> group : groups) {
                if (group.contains(this.name)) {
                    currentGroup = group;
                    break;
                }
            }
        }
        log.info("CURRENT GROUP: " + currentGroup);
        if (currentGroup != null && !currentGroup.isEmpty()) {
            currentGroup.remove(this.name);
        }
        // Сохранить параметры в плеер
        this.savedGroup = currentGroup;
        this.savedPlaylistOk = false;
        log.info("++++++++++++++++ SAVED SYNC ++++++++++++++++++++++++");
        log.info("SAVED GROUP: " + this.savedGroup);
        log.info("SAVED INDEX: " + this.savedPlaylistIndex);
        log.info("SAVED TIME: " + this.savedPlaylistTime);
        log.info("SAVED MODE: " + this.savedPlaylistMode);
        log.info("SAVED VOLUME: " + this.savedPlaylistVolume);
        log.info("SAVED PLAYLIST: " + this.savedPlaylistName);
        log.info("SAVED OK: " + this.savedPlaylistOk);
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++");
        log.info(finish);
    }

    public void savePlaylistScript() {
        log.info(start);
        log.info("++++++++++++++++ SAVED BEFORE ++++++++++++++++++++++++");
        log.info("SAVED GROUP: " + this.savedGroup);
        log.info("SAVED INDEX: " + this.savedPlaylistIndex);
        log.info("SAVED TIME: " + this.savedPlaylistTime);
        log.info("SAVED MODE: " + this.savedPlaylistMode);
        log.info("SAVED VOLUME: " + this.savedPlaylistVolume);
        log.info("SAVED PLAYLIST: " + this.savedPlaylistName);
        log.info("SAVED OK: " + this.savedPlaylistOk);
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++");
        // Определить группу, если был в группе
        List<String> currentGroup = null;
        List<List<String>> groups = lmsPlayers.syncgroups();
        if (groups != null && !groups.isEmpty()) {
            for (List<String> group : groups) {
                if (group.contains(this.name)) {
                    currentGroup = group;
                    break;
                }
            }
        }
        log.info("SAVE CURRENT GROUP: " + currentGroup);
        if (currentGroup != null && !currentGroup.isEmpty()) {
            currentGroup.remove(this.name);
        }
        this.savedGroup = currentGroup;

        // проверить если сейчас плейлист пустой - не сохранять
        Integer tracksCount = this.playlistTracksCurrentCount();
        if (tracksCount == null || tracksCount == 0) {
            log.info("PLAYLIST CURRENT EMPTY. SKIP SAVE");
            log.info(finish);
            return;
        }

        // Выбрать имя для сохранения плейлиста
        String currentPlaylistName = this.playlistName();
        if ("loop".equals(currentPlaylistName)) {
            log.info("PLAYLIST CURRENT LOOP. SKIP SAVE " + currentPlaylistName);
            log.info(finish);
            return;
        }
        if (currentPlaylistName != null) currentPlaylistName = titleCrop(currentPlaylistName);
        if (currentPlaylistName == null) currentPlaylistName = this.name + "_restore";
        log.info("CURRENT PLAYLIST NAME: " + currentPlaylistName);

        // Получить: индекс трека, время трека, режим, громкость
        PlayerStatus.Result result = this.statusFast();
        if (result == null) {
            log.info("PLAYLIST RESTORE NOT SAVED PLAYER OFFLINE " + this);
            log.info(finish);
            return;
        }

        // Сохранить плейлист в LMS
        Requests.postToLmsForStatus(RequestParameters.playlistSave(this.name, currentPlaylistName).toString());

        // Сохранить параметры в плеер
        Player.savedPlaylistNameCommon = currentPlaylistName;
        this.savedPlaylistName = currentPlaylistName;
        this.savedPlaylistIndex = result.playlist_cur_index;
        this.savedPlaylistTime = result.time;
        this.savedPlaylistMode = result.mode;
        this.savedPlaylistVolume = String.valueOf(result.mixer_volume);
        this.savedPlaylistOk = true;
        log.info("++++++++++++++++ SAVED ++++++++++++++++++++++++");
        log.info("CURRENT PLAYLIST: " + currentPlaylistName);
        log.info("SAVED GROUP: " + this.savedGroup);
        log.info("SAVED INDEX: " + this.savedPlaylistIndex);
        log.info("SAVED TIME: " + this.savedPlaylistTime);
        log.info("SAVED MODE: " + this.savedPlaylistMode);
        log.info("SAVED VOLUME: " + this.savedPlaylistVolume);
        log.info("SAVED PLAYLIST: " + this.savedPlaylistName);
        log.info("SAVED OK: " + this.savedPlaylistOk);
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++");
        log.info(finish);
    }

    public boolean restorePlaylistScript(Boolean restoreSync) {
        log.info(start);
        boolean result = false;
        log.info("++++++++++++++++ RESTORE PLAYLIST ++++++++++++++++++++++++");
        log.info("RESTORE SYNC: " + restoreSync);
        log.info("LOAD GROUP: " + this.savedGroup);
        log.info("LOAD INDEX: " + this.savedPlaylistIndex);
        log.info("LOAD TIME: " + this.savedPlaylistTime);
        log.info("LOAD MODE: " + this.savedPlaylistMode);
        log.info("LOAD VOLUME: " + this.savedPlaylistVolume);
        log.info("LOAD PLAYLIST THIS: " + this.savedPlaylistName);
        log.info("LOAD PLAYLIST COMMON: " + Player.savedPlaylistNameCommon);
        log.info("LOAD OK: " + this.savedPlaylistOk);
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++");


        if ("loop".equals(this.savedPlaylistName)) this.savedPlaylistName = null;
        if ("loop".equals(Player.savedPlaylistNameCommon)) Player.savedPlaylistNameCommon = null;

//       если был в группе, вернуть в группу и завершить
        if (this.savedGroup != null && !this.savedGroup.isEmpty() && restoreSync) {
            log.info("RESTORE SYNC TO: " + this.savedGroup);
            this.syncTo(this.savedGroup.get(0));
            log.info(finish);
            return false;
        }

        if (!this.savedPlaylistOk) {
            this.savedPlaylistName = null;
            log.info("SKIP RESTORE PLAYLIST savedPlaylistOk=" + this.savedPlaylistOk);
            log.info(finish);
            return false;
        }

        log.info("RESTORE PLAYLIST: " + this.savedPlaylistName);
        if (this.savedPlaylistOk) this.playlistRestore(this.savedPlaylistName); // загрузить плейлист

        this.waitMilSeconds(100);

        //TODO проверить что плейлист нормальный
        // проверить если сейчас плейлист пустой - не сохранять
        Integer tracksCount = this.playlistTracksCurrentCount();
        log.info("CHECK TRACKS COUNT IN RESTORED PLAYLIST: " + tracksCount);
        if (tracksCount == null) return false;
        if (tracksCount == 0) return false;


        // проверить плейлист содержимое если трек технический - не загружать
        //log.info("SAVED PLAYLIST CHECK CONTENT...");
        //Integer playlistId = this.savedPlaylistGetIdByName(playlistName);
        //List<PlaylistTracksLoop.Track> playlist = this.savedPlaylistGetTracksById(playlistId, 0, 10);
        //if (playlist == null || playlist.isEmpty()) {
        //    log.info("PLAYLIST EMPTY. SKIP RESTORE !!!");
        //    return null;
        //}
        //List<PlaylistTracksLoop.Track> errorTracks = playlist.stream().filter(p -> p.title.contains("restore")).collect(Collectors.toList());
        //if (errorTracks != null && !errorTracks.isEmpty()) {
        //    log.info("BAD PLAYLIST SKIP RESTORE !!! " + errorTracks);
        //    return null;
        //} else
        //    log.info("PLAYLIST CHECK OK");


        this.waitMilSeconds(100);
        log.info("RESTORE SAVED INDEX: " + this.savedPlaylistIndex);
        if (this.savedPlaylistIndex != null && !"0".equals(this.savedPlaylistIndex)) {
            log.info("RESTORE SAVED INDEX: " + this.savedPlaylistIndex);
            this.playlistIndexSet(this.savedPlaylistIndex); // если трек превый 0 то ненадо восстанавливать индекс
        }
        //log.info("RESTORE SAVED MODE: " +this.savedPlaylistMode);
        //if ("play".equals(this.savedPlaylistMode)) {
        //    if (savedPlaylistTime > 5)
        //        this.playlistTimeSet(this.savedPlaylistTime);
        //    this.play();
        //} else {
        //    log.info("RESTORE SAVED MODE STOP");
        //    this.stop().waitSeconds(1).stop();
        //}


        log.info(finish);
        return true;
    }

    public void restoreSyncScript() {
        log.info(start);
//       если был в группе, вернуть в группу и завершить
        if (this.savedGroup != null && !this.savedGroup.isEmpty()) {
            log.info("RESTORE SYNC " + this.name + " TO: " + this.savedGroup);
            lmsPlayers.playerByName(this.savedGroup.get(0)).pause(); // остановить группу перед синком
            this.syncTo(this.savedGroup.get(0));
            log.info(finish);
            return;
        }
        log.info(finish);
    }

    // ========================================================================
    // Навигация по трекам
    // ========================================================================

    public Player ctrlPrevTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        this.repeatOff();
        Requests.postToLmsForStatus(RequestParameters.prevtrack(this.name).toString());
        this.saveLastTime();
        return this;
    }

    public Player ctrlNextTrack() {
        log.info("PLAYER: " + this.name + " NEXT TRACK");
        this.repeatOff();
        Requests.postToLmsForStatus(RequestParameters.nexttrack(this.name).toString());
        this.saveLastTime();
        return this;
    }

    public Player forward() {
        log.info("PLAYER: " + this.name + " FORWARD");
        Requests.postToLmsForStatus(RequestParameters.forward(this.name).toString());
        return this;
    }

    public Player rewind() {
        log.info("PLAYER: " + this.name + " REWIND");
        Requests.postToLmsForStatus(RequestParameters.rewind(this.name).toString());
        return this;
    }

    public Player playTrackNumber(String track) {
        log.info("PLAYER: " + this.name + " TRACK NUMBER " + track);
        Requests.postToLmsForStatus(RequestParameters.track(this.name, track).toString());
        this.saveLastTime();
        return this;
    }

    // ========================================================================
    // Режимы воспроизведения
    // ========================================================================

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

    // ========================================================================
    // Управление избранным и каналами
    // ========================================================================

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

    public String favoritesAdd() {
        log.info("START ADD ON " + this.name);
        String url = this.path();
        String title = "this.title";
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

    public Integer currentChannelIndexInFavorites() {
        String currentTitle = this.playlistName();
        log.info("CURRENT TITLE: " + currentTitle);
        Integer currentChannelIndex = null;
        List<String> favList = this.favorites();


        favList.forEach(f -> log.info(f));
        favList.replaceAll(f -> titleCrop(f));
        favList.forEach(f -> log.info(f));
        currentTitle = titleCrop(currentTitle);

        log.info("CURRENT TITLE CROP: " + currentTitle);
        log.info("FAVORITES LIST CROP: " + favList);
        if (favList.contains(currentTitle)) currentChannelIndex = favList.indexOf(currentTitle);
        return currentChannelIndex;
    }

    public Player playChannel(String channel) {
        int id = 1;
        if (channel == null || channel.equals(0)) return this;
        if (channel.contains("-")) id = channelGetRelative(channel) - 1 - 1;
        if (channel.contains("+")) id = channelGetRelative(channel) - 1 + 1;
        if (!(channel.contains("-") || channel.contains("+"))) id = Integer.parseInt(channel) - 1;
        log.info("PLAY CHANNEL: " + id);
        Requests.postToLmsForStatus(RequestParameters.playFavoritesId(this.name, id).toString());
        this.saveLastTime(); // сохранить время последнего включения
        this.lastChannelPlayer = id; // сохранить последний канал плеера
        Player.lastChannelCommon = id; // сохранить последний канал общий
        String channelName = this.favorites().get(id); // получить имя канала изи Избранного для сохранения плейлиста
        this.savePlaylist(channelName); // сохранить последний плейлист
        return this;
    }

    // если команда -1 или +1 канал найти в избраном по названию индекс текущего канала
    // если по названию не найден то последний сохраненный канал плеера или общий
    // если неполучилось отпределить текущий канал то переключить на 1 в Избранном
    public Integer channelGetRelative(String channel) {
        Integer delta = 0;
        if (channel.contains("-")) delta = Integer.valueOf(channel) * (-1);
        if (channel.contains("+")) delta = Integer.valueOf(channel);
        log.info("DELTA: " + delta);

        Integer currentChannel = this.currentChannelIndexInFavorites();

        log.info("CURRENT CHANNEL " + currentChannel);
        if (currentChannel == null) {
            currentChannel = this.lastChannelPlayer;
            log.info("GET CURRENT FROM PLAYER LAST CHANNEL: " + currentChannel);
        }
        if (currentChannel == null) {
            currentChannel = Player.lastChannelCommon;
            log.info("GET CURRENT FROM COMMON LAST CHANNEL: " + currentChannel);
        }
        if (currentChannel == null) {
            log.info("RETURN DEFAULT CHANNEL: 0");
            return 0;
        }
        Integer channelNew = currentChannel + delta;
        log.info("CHANNEL NEW: " + currentChannel + " + " + delta + " = " + channelNew);
        return channelNew;
    }

    public Player ctrlPrevChannel() {
        log.info("PLAYER: " + this.name + " PREV CHANNEL");
        ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
        playChannel("-1");
        return this;
    }

    public Player ctrlNextChannel() {
        log.info("PLAYER: " + this.name + " NEXT CHANNEL");
        ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false);
        playChannel("+1");
        return this;
    }

    public Player ctrlNextChannelOrTrack() {
        log.info("ctrlNextChannelOrTrack");
        if (this.playlistTracksCurrentCount() < 2) this.ctrlNextChannel();
        else this.ctrlNextTrack();
        return this;
    }

    public Player ctrlPrevChannelOrTrack() {
        log.info("ctrlPrevChannelOrTrack");
        if (this.playlistTracksCurrentCount() < 2) this.ctrlPrevChannel();
        else this.ctrlPrevTrack();
        return this;
    }

    public Integer savedPlaylistTracksCount(String playlistName, int first, int last) {
        log.info(start);
        // 1. Получить список всех плейлистов
        String json1 = Requests.postToLmsForJsonBody(RequestParameters.playlistsGetAll().toString());
        log.info("ALL PLAYLISTS GET OK");
        Map<String, Integer> playlistMap = JsonUtils.getPlaylistsMap(json1);
        if (playlistMap == null || playlistMap.isEmpty()) {
            log.warn("No playlists found");
            return null;
        }
        // 2. Найти ID плейлиста по имени
        Integer id = playlistMap.get(playlistName);
        if (id == null) {
            log.warn("Playlist '{}' not found", playlistName);
            return null;
        }
        log.info("Found playlist '{}' with id: {}", playlistName, id);

        String json = Requests.postToLmsForJsonBody(RequestParameters.playlistTracks(this.name, id, first, last).toString());
        // 4. Парсим JSON и извлекаем count
        Integer count = Integer.valueOf(JsonUtils.jsonGetValue(json, "count"));
        log.info("PLAYLIST " + playlistName + " TRACKS COUNT: " + count);
        log.info(finish);
        return count;

    }

    public Integer savedPlaylistGetIdByName(String playlistName) {
        log.info(start);
        // 1. Получить список всех плейлистов
        String json1 = Requests.postToLmsForJsonBody(RequestParameters.playlistsGetAll().toString());
        log.info("ALL PLAYLISTS GET OK");
        Map<String, Integer> playlistMap = JsonUtils.getPlaylistsMap(json1);
        if (playlistMap == null || playlistMap.isEmpty()) {
            log.warn("No playlists found");
            return null;
        }
        // 2. Найти ID плейлиста по имени
        Integer id = playlistMap.get(playlistName);
        if (id == null) {
            log.warn("Playlist '{}' not found", playlistName);
            return null;
        }
        log.info("Found playlist '{}' with id: {}", playlistName, id);
        return id;
    }

    public List<PlaylistTracksLoop.Track> savedPlaylistGetTracksById(Integer playlistId, int first, int last) {
        log.info(start);
        String json = Requests.postToLmsForJsonBody(RequestParameters.playlistTracks(this.name, playlistId, first, last).toString());
        //log.info(json);
        if (json == null) return null;
        PlaylistTracksLoop playlist = JsonUtils.jsonToPojo(json, PlaylistTracksLoop.class);
        if (playlist.result == null && playlist.result.playlisttracksLoop == null) return null;
        log.info(playlist);
        //log.info(playlist.result.count);
        //log.info(playlist.result.playlisttracksLoop);
        //log.info(playlist.result.playlisttracksLoop.get(0).title);
        log.info(finish);
        return playlist.result.playlisttracksLoop;
    }

    public ArrayList<PlayerStatus.PlaylistLoop> playlistTracksCurrent(int first, int last) {
        String json = Requests.postToLmsForJsonBody(RequestParameters.statusPlayer(name, last).toString());
        if (json == null) return null;
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        this.playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) return null;
        log.info("PLAYLIST TRACKS LOOP: " + this.playerStatus.result.playlist_loop);
        log.info(finish);
        return this.playerStatus.result.playlist_loop;
    }

    public int playlistTracksCurrentCount() {
        log.info(start);
        String json = Requests.postToLmsForJsonBody(RequestParameters.statusPlayer(name, 50).toString());
        if (json == null) return 0;
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        this.playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) return 0;
        log.info("PLAYLIST TRACKS CURRENT COUNT: " + this.playerStatus.result.playlist_tracks);
        log.info(finish);
        return this.playerStatus.result.playlist_tracks;
    }


    // ========================================================================
    // Синхронизация
    // ========================================================================

    public Player syncTo(String toPlayerName) {
        if (this.name.equals(toPlayerName)) {
            log.info("SKIP SYNC source and target player are the same " + this.name);
            return this;
        }
        log.info("SYNC " + this.name + " TO " + toPlayerName + " SYNC=true");
        Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
        this.sync = true;
        this.saveLastTime();
        return this;
    }

    public Player unsync() {
        log.info("PLAYER UNSYNC: " + this.name + " SYNC=false");
        Requests.postToLmsForStatus(RequestParameters.unsync(this.name).toString());
        this.sync = false;
        return this;
    }

    //public Player syncToAlt(String toPlayerName) {
    //    String path = lmsPlayers.playerByName(toPlayerName).path();
    //    if (path.contains("di.fm") || path.contains("audioaddict")) {
    //        log.info("SYNC DI.FM " + this.name + " PLAY PATH FROM " + toPlayerName);
    //        this.playPath(path);
    //        return this;
    //    } else {
    //        log.info("SYNC " + this.name + " TO " + toPlayerName);
    //        Requests.postToLmsForStatus(RequestParameters.sync(this.name, toPlayerName).toString());
    //    }
    //    return this;
    //}

    public Player syncToPlayingOrPlayLast() {
        log.info(start);
        // найти играющий плеер и подключиться к нему
        Player playingPlayer = lmsPlayers.playingPlayer(this.name, true);
        if (playingPlayer != null && !playingPlayer.separate && !this.separate) {
            log.info(this.name + " SYNC TO PLAYING " + playingPlayer.name);
            this.syncTo(playingPlayer.name);
            this.sync = true;
        }
        // если нет играющего, некуда подключиться - играть последнее
        else {
            if (!"play".equals(this.mode)) {
                log.info(this.name + " PLAY LAST");
                this.playLast();
            } else {
                log.info(this.name + " ITS PLAYING " + this.mode);
            }
        }
        log.info(finish);
        return this;
    }

    public Player syncOtherPlayingNotInGroupToThis() {
        log.info("SYNC OTHER PLAYING TO THIS " + this.name);
        if (this.separate) {
            log.info("PLAYER IS SEPARETE. STOP SYNC");
            return this;
        }
        Map<String, List<String>> groups = this.playingPlayersNameGroups(true);
        List<String> playingPlayersNamesNotInCurrentGrop = groups.get("notInGroup");

        playingPlayersNamesNotInCurrentGrop.stream()
                .map(name -> lmsPlayers.playerByName(name))
                .filter(player -> !player.separate)
                .forEach(player -> player
                        .unsync()
                        .syncTo(this.name));
        return this;
    }

    public Map<String, List<String>> playingPlayersNameGroups(Boolean exceptSeparated) {
        log.info("SEARCH FOR PLAYERS PLAYING IN CURRENT GROUP AND OTHER GROUP");
        List<String> playingPlayersNames = lmsPlayers.playingPlayersNames(this.name, exceptSeparated);
        List<List<String>> groups = lmsPlayers.syncgroups();
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

    // ========================================================================
    // Получение статуса и информации
    // ========================================================================

    public void resetPlayerStatus() {
        //log.info("RESET STATUS " + name);
        playerStatus = null;
        connected = false;
        mode = "offline";
        playing = false;
        volume = "0";
        sync = false;
        title = "unknown";
        //separate = false;
    }

    public void statusClear() {
        this.connected = false;
        this.mode = "stop";
        this.playing = false;
        this.playerStatus = null;
        this.volume = null;
        this.title = null;
    }

    public PlayerStatus.Result status() {
        //log.info(start);
        log.debug("STATUS FOR PLAYER: " + name);
        resetPlayerStatus();
        String json = Requests.postToLmsForJsonBody(RequestParameters.statusPlayer(name, 50).toString());
        if (json == null) return null;
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) return null;
        PlayerStatus.Result result = playerStatus.result;
        connected = (result.player_connected == 1);
        volume = String.valueOf(result.mixer_volume);
        shuffle = result.playlist_shuffle != 0;
        repeat = result.playlist_repeat != 0;
        playing = "play".equals(result.mode);
        mode = playing ? "play" : "stop";
        title = result.current_title;
        sync = (result.sync_slaves != null || result.sync_master != null);
        if (sync) separate = false;
        log.info(this);
        //log.info(finish);
        return result;
    }

    public PlayerStatus.Result statusFast() {
        log.debug("REQUEST STATUS FAST" + name);
        String json = Requests.postToLmsForJsonBody(RequestParameters.statusPlayer(name, 0).toString());
        if (json == null) return null;
        json = json.replaceAll("(\"\\w*)(\\s)(\\w*\":)", "$1_$3");
        playerStatus = JsonUtils.jsonToPojo(json, PlayerStatus.class);
        if (playerStatus == null || playerStatus.result == null) return null;
        return playerStatus.result;
    }

    public void update() {
        this.status();
    }

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

    public Boolean playing() {
        this.playing = false;
        Response response = Requests.postToLmsForResponse(RequestParameters.mode(this.name).toString());
        if (response == null) {
            log.info("REQUEST ERROR " + this.name);
            return this.playing;
        }
        if (response.result._mode.equals("play")) this.playing = true;
        log.info("PLAYER: " + this.name + " MODE: " + response.result._mode + " PLAYING: " + this.playing);
        return this.playing;
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

    public String path() {
        Response response = Requests.postToLmsForResponse(RequestParameters.path(this.name).toString());
        if (response == null) return "";
        if (!pathIsOk(response.result._path)) return "";
        return response.result._path;
    }

    public String playlistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.playlistname(this.name).toString());
        if (response == null) return null;
        log.info("PLAYER: " + this.name + " PLAYLIST: " + response.result._name);
        return response.result._name;
    }

    public String albumName() {
        String jsonResponse = Requests.postToLmsForJsonBody(RequestParameters.albumname(this.name).toString());
        if (jsonResponse == null) return null;
        jsonResponse = JsonUtils.fixQuote(jsonResponse);
        Response response = JsonUtils.jsonToPojo(jsonResponse, Response.class);
        if (response == null) return null;
        return response.result._album;
    }

    public String trackName() {
        this.trackName = null;
        PlayerStatus.Result playerStatus = this.statusFast();
        if (playerStatus != null && playerStatus.remoteMeta != null && playerStatus.remoteMeta.title != null) {
            log.info("TRACK NAME: " + playerStatus.remoteMeta.title);
            this.trackName = playerStatus.remoteMeta.title;
        }
        return this.trackName;
    }

    public String artistName() {
        Response response = Requests.postToLmsForResponse(RequestParameters.artistname(this.name).toString());
        if (response == null) return null;
        return response.result._artist;
    }

    public String title() {
        log.info(start);
//        log.info("TITLE FOR PLAYER: " + this.name + " -------------");
        if (!this.connected) {
            this.title = "offline";
            log.info("PLAYER OFFLINE");
            return this.title;
        }
        String title = playlistName();
        if (title == null) title = artistName();
        if (title == null) title = albumName();
        if (title == null) title = trackName();
        if (title != null) title = titleCrop(title);
        if (title != null && title.contains("music/sounds")) title = null;
        if (title != null && title.contains("_restore")) title = null;
        if (title != null && title.equals("loop")) title = null;
        if (title == null) title = "unknown";
        log.info("PLAYER: " + this.name + " TITLE: " + title);
        this.title = title;

        log.info(finish);
        return title;
    }

    public String titleCrop(String title) {
        if (title == null) return null;
        title = title
                .replaceAll(":.*", "")
                .replaceAll("  .*", "")
                .replaceAll("\\s+", " ")
                .replaceAll(" - .*", "")
                .replaceAll("\\(.*", "")
                .replaceAll("\\[.*", "")
                .replaceAll(",.*", "")
                .replaceAll(",", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (title.length() > 30) title = title.substring(0, 30);
        return title;
    }

    // ========================================================================
    // Сценарии включения/выключения музыки
    // ========================================================================

    public Player turnOnMusic(String volume) {
        log.info(start);
        log.info("PLAYER: " + this.name + " TURN ON MUSIC");
        if (!this.playing) this.unsync();
        this.ifExpiredAndNotPlayingUnsyncWakeSetVolume(volume, true);
        this.syncToPlayingOrPlayLast();
        this.saveLastTime();
        log.info(finish);
        return this;
    }

    public Player turnOffMusic() {
        log.info(start);
        log.info("PLAYER: " + this.name + " TURN OFF MUSIC");
        this.unsync().pause();
        log.info(finish);
        return this;
    }

    public Player toggleMusic() {
        if (this.playing) {
            log.info("PLAYER: " + this.name + " MUSIC TURN OFF");
            this.turnOffMusic();
        } else {
            log.info("PLAYER: " + this.name + " MUSIC TURN ON");
            this.turnOnMusic(null);
        }
        return this;
    }

    public Player stopOther() {
        log.info("STOP ALL except " + this.name);
        lmsPlayers.checkUpdated();
        lmsPlayers.players.parallelStream()
                .filter(p -> !p.name.equals(this.name))
                .filter(p -> p.connected)
                .forEach(player -> player.turnOffMusic());
        return this;
    }

    public Player playLast() {
        log.info(start);

        // если у плеера сейчас есть какойто путь и это не технический типа шума, тишины, уведомления то просто нажать плэй
        String thisPath = this.path();
        if (pathIsOk(thisPath)) {
            log.info("PLAY LAST - PUSH PLAY BUTTON: " + thisPath);
            this.play();
            log.info(finish);
            return this;
        }

        // если у плеера путь пустой попытка восстановить сохраненный плейлист
        log.info("PLAY LAST - RESTORE PLAYLIST TRY...");
        if (this.restorePlaylistScript(true)) {
            log.info("PLAY LAST - RESTORE PLAYLIST OK");
            log.info(finish);
            return this;
        }

////        log.info("THIS LAST PATH: " + thisLastPath);
////        log.info("COMMON LAST PATH: " + commonLastPath);
        log.info("PLAY LAST - TRY DEFAULT");
        List<String> favorites = this.favorites();
        if (favorites != null && !favorites.isEmpty()) {
            log.info("PLAY LAST - PLAY CHANNEL DEFAULT " + STRING_DEFAULT_CHANNEL.value);
            this.playChannel(STRING_DEFAULT_CHANNEL.value);
        } else {
            log.info("PLAY LAST - PLAY LINK DEFAULT " + STRING_DEFAULT_LINK.value);
            this.playPath(STRING_DEFAULT_LINK.value);
        }
        log.info(finish);
        return this;
    }

    // ========================================================================
    // Управление разделением (separate)
    // ========================================================================

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

    public Player separateOn() {
        log.info("SEPARATE ON START. PLAYER: " + this);
        if (this.separate) {
            log.info("ALREADY SEPARATE ON, UNSYNC, WAKE, PLAY LAST ");
            if (!this.playing) this.turnOnMusic(null);
            return this;
        }
        log.info("SEPARATE ON, UNSYNC, WAKE, PLAY LAST ");
        this
                .separateFlagTrue()
                .unsync();
        if (!this.playing) this.turnOnMusic(null);
        else
            this.play();
        log.info("SEPAREATE ON FINISH. PLAYER: " + this);
        return this;
    }

    public Player separateOff() {
        log.info("SEPARATE OFF ALL. SYNC ALL PLAYING TO HERE");
        // Сброс флага у всех (включая this, если он в списке)
        lmsPlayers.players.forEach(Player::separateFlagFalse);
        List<Player> playingPlayers = lmsPlayers.playingPlayers(this.name, false);
        // если этот играет все играющие подключить к нему
        if (this.playing && playingPlayers != null) {
            playingPlayers.forEach(p -> {
                p.unsync().syncTo(this.name);
            });
            return this;
        }
        // если этот не играет подключить его к играющим
        if (!this.playing) {
            this.ifExpiredAndNotPlayingUnsyncWakeSetVolume(null, false)
                    .unsync().syncToPlayingOrPlayLast();
        }
        return this;
    }

    // ========================================================================
    // TTS и звуковые уведомления
    // ========================================================================

    public Player say(String text, Boolean restorePlaylist, Boolean restoreSync) {
        log.info(start);
        log.info("PLAYER: " + this.name + " TEXT: " + text + " TOGGLE: " + VOICE_TOGGLE_VOICE_TO_LMS.value);
        if (!VOICE_TOGGLE_VOICE_TO_LMS.value || text == null) return this;
        if (restorePlaylist) this.savePlaylistScript(); // сохранить плейлист перед уведомлением
        if (restoreSync && !restorePlaylist) this.saveSyncScript(); // сохранить только группу
        this.savedPlaylistVolume = this.volumeGet(); // сохранить громкость
        this.unsync();
        this.pause();

        if (VOICE_TOGGLE_VOLUME_AMP_LMS.value) {
            int volume = this.valueVolumeByTime() + 10; // громкость уведомления по времени + 10
            this.volumeSetHardAsync(String.valueOf(volume), 300, 3); // громкость для уведомления
        }

        if (VOICE_TOGGLE_TTS_GOOGLE.value) {
            this.playFile(GoogleTTS.textToVoiceFile(text)); // генерация через Google
            log.info("***** WAIT " + GoogleTTS.duration * 1000 + VOICE_VALUE_DELAY_AFTER_NOTIFICATION.value);
            this.waitMilSeconds((int) (GoogleTTS.duration * 1000 + VOICE_VALUE_DELAY_AFTER_NOTIFICATION.value));
            waitForPlaybackCompletion(this, VOICE_VALUE_WAIT_FOR_NOTIFICATION_COMPLITED.value);

        }
        if (VOICE_TOGGLE_TTS_YANDEX.value) {
            this.playFile(YandexTTS.textToVoiceFile(text)); // воспроизведение уведомления
            log.info("***** WAIT " + YandexTTS.duration * 1000 + VOICE_VALUE_DELAY_AFTER_NOTIFICATION.value);
            this.waitMilSeconds((int) (YandexTTS.duration * 1000 + VOICE_VALUE_DELAY_AFTER_NOTIFICATION.value));
            waitForPlaybackCompletion(this, VOICE_VALUE_WAIT_FOR_NOTIFICATION_COMPLITED.value); // дождаться завершения уведомления
        }
        log.info("***** WAIT FINISH");

        if (VOICE_TOGGLE_VOLUME_AMP_LMS.value) this.volumeSet(this.savedPlaylistVolume); // громкость восстановить

        this.playlistClear();
        if (restorePlaylist) this.restorePlaylistScript(restoreSync); // восстановить плейлист до уведомления
        if (!restorePlaylist || restoreSync) this.restoreSyncScript(); // восстановить плейлист до уведомления

        if (VOICE_TOGGLE_VOLUME_AMP_LMS.value) this.volumeSetHardAsync(this.savedPlaylistVolume, 500, 6);

        log.info(finish);
        return this;
    }

    public Player signal(String file) {
        log.info(start);
        log.info("PLAYER: " + this.name + " FILE: " + file + " TOGGLE: " + VOICE_TOGGLE_VOICE_TO_LMS.value);
        if (!VOICE_TOGGLE_VOICE_TO_LMS.value || file == null) return this;
        this.unsync();
        //this.pause();
        this.playFile("http://" + Main.myIp + ":8010/music/sounds/" + file + ".mp3"); // воспроизведение уведомления
        this.waitMilSeconds(2000);
        waitForPlaybackCompletion(this, VOICE_VALUE_WAIT_FOR_NOTIFICATION_COMPLITED.value); // дождаться завершения уведомления
        //this.playlistClear();
        log.info(finish);
        return this;
    }


    // ========================================================================
    // Сохранение времени и последнего пути
    // ========================================================================

    public Player saveLastTime() {
        this.lastPlayTimePlayer = LocalTime.now(zoneId).truncatedTo(MINUTES).toString();
        log.info(this.name + " LAST TIME: " + this.lastPlayTimePlayer);
//        CompletableFuture.runAsync(() -> {
//            saveLastPathThis();
//            savePlaylistScript();
//        lmsPlayers.write();
//        });
        return this;
    }


    // ========================================================================
    // Работа с просроченным состоянием и пробуждением
    // ========================================================================

    public boolean checkLastPlayTimeExpired() {
        if (TOGGLE_EXPIRED_ALWAYS.value) {
            log.info("TOGGLE LAST PLAY TIME ALWAYS EXPIRED ENABLED !!! ");
            return true;
        }
        long delayExpire = lmsPlayers.delayExpire;
        if (this.lastPlayTimePlayer == null) {
            log.info("PLAYER: " + this.name + "EXPIRED LAST TIME NULL " + this.lastPlayTimePlayer);
            return true;
        }
        LocalTime playerTime = LocalTime.parse(this.lastPlayTimePlayer).truncatedTo(MINUTES);
        LocalTime nowTime = LocalTime.now(zoneId).truncatedTo(MINUTES);
        long diff = playerTime.until(nowTime, MINUTES);
        Boolean expired = diff > delayExpire || diff < 0;
        log.info("PLAYER: " + this.name + " LAST PLAY TIME: " + this.lastPlayTimePlayer + " NOW: " + nowTime + " DELAY MINUTES: " + delayExpire + " DIFF: " + diff + " EXPIRED: " + expired);
        return expired;
    }


    public Player ifExpiredAndNotPlayingUnsyncWakeSetVolume(String volume) {
        return ifExpiredAndNotPlayingUnsyncWakeSetVolume(volume, true);
    }

    public Player ifExpiredAndNotPlayingUnsyncWakeSetVolume(String volume, boolean restore) {

        log.info(start);
        if (!this.connected) return this;
        // LMS неможет изменить горомкость плеера если он не играет, надо запустить воспроизведение и только тогда менять громкость !!!
        if (!this.playing && this.checkLastPlayTimeExpired()) {
            log.info("PLAYER " + this.name + " NOT PLAY - WAKE, SET VOLUME " + volume + " RESTORE " + restore);
            this.wakeAndSetVolume(volume, restore);
            log.info(finish);
            return this;
        } else {
            log.info("PLAYER " + this.name + " PLAYING - SKIP WAKE, SET VOLUME " + volume + " RESTORE " + restore);
            if (volume != null) this.volumeSetLimited(volume);
        }
        log.info(finish);
        return this;
    }

    public Player volumeSetHardAsync(String volume, int delay, int count) {
        log.info("VOLUME SET HARD ASYNC " + volume);
        String vv = volume;
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < count; i++) {
                log.info("VOLUME RETRY count " + i + " PLAYER: " + this.name);
                this.volumeSet("+1");
                this.waitMilSeconds(delay);
                this.volumeSet(vv);
                this.waitMilSeconds(100);
                this.volumeGet();
            }
        });
        return this;
    }

    private Player wakeAndSetVolume(String volume, boolean restore) {
        log.info(start);
        log.info("WAKE PLAYER: " + this.name + " WAIT: " + this.delay + " VOLUME: " + volume + " RESTORE " + restore);
        if (volume == null) volume = String.valueOf(this.valueVolumeByTime());
        if (this.delay == null) this.delay = config.delay;

        if (!TOGGLE_WAKE.value) {
            log.info("TOGGLE WAKE DISABLED !!!");
            this.volumeSet(volume);
            return this;
        }

        if (restore) this.savePlaylistScript(); // сохранить плейлист до
        this.unsync();

        log.info("******* WAKE START ******* count: " + this.delay);
        this.playSilence(); // запуск проигрывания шума
        for (int i = 0; i < delay; i++) {
            log.info("count " + i + " PLAYER: " + this.name);
            this.volumeSet("+1");
            this.volumeSet(volume);
            this.waitSeconds(1);
        }
        this.pause();
        log.info("******* WAKE FINISH *******");

        if (restore) this.restorePlaylistScript(false); // восстановить плейлист
        this.saveLastTime();
        log.info(finish);
        return this;
    }

    // ========================================================================
    // Вспомогательные утилиты
    // ========================================================================

    public Boolean pathIsOk(String path) {
        return (path != null &&
                !path.equals(config.silence) &&
                !path.contains("speech.mp3") &&
                !path.contains("music/sounds") &&
                !path.equals(""));
    }

    public Player waitSeconds(Integer delay) {
        log.info("wait " + delay + " second");
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public Player waitMilSeconds(Integer delay) {
        log.info("wait " + delay + " second");
        try {
            Thread.sleep(delay);
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

    public void remove() {
        lmsPlayers.players.remove(this);
    }

    // ========================================================================
    // Переопределённые методы
    // ========================================================================

    @Override
    public String toString() {
        return String.format(
                "NAME:%-15s " +
                        "ROOM:%-10s" +
                        "CONNECTED:%-5b " +
                        "SEPARATED:%-5b " +
                        "SYNC:%-5b " +
                        "VOL:%-3s " +
                        "MODE:%-7s " +
                        "TITLE:%-15s" +
                        " PLAY:%-5b " +
                        "TIME:%-5s ",
                name,
                room,
                connected,
                separate,
                sync,
                volume,
                mode,
                title,
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