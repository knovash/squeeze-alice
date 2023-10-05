package org.knovash.squeezealice.provider.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auth {

    public String scope;
    public String state;
    public String redirect_uri;
    public String response_type;
    public String client_id;
}



//        https://alice.loca.lt/auth?
//        scope=home%3Alights&
//        state=https%3A%2F%2Fsocial.yandex.ru%2Fbroker2%2Fauthz_in_web%2F014a50f4e7a54b73907093cbf5e6896d%2Fcallback&
//        redirect_uri=https%3A%2F%2Fsocial.yandex.net%2Fbroker%2Fredirect&
//        response_type=code&
//        client_id=12345
