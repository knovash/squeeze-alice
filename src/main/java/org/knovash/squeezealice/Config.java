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

    //    Squeeze-Alice
    public int port; // порт этого сервера
    public String domain; // для колбэка но в локальном ненужно
    public ZoneId zoneId; // для установки часового пояса пользователя
    //    Lyrion Music Server
    public String lmsIp; // адрес LMS
    public String lmsPort; // порт LMS обычно 9000
    public String lmsUrl; // урл LMS для запросов jsonrpc.js
    public String silence; // урл для звука тишины перед включением музыки
    //    Hive
    public String hiveBroker;
    public String hiveUsername;
    public String hivePassword;
    //    Yandex
    public String yandexToken; // для получения списка комнат в умном доме
    public String yandexName; // для отображения кто вошел
    public String yandexUid; // для уникального названия топика пользователя
    //    Spotify
    public String spotifyToken; // для запросов поиска
    public String spotifyName; // для отображения кто вошел
    public String spotifyRefreshToken; // рефреш токен
    public long spotifyTokenExpiresAt; // системное время когда истекает токен


    public void readConfigProperties() {
        log.debug("READ CONFIG FROM config.properties");
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        this.port = Integer.parseInt(bundle.getString("port"));
        this.domain = bundle.getString("domain");
        this.lmsIp = bundle.getString("lmsIp");
        this.lmsPort = bundle.getString("lmsPort");
        this.silence = bundle.getString("silence");
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
        this.hiveBroker = bundle.getString("hiveBroker");
        this.hiveUsername = bundle.getString("hiveUsername");
        this.hivePassword = bundle.getString("hivePassword");
        log.info("CONFIG FROM config.properties : " + config);
    }

    public void readConfigJson() {
        log.debug("READ CONFIG FROM config.json");
        Config jsonConfig = JsonUtils.jsonFileToPojo("data/config.json", Config.class);
        if (jsonConfig == null) {
            log.info("NO FILE config.json WRITE NEW");
            config.writeConfig();
            return;
        }
        this.port = jsonConfig.port;
        this.domain = jsonConfig.domain;
        this.zoneId = jsonConfig.zoneId;

        this.lmsIp = jsonConfig.lmsIp;
        this.lmsPort = jsonConfig.lmsPort;
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
        this.silence = jsonConfig.silence;

        this.hiveBroker = jsonConfig.hiveBroker;
        this.hiveUsername = jsonConfig.hiveUsername;
        this.hivePassword = jsonConfig.hivePassword;

        this.yandexToken = jsonConfig.yandexToken;
        this.yandexName = jsonConfig.yandexName;
        this.yandexUid = jsonConfig.yandexUid;

        this.spotifyToken = jsonConfig.spotifyToken;
        this.spotifyName = jsonConfig.spotifyName;
        this.spotifyRefreshToken = jsonConfig.spotifyRefreshToken;
        this.spotifyTokenExpiresAt = jsonConfig.spotifyTokenExpiresAt;
        log.info("CONFIG FROM config.json : " + config);
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

    public void writeConfig() {
        log.info("WRITE CONFIG TO config.json");
        JsonUtils.pojoToJsonFile(this, "data/config.json");
    }
}