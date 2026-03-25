package org.knovash.squeezealice.volumio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VolumioSystemInfoResponse {
    private String systemversion;
    private String hardware;
    private String uptime;
}