package org.knovash.squeezealice.pojo.alice_pojo;

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