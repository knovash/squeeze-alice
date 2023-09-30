package org.knovash.squeezealice.requests;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAlice {
    public String text;
    public boolean end_session;
}
