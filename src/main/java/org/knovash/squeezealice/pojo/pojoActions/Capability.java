package org.knovash.squeezealice.pojo.pojoActions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.knovash.squeezealice.provider.pojoUserDevices.Parameters;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Capability {
    public String type;
    public State state;
    public Boolean retrievable = true; // Доступен ли для данного умения устройства запрос состояния
    public Boolean reportable = false; // Признак включенного оповещения об изменении состояния умения при помощи сервиса уведомлений
    public Parameters parameters;
}
