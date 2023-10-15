package org.knovash.squeezealice.provider.pojo.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.pojo.device.Parameters;
import org.knovash.squeezealice.provider.pojo.device.State;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capability {

//    public String id = null;
    public String type = null;
    public Boolean retrievable = true; // Доступен ли для данного умения устройства запрос состояния
    public Boolean reportable = false; // Признак включенного оповещения об изменении состояния умения при помощи сервиса уведомлений
//    public Float last_updated;
    public State state = new State();
    public Parameters parameters = new Parameters();
}


