package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumioResponse {
    private boolean success;
    private String status;
    private String message;
}