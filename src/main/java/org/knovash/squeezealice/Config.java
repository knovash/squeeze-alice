package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.utils.JsonUtils;

import java.time.ZoneId;
import java.util.ResourceBundle;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {

    public Boolean inCloud;
    public int port;
    public String lmsIp;
    public String lmsPort;
    public String lmsUrl;
    public String silence;
    public String domain;
    public ZoneId zoneId;

    public void readProperties() {
        log.info("READ CONFIG FROM config.properties");
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        this.inCloud = Boolean.valueOf(bundle.getString("inCloud"));
        this.port = Integer.parseInt(bundle.getString("port"));
        this.lmsIp = bundle.getString("lmsIp");
        this.lmsPort = bundle.getString("lmsPort");
        this.silence = bundle.getString("silence");
        this.domain = bundle.getString("domain");
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
    }

    public void readConfigJson() {
        log.info("READ CONFIG FROM config.json");
        Config jsonConfig = JsonUtils.jsonFileToPojo("config.json", Config.class);
        this.inCloud = jsonConfig.inCloud;
        this.port = jsonConfig.port;
        this.lmsIp = jsonConfig.lmsIp;
        this.lmsPort = jsonConfig.lmsPort;
        this.silence = jsonConfig.silence;
        this.domain = jsonConfig.domain;
        this.zoneId = jsonConfig.zoneId;
        this.lmsUrl = "http://" + lmsIp + ":" + lmsPort + "/jsonrpc.js/";
    }

    @Override
    public String toString() {
        return "Config {" + "\n" +
                " inCloud = " + inCloud + "\n" +
                " port = " + port + "\n" +
                " lmsIp = " + lmsIp + "\n" +
                " lmsPort = " + lmsPort + "\n" +
                " lmsUrl = " + lmsUrl + "\n" +
                " silence = " + silence + "\n" +
                " domain = " + domain + "\n" +
                " zoneId = " + zoneId + "\n" +
                '}';
    }

    public void writeConfig() {
        log.info("WRITE CONFIG TO config.json");
        JsonUtils.pojoToJsonFile(this, "config.json");
    }
}