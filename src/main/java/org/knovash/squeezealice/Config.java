package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.ZoneId;
import java.util.ResourceBundle;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    private static final String CONFIG_JSON_PATH = "data/config.json";

    public int port;
    public String domain;
    public ZoneId zoneId;

    public String lmsIp;
    public String lmsPort;
    public String silence;

    public String volumioIp;

    public String hiveBroker;
    public String hiveUsername;
    @ToString.Exclude
    public String hivePassword;

    @ToString.Exclude
    public String yandexToken;
    public String yandexName;
    public String yandexUid;
    public String skillId;
    public String yandextSkillTokenDeveloper;

    @ToString.Exclude
    public String spotifyToken;
    public String spotifyName;
    @ToString.Exclude
    public String spotifyRefreshToken;
    public long spotifyTokenExpiresAt;

    public String yandexSstTttsApiKey;
    public  String scenarioId;

    public String fileRoomsAndAliceIds = "data/rooms_and_alice_ids.json";
    public String fileRoomsAndPlayers = "data/rooms_and_players.json";
    public String fileDevices = "data/devices.json";
    public String fileLmsPlayers = "data/lms_players.json";

    public int volumeVoicePlus = 20;

    public void load() {
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        this.port = Integer.parseInt(bundle.getString("port"));
        this.domain = bundle.getString("domain");
        this.lmsIp = bundle.getString("lmsIp");
        this.lmsPort = bundle.getString("lmsPort");
        this.volumioIp = bundle.getString("volumioIp");
        this.silence = bundle.getString("silence");
        this.hiveBroker = bundle.getString("hiveBroker");
        this.hiveUsername = bundle.getString("hiveUsername");
        this.hivePassword = bundle.getString("hivePassword");
        this.skillId = bundle.getString("skillId");
        this.yandextSkillTokenDeveloper = bundle.getString("yandextSkillTokenDeveloper");
        this.yandexSstTttsApiKey = bundle.getString("yandexSstTttsApiKey");
        this.fileRoomsAndAliceIds = bundle.getString("fileRoomsAndAliceIds");
        this.fileRoomsAndPlayers = bundle.getString("fileRoomsAndPlayers");
        this.fileDevices = bundle.getString("fileDevices");
        this.fileLmsPlayers = bundle.getString("fileLmsPlayers");
        this.volumeVoicePlus = Integer.parseInt(bundle.getString("volumeVoicePlus"));

        Config jsonConfig = JsonUtils.jsonFileToPojo(CONFIG_JSON_PATH, Config.class);
        if (jsonConfig != null) {
            copyNonNullFields(jsonConfig, this);
        } else {
            log.info("Файл {} не найден, создаём новый", CONFIG_JSON_PATH);
            write();
        }
    }

    private void copyNonNullFields(Config source, Config target) {
        Field[] fields = Config.class.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (IllegalAccessException e) {
                log.warn("Не удалось скопировать поле {}", field.getName(), e);
            }
        }
    }

    public void write() {
        log.info("Запись конфигурации в {}", CONFIG_JSON_PATH);
        JsonUtils.pojoToJsonFile(this, CONFIG_JSON_PATH);
    }
}