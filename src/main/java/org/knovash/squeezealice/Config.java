package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;
import org.knovash.squeezealice.utils.Mask;

import java.time.ZoneId;
import java.util.ResourceBundle;

import static org.knovash.squeezealice.Main.config;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {

// Squeeze-Alice
    public int port; // порт этого сервера
    public String domain; // для колбэка но в локальном ненужно
    public ZoneId zoneId; // для установки часового пояса пользователя
// Lyrion Music Server
    public String lmsIp; // адрес LMS
    public String lmsPort; // порт LMS обычно 9000
    public String lmsUrl; // урл LMS для запросов jsonrpc.js
    public String silence; // урл для звука тишины перед включением музыки
// Volumio
    public String volumioIp;
// Hive
    public String hiveBroker;
    public String hiveUsername;
    public String hivePassword;
// Yandex
    public String yandexToken; // для получения списка комнат в умном доме
    public String yandexName; // для отображения кто вошел
    public String yandexUid; // для уникального названия топика пользователя
    public String skillId; // id навыка Lyrion Music Server публичный
    public String yandextSkillTokenDeveloper; // id навыка Lyrion Music Server публичный
//Тип токена: OAuth-токен разработчика навыка.
//Как получить: Через консоль разработчика Яндекс.Диалогов: Навык → Настройки → Авторизация для HTTP-запросов → Скопировать OAuth-токен.
//Не требует программирования – токен статичен для навыка.
//Назначение: Управление состоянием навыка Алисы (отправка событий, состояние сессии).
//Срок жизни: Бессрочный (но можно перегенерировать вручную).

// Spotify
    public String spotifyToken; // для запросов поиска
    public String spotifyName; // для отображения кто вошел
    public String spotifyRefreshToken; // рефреш токен
    public long spotifyTokenExpiresAt; // системное время когда истекает токен

    public String fileRoomsAndAliceIds = "data/rooms_and_alice_ids.json";
    public String fileRoomsAndPlayers = "data/rooms_and_players.json";
    public String fileDevices = "data/devices.json";
    public String fileLmsPlayers = "data/lms_players.json";

    public void readConfigProperties() {
        log.info("READ config.properties");
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        this.port = Integer.parseInt(bundle.getString("port"));
        this.domain = bundle.getString("domain");
        this.lmsIp = bundle.getString("lmsIp");
        this.lmsPort = bundle.getString("lmsPort");
        this.volumioIp = bundle.getString("volumioIp");
        this.silence = bundle.getString("silence");
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
        this.hiveBroker = bundle.getString("hiveBroker");
        this.hiveUsername = bundle.getString("hiveUsername");
        this.hivePassword = bundle.getString("hivePassword");
        this.skillId = bundle.getString("skillId");
        this.yandextSkillTokenDeveloper = bundle.getString("yandextSkillTokenDeveloper");
        log.debug("CONFIG: " + config);
    }

    public void readConfigJson() {
        log.info("READ config.json");
        Config jsonConfig = JsonUtils.jsonFileToPojo("data/config.json", Config.class);
        if (jsonConfig == null) {
            log.info("NO FILE config.json WRITE NEW");
            config.write();
            return;
        }
        this.port = jsonConfig.port;
        this.domain = jsonConfig.domain;
        this.zoneId = jsonConfig.zoneId;

        this.lmsIp = jsonConfig.lmsIp;
        this.lmsPort = jsonConfig.lmsPort;
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
        this.silence = jsonConfig.silence;

        if (jsonConfig.volumioIp != null) this.volumioIp = jsonConfig.volumioIp;

        if (jsonConfig.hiveBroker != null) this.hiveBroker = jsonConfig.hiveBroker;
        if (jsonConfig.hiveUsername != null) this.hiveUsername = jsonConfig.hiveUsername;
        if (jsonConfig.hivePassword != null) this.hivePassword = jsonConfig.hivePassword;

        if (jsonConfig.yandexToken != null) this.yandexToken = jsonConfig.yandexToken;
        if (jsonConfig.yandexName != null) this.yandexName = jsonConfig.yandexName;
        if (jsonConfig.yandexUid != null) this.yandexUid = jsonConfig.yandexUid;

        this.spotifyToken = jsonConfig.spotifyToken;
        this.spotifyName = jsonConfig.spotifyName;
        this.spotifyRefreshToken = jsonConfig.spotifyRefreshToken;
        this.spotifyTokenExpiresAt = jsonConfig.spotifyTokenExpiresAt;
        log.debug("CONFIG: " + config);
    }

    @Override
    public String toString() {
        return "\n" +
                "port=" + port + "\n" +
                "domain=" + domain + "\n" +
                "zoneId=" + zoneId + "\n" +

                "lmsIp=" + lmsIp + "\n" +
                "lmsPort=" + lmsPort + "\n" +
                "lmsUrl=" + lmsUrl + "\n" +
                "silence=" + silence + "\n" +

                "volumioIp=" + volumioIp + "\n" +

                "hiveBroker=" + hiveBroker + "\n" +
                "hiveUsername=" + hiveUsername + "\n" +
                "hivePassword=" + Mask.mask(hivePassword, 40) + "\n" +

                "yandexToken=" + Mask.mask(yandexToken, 70) + "\n" +
                "yandexName=" + yandexName + "\n" +
                "yandexUid=" + yandexUid + "\n" +

                "spotifyToken=" + Mask.mask(spotifyToken, 80) + "\n" +
                "spotifyRefreshToken=" + spotifyRefreshToken + "\n" +
                "spotifyTokenExpiresAt=" + spotifyTokenExpiresAt + "\n" +
                "spotifyName=" + spotifyName;
    }

    public void write() {
        log.info("WRITE data/config.json");
        JsonUtils.pojoToJsonFile(this, "data/config.json");
    }
}