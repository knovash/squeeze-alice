package org.knovash.squeezealice.pojo.spotify_pojo.spotifyalbums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Albums {

    public String href;
    public ArrayList<Item> items;
    public int limit;
    public String next;
    public int offset;
    public Object previous;
    public int total;
}