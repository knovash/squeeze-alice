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
    public String lmsIp;
    public String lmsPort;
    public String lmsUrl;
    public String silence;
    public String domain;
    public ZoneId zoneId;

    public String hiveBroker;
    public String hiveUsername;
    public String hivePassword;
    public String hiveUserId;
    public String hiveYandexEmail;

    public void readConfigProperties() {
        log.debug("READ CONFIG FROM config.properties");
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        this.port = Integer.parseInt(bundle.getString("port"));
        this.lmsIp = bundle.getString("lmsIp");
        this.lmsPort = bundle.getString("lmsPort");
        this.silence = bundle.getString("silence");
        this.domain = bundle.getString("domain");
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";

        this.hiveBroker = bundle.getString("hiveBroker");
        this.hiveUsername = bundle.getString("hiveUsername");
        this.hivePassword = bundle.getString("hivePassword");
        this.hiveUserId = bundle.getString("hiveUserId");
        log.info("CONFIG FROM config.properties : " + config);
    }

    public void readConfigJson() {
        log.debug("READ CONFIG FROM config.json");
        Config jsonConfig = JsonUtils.jsonFileToPojo("config.json", Config.class);
        if(jsonConfig == null){
            log.info("NO FILE config.json WRITE NEW");
            config.writeConfig();
            return;}

        this.port = jsonConfig.port;
        this.lmsIp = jsonConfig.lmsIp;
        this.lmsPort = jsonConfig.lmsPort;
        this.silence = jsonConfig.silence;
        this.domain = jsonConfig.domain;
        this.zoneId = jsonConfig.zoneId;
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";

        this.hiveBroker = jsonConfig.hiveBroker;
        this.hiveUsername = jsonConfig.hiveUsername;
        this.hivePassword = jsonConfig.hivePassword;
        this.hiveUserId = jsonConfig.hiveUserId;
        log.info("CONFIG FROM config.json : " + config);
    }

    @Override
    public String toString() {
        return "Config {" + "\n" +
                " port = " + port + "\n" +
                " lmsIp = " + lmsIp + "\n" +
                " lmsPort = " + lmsPort + "\n" +
                " lmsUrl = " + "http://" + config.lmsIp + ":" + config.lmsPort + "/jsonrpc.js/" + "\n" +
                " silence = " + silence + "\n" +
                " domain = " + domain + "\n" +
                " hiveBroker = " + hiveBroker + "\n" +
                " hiveUsername = " + hiveUsername + "\n" +
                " hivePassword = " + hivePassword + "\n" +
                " hiveUserId = " + hiveUserId + "\n" +
                '}';
    }

    public void writeConfig() {
        log.info("WRITE CONFIG TO config.json");
        JsonUtils.pojoToJsonFile(this, "config.json");
    }
}