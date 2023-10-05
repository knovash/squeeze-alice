package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;
import org.knovash.squeezealice.provider.pojo.Device;
import org.knovash.squeezealice.provider.pojo.Payload;
import org.knovash.squeezealice.provider.pojo.ResponseQuery;


@Log4j2
public class MainTest {

    public static void main(String[] args) {
        log.info("  ---+++===[ START ]===+++---");

        ResponseQuery responseQuery = new ResponseQuery();
        responseQuery.request_id = "33";
        Device device1 = new Device();
        device1.id = "11";
        Device device2 = new Device();
        device2.id = "22";
        log.info(device1);
        log.info(device2);
        Device[] dd = new Device[2];
        dd[0] =device1;
        dd[1] =device2;
        log.info(dd[0]);
        Payload payload = new Payload();
        payload.devices = dd;
        log.info(payload);
        responseQuery.payload = payload;
        log.info(responseQuery);




    }
}