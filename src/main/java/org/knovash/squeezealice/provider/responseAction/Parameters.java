package org.knovash.squeezealice.provider.responseAction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {

    public String instance;
    public Range range = null;
    public List<Mode> modes = new ArrayList<>();
//    public boolean random_access = true;
//    public boolean split = false; // Параметр используется совместно с retrievable:false и показывает, что за включение/выключение устройства у провайдера отвечают разные команды
}


