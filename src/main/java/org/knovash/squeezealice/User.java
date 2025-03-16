package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public String userName;
    public String userLogin;
    public String userPassword;
    public String userTopic;
}
