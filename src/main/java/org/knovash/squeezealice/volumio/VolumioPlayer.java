package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.Player;
import org.knovash.squeezealice.lms.PlayerStatus;
import org.knovash.squeezealice.lms.ServerStatus;
import org.knovash.squeezealice.volumio.VolumioStateResponse;
import org.knovash.squeezealice.volumio.RequestsVolumio;
import org.knovash.squeezealice.volumio.VolumioCommands;

import java.util.*;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.lmsPlayers;

@Log4j2
@Data
@EqualsAndHashCode(callSuper = true)  // добавляем эту строку
public class VolumioPlayer extends Player {

    public String baseUrl;
    public transient VolumioStateResponse currentState;

    // -------------------------------------------------------------------------
    // Конструктор
    // -------------------------------------------------------------------------
    public VolumioPlayer(String name) {
        this.name = name;
        this.baseUrl =  "http://" + config.volumioIp;
    }

    // -------------------------------------------------------------------------
    // Сброс состояния
    // -------------------------------------------------------------------------
    @Override
    public void resetPlayerStatus() {
        log.info("RESET STATUS " + name);
        currentState = null;
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

    // -------------------------------------------------------------------------
    // Статические методы обновления/очистки (скрывают родительские)
    // -------------------------------------------------------------------------
//    public static void updatePlayer(ServerStatus.PlayersLoop p) {
//        Player player = lmsPlayers.playerByName("Volumio");
//        player.status();
//    }

    public void update(ServerStatus.PlayersLoop p) {
       log.info("VOLUMIO PLAYER UPDATE");
       status();
    }

    public void cleanPlayer() {
        this.connected = true;
        this.mode = "stop";
        this.playing = false;
        this.playerStatus = null;
        this.volume = null;
        this.title = null;
    }

    // -------------------------------------------------------------------------
    // Получение статуса
    // -------------------------------------------------------------------------
    @Override
    public void status() {
        String url = VolumioCommands.getState(baseUrl);
        String json = RequestsVolumio.get(url);

        connected = false;
        mode = "stop";
        playing = false;

        try {
            currentState = new ObjectMapper().readValue(json, VolumioStateResponse.class);
            connected = (currentState != null);
            playing = "play".equals(currentState.status);
            mode = playing ? "play" : "stop";
            title = currentState.title != null ? currentState.title : "unknown";
            volume = String.valueOf(currentState.volume);
        } catch (Exception e) {
            log.error("Failed to parse state response from {}: {}", baseUrl, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Получение информации о текущем треке
    // -------------------------------------------------------------------------
    @Override
    public String title() {
        if (currentState != null && currentState.title != null)
            return titleCrop(currentState.title);
        return "unknown";
    }

    // titleCrop() унаследован, можно не переопределять

    @Override
    public String path() {
        return currentState != null ? currentState.uri : "";
    }

    @Override
    public String requestPlaylistName() {
        return currentState != null ? currentState.title : null;
    }

    @Override
    public String albumName() {
        return currentState != null ? currentState.album : null;
    }

    @Override
    public String trackName() {
        return currentState != null ? currentState.title : null;
    }

    @Override
    public String artistName() {
        return currentState != null ? currentState.artist : null;
    }

    // -------------------------------------------------------------------------
    // Статистические запросы к LMS (для Volumio неактуальны)
    // -------------------------------------------------------------------------
    public static String count() {
        log.debug("count() not supported for Volumio");
        return "0";
    }

    public static String name(String index) { // неиспользуется
        log.debug("name() not supported for Volumio");
        return null;
    }

    public static String id(String index) { // неиспользуется
        log.debug("id() not supported for Volumio");
        return null;
    }

    // -------------------------------------------------------------------------
    // Геттеры состояния
    // -------------------------------------------------------------------------
    @Override
    public String mode() {
        status();
        return mode;
    }

    @Override
    public Boolean playing() {
        status();
        return playing;
    }

    @Override
    public String volumeGet() {
        status();
        return volume;
    }

    // -------------------------------------------------------------------------
    // Избранное (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public List<String> favorites() {
        return new ArrayList<>();
    }

    @Override
    public void favoritesAdd(String url, String title) {
        log.debug("favoritesAdd not supported for Volumio");
    }

    // -------------------------------------------------------------------------
    // Базовые команды воспроизведения
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer play() {
        log.info("PLAY: {}", name);
        RequestsVolumio.get(VolumioCommands.play(baseUrl));
        return this;
    }

    @Override
    public VolumioPlayer pause() {
        log.info("PAUSE: {}", name);
        RequestsVolumio.get(VolumioCommands.pause(baseUrl));
        return this;
    }

    @Override
    public VolumioPlayer togglePlayPause() {
        log.info("TOGGLE PLAY/PAUSE: {}", name);
        RequestsVolumio.get(VolumioCommands.toggle(baseUrl));
        return this;
    }

    @Override
    public VolumioPlayer playPath(String path) {
        log.info("playPath not supported for Volumio: {}", path);
        return this;
    }

    @Override
    public VolumioPlayer playSilence() {
        log.info("playSilence not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer ctrlPrevTrack() {
        log.info("PREV: {}", name);
        RequestsVolumio.get(VolumioCommands.prev(baseUrl));
        return this;
    }

    @Override
    public VolumioPlayer ctrlNextTrack() {
        log.info("NEXT: {}", name);
        RequestsVolumio.get(VolumioCommands.next(baseUrl));
        return this;
    }

    @Override
    public VolumioPlayer playTrackNumber(String track) {
        log.info("playTrackNumber not supported for Volumio: {}", track);
        return this;
    }

    @Override
    public VolumioPlayer shuffleOn() {
        log.debug("shuffle not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer shuffleOff() {
        log.debug("shuffle not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer repeatOn() {
        log.debug("repeat not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer repeatOff() {
        log.debug("repeat not supported for Volumio");
        return this;
    }

    // -------------------------------------------------------------------------
    // Синхронизация (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer syncTo(String toPlayerName) {
        log.debug("syncTo not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer syncToPlayingOrPlayLast() {
        log.debug("syncToPlaying not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer syncToPlayingAndStopPlayingPlayer() {
        log.debug("syncToPlayingAndStopPlayingPlayer not supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer unsync() {
        log.debug("unsync not supported for Volumio");
        return this;
    }

    // -------------------------------------------------------------------------
    // Сценарии включения/выключения
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer turnOnMusic(String volume) {
        log.info("TURN ON MUSIC: {}", name);
        this.play();
        if (volume != null) this.volume(volume);
        return this;
    }

    @Override
    public VolumioPlayer turnOffMusic() {
        log.info("TURN OFF MUSIC: {}", name);
        this.pause();
        return this;
    }

    @Override
    public VolumioPlayer toggleMusic() {
        log.info("TOGGLE MUSIC: {}", name);
        if (this.playing) this.turnOffMusic();
        else this.turnOnMusic(null);
        return this;
    }

    // -------------------------------------------------------------------------
    // Управление громкостью
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer volume(String value) {
        log.info("SET VOLUME {} to {}", value, name);
        try {
            int vol = Integer.parseInt(value);
            RequestsVolumio.get(VolumioCommands.setVolume(baseUrl, vol));
        } catch (NumberFormatException e) {
            log.error("Invalid volume value: {}", value);
        }
        return this;
    }

    @Override
    public VolumioPlayer volumeNoLog(String value) {
        // без лога
        try {
            int vol = Integer.parseInt(value);
            RequestsVolumio.get(VolumioCommands.setVolume(baseUrl, vol));
        } catch (NumberFormatException e) {
            // ignore
        }
        return this;
    }

    @Override
    public VolumioPlayer volumeSetLimited(String value) {
        log.info("VOLUMIO VOLUME LIMITED {}", value);
        if (value.contains("-")) volumeDown();
        else if (value.contains("+")) volumeUp();
        else volume(value);
        return this;
    }

    // Специфичные для Volumio шаги громкости
    public VolumioPlayer volumeUp() {
        log.info("VOLUME UP: {}", name);
        RequestsVolumio.get(VolumioCommands.volumeUp(baseUrl));
        return this;
    }

    public VolumioPlayer volumeDown() {
        log.info("VOLUME DOWN: {}", name);
        RequestsVolumio.get(VolumioCommands.volumeDown(baseUrl));
        return this;
    }

    public VolumioPlayer mute() {
        log.info("MUTE: {}", name);
        RequestsVolumio.get(VolumioCommands.mute(baseUrl));
        return this;
    }

    public VolumioPlayer unmute() {
        log.info("UNMUTE: {}", name);
        RequestsVolumio.get(VolumioCommands.unmute(baseUrl));
        return this;
    }

    // -------------------------------------------------------------------------
    // Каналы (не поддерживаются)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer playChannel(String channel) {
        log.debug("playChannel not supported for Volumio");
        return this;
    }

    @Override
    public Integer channelGetRelative(String channel) {
        log.debug("channelGetRelative not supported for Volumio");
        return 0;
    }

    @Override
    public Integer currentChannelIndexInFavorites() {
        log.debug("currentChannelIndexInFavorites not supported for Volumio");
        return 0;
    }

    @Override
    public VolumioPlayer ctrlPrevChannel() {
        return ctrlPrevTrack();
    }

    @Override
    public VolumioPlayer ctrlNextChannel() {
        return ctrlNextTrack();
    }

    @Override
    public VolumioPlayer ctrlNextChannelOrTrack() {
        return ctrlNextTrack();
    }

    @Override
    public VolumioPlayer ctrlPrevChannelOrTrack() {
        return ctrlPrevTrack();
    }

    @Override
    public int requestPlaylistTracks() {
        log.debug("requestPlaylistTracks not supported for Volumio");
        return 0;
    }

    // -------------------------------------------------------------------------
    // Добавление в избранное (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public String favoritesAdd() {
        log.debug("favoritesAdd not supported for Volumio");
        return null;
    }

    // -------------------------------------------------------------------------
    // Альтернативная синхронизация (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer syncToAlt(String toPlayerName) {
        log.debug("syncToAlt not supported for Volumio");
        return this;
    }

    // -------------------------------------------------------------------------
    // Пробуждение и установка громкости (упрощённо)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer ifExpiredAndNotPlayingUnsyncWakeSetVolume(String volume) {
        // Всегда просто устанавливаем громкость
        this.volume(volume);
        return this;
    }

    // wakeAndSetVolume приватный в родителе, не переопределяем

    // -------------------------------------------------------------------------
    // Эксклюзивное управление
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer onlyHere() {
        log.info("ONLY HERE not fully supported for Volumio");
        return this;
    }

    @Override
    public VolumioPlayer stopOther() {
        log.info("stopOther not supported for Volumio");
        return this;
    }

    // -------------------------------------------------------------------------
    // Воспроизведение последнего
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer playLast() {
        log.info("playLast not implemented for Volumio, just play");
        this.play();
        return this;
    }

    // -------------------------------------------------------------------------
    // Управление громкостью по времени (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer setVolumeByTime() {
        log.debug("setVolumeByTime not supported for Volumio");
        return this;
    }

    @Override
    public Integer valueVolumeByTime() {
        log.debug("valueVolumeByTime not supported for Volumio");
        return Integer.valueOf(volume);
    }

    // -------------------------------------------------------------------------
    // Ожидание (наследуется)
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Сохранение времени и пути (не нужно)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer saveLastTimePath() {
        // Ничего не делаем
        return this;
    }

    // -------------------------------------------------------------------------
    // Удаление плеера (наследуется)
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // Флаги отдельного управления
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer separateFlagTrue() {
        this.separate = true;
        log.info("SEPARATE FLAG: {} {}", this.name, this.separate);
        return this;
    }

    @Override
    public VolumioPlayer separateFlagFalse() {
        this.separate = false;
        log.info("SEPARATE FLAG: {} {}", this.name, this.separate);
        return this;
    }

    @Override
    public VolumioPlayer separateOn() {
        log.info("SEPARATE ON (stub)");
        this.separateFlagTrue();
        return this;
    }

    @Override
    public VolumioPlayer separateOff() {
        log.info("SEPARATE OFF (stub)");
        this.separateFlagFalse();
        return this;
    }

    // -------------------------------------------------------------------------
    // Синхронизация других играющих (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public VolumioPlayer syncOtherPlayingNotInGroupToThis() {
        log.debug("syncOtherPlayingNotInGroupToThis not supported for Volumio");
        return this;
    }

    // -------------------------------------------------------------------------
    // Проверка устаревания времени последнего воспроизведения
    // -------------------------------------------------------------------------
    @Override
    public boolean checkLastPlayTimeExpired() {
        // Всегда считаем, что не устарело
        return false;
    }

    // -------------------------------------------------------------------------
    // Группы играющих плееров (не поддерживается)
    // -------------------------------------------------------------------------
    @Override
    public Map<String, List<String>> playingPlayersNameGroups(Boolean exceptSeparated) {
        return new HashMap<>();
    }

    // -------------------------------------------------------------------------
    // Специфичные геттеры Volumio
    // -------------------------------------------------------------------------
    public String getVolume() {
        return volume;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPlaying() {
        return playing;
    }

    // -------------------------------------------------------------------------
    // Дополнительные команды Volumio
    // -------------------------------------------------------------------------
    public VolumioPlayer stop() {
        log.info("STOP: {}", name);
        RequestsVolumio.get(VolumioCommands.stop(baseUrl));
        return this;
    }

    // -------------------------------------------------------------------------
    // toString, equals, hashCode
    // -------------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format(
                "ROOM:%-10s NAME:%-15s CONNECTED:%-5b VOL:%-3s MODE:%-7s TITLE:%-30s",
                room, name, connected, volume, mode, title
        );
    }

    // equals и hashCode наследуются от Player
}