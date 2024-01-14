package org.knovash.squeezealice.provider.pojoUserDevices;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capability {

    public String type;
    public Boolean retrievable = true; // Доступен ли для данного умения устройства запрос состояния
    public Boolean reportable = false; // Признак включенного оповещения об изменении состояния умения при помощи сервиса уведомлений
//    public State state = new State();
public String state;
    public Parameters parameters = new Parameters();
}


