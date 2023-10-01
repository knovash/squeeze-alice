package org.knovash.squeezealice.requests.alice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alice {
    public ResponseAlice response;
    public String version;
}