package org.knovash.squeezealice.pojo.alice;
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
