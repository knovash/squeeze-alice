package org.knovash.squeezealice.pojo.lms_pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loop {

    public String id;
    public String name;
    public String type;
    public String image;
    public String url;
    public int isaudio;
    public int hasitems;
}
