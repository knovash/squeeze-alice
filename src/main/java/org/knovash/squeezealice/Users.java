package org.knovash.squeezealice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    public List<User> users;
}
