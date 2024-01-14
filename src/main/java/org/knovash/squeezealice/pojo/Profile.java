package org.knovash.squeezealice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile{
    public String name2;
    public int age;

    @Override
    public String toString() {
        return "Profile{" +
                "name2='" + name2 + '\'' +
                ", age=" + age +
                '}';
    }
}
