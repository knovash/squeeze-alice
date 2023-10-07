package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.knovash.squeezealice.Utils.isLms;

@Log4j2
public class Ping {

    public static String go(Integer index) {
        String lmsip = "192.168.1.52";
        String ip = "192.168.1." + index;
        log.info("PING " + index);
        if (lmsip.equals(ip)) return ip;
        return null;
    }

    public static String ipIsReachable(String fullIp, Integer index) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(fullIp);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        byte[] ip = inetAddress.getAddress();
        ip[3] = Byte.parseByte(String.valueOf(index));
        String ipTry = null;
        try {
            InetAddress address = InetAddress.getByAddress(ip);
            ipTry = address.toString().substring(1);
//            log.info("TRY IP... " + ipTry);
            if (address.isReachable(1000) && isLms(ipTry)) {
                log.info("IP IS LMS: " + ipTry);
                return ipTry;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
