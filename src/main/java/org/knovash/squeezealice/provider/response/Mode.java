package org.knovash.squeezealice.provider.response;

import lombok.Data;

@Data
public class Mode {
    private String value;

    public Mode(String value) {
        this.value = value;
    }
}