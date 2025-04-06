package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;

import java.time.ZoneId;
import java.util.ResourceBundle;

import static org.knovash.squeezealice.Main.config;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    public int port;
    public String domain;
    public String lmsIp;
    public String lmsPort;
    public String lmsUrl;
    public String silence;
    public ZoneId zoneId;
    public String hiveBroker;
    public String hiveUsername;
    public String hivePassword;
    public String yandexToken;
    public String yandexUid;
    public String spotifyToken;

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
        this.lmsIp = jsonConfig.lmsIp;
        this.lmsPort = jsonConfig.lmsPort;
        this.silence = jsonConfig.silence;
        this.zoneId = jsonConfig.zoneId;
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
        this.hiveBroker = jsonConfig.hiveBroker;
        this.hiveUsername = jsonConfig.hiveUsername;
        this.hivePassword = jsonConfig.hivePassword;
        this.yandexToken = jsonConfig.yandexToken;
        this.yandexUid = jsonConfig.yandexUid;
        this.spotifyToken = jsonConfig.spotifyToken;
        log.info("CONFIG FROM config.json : " + config);
    }

    @Override
    public String toString() {
        return "\n" +
                "port=" + port + "\n" +
                "domain=" + domain + "\n" +
                "lmsIp=" + lmsIp + "\n" +
                "lmsPort=" + lmsPort + "\n" +
                "lmsUrl=" + lmsUrl + "\n" +
                "silence=" + silence + "\n" +
                "zoneId=" + zoneId + "\n" +
                "hiveBroker=" + hiveBroker + "\n" +
                "hiveUsername=" + hiveUsername + "\n" +
                "hivePassword=" + hivePassword + "\n" +
                "yandexToken=" + yandexToken + "\n" +
                "yandexUid=" + yandexUid + "\n" +
                "spotifyToken=" + spotifyToken;
    }

    public void writeConfig() {
        log.info("WRITE CONFIG TO config.json");
        JsonUtils.pojoToJsonFile(this, "data/config.json");
//        JsonUtils.pojoToJsonFile(this, "config/config.json");
//        JsonUtils.pojoToJsonPathFile(this, "","config.json");
    }
}