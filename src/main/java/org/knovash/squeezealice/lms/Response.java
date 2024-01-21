package org.knovash.squeezealice.lms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {

    public List<Object> params;
    public Result result;
    public String id;
    public String method;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Result {

        public String _volume;
        public String _count;
        public String _mode;
        public String _path;
        public String _name;
        public String _title;
        public String _album;
        public String _artist;
        public String _id;
        public String _syncgroups;
    }
}
