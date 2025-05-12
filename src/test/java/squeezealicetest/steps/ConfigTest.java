package squeezealicetest.steps;

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
public class ConfigTest {

    //    Squeeze-Alice
    public int port =8010; // порт этого сервера
    public String domain="https://alice-lms.zeabur.app"; // для колбэка но в локальном ненужно
    public ZoneId zoneId; // для установки часового пояса пользователя
    //    Lyrion Music Server
    public String lmsIp = "192.168.1.110"; // адрес LMS
    public String lmsPort="9000"; // порт LMS обычно 9000
    public String lmsUrl="http://192.168.1.110:9000/jsonrpc.js/"; // урл LMS для запросов jsonrpc.js
    public String silence="loop://natural/rain_outside.mp3"; // урл для звука тишины перед включением музыки
    //    Hive
    public String hiveBroker="ssl://811c56b338f24aeea3215cd680851784.s1.eu.hivemq.cloud:8883";
    public String hiveUsername="novashki";
    public String hivePassword="Darthvader0";
    //    Yandex
    public String yandexToken ="y0__xDzxbXDARi79i4g1Kq52BLBrH5AiuK_6jAmQvamADVB964geA"; // для получения списка комнат в умном доме
    public String yandexName="Константин Н."; // для отображения кто вошел
    public String yandexUid="409821939"; // для уникального названия топика пользователя
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
        ConfigTest jsonConfig = JsonUtils.jsonFileToPojo("data/config.json", ConfigTest.class);
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

    public void write() {
        log.info("WRITE CONFIG TO config.json");
        JsonUtils.pojoToJsonFile(this, "data/config.json");
    }
}