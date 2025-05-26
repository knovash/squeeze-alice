package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    public String id; // 15497314-8ca8-4a50-8a16-2ad588da828f
    public String idExt; // 1
    public String roomId; // 14c6cd6d-716f-45b6-b133-16cee528e305
    public String roomName; // Гостиная
    public String playerName; // Homepod1

    @Override
    public String toString() {
        return "\nLink{" +
                "id='" + id + '\'' +
                ", idExt='" + idExt + '\'' +
                ", roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}