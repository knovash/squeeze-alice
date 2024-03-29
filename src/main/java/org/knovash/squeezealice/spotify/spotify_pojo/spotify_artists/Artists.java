package org.knovash.squeezealice.spotify.spotify_pojo.spotify_artists;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artists {
    public String href;
    public ArrayList<Item> items;
    public int limit;
    public String next;
    public int offset;
    public Object previous;
    public int total;
}
