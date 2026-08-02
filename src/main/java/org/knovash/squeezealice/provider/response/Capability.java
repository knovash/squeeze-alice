package org.knovash.squeezealice.provider.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capability {

    public String type;
    public State state;
    public Boolean retrievable = true; // Доступен ли для данного умения устройства запрос состояния
    public Boolean reportable = true; // Признак включенного оповещения об изменении состояния умения при помощи сервиса уведомлений
    public Parameters parameters = new Parameters();
    private List<Mode> modes;
    public double last_updated;
}


