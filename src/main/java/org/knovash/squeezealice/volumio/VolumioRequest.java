package org.knovash.squeezealice.volumio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolumioRequest {
    private String method;   // HTTP-метод: GET, POST
    private String endpoint; // путь, например "/api/v1/getState"
    private Object body;     // тело для POST-запросов
}