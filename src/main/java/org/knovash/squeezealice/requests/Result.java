package org.knovash.squeezealice.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    public String _volume;
    public String _count;
    public String _mode;
    public String _path;
    public String _name;
    public String _artist;
    public String _id;
    public String _syncgroups;
}
