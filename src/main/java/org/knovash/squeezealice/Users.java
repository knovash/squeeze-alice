package org.knovash.squeezealice;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.knovash.squeezealice.Main.config;
import static org.knovash.squeezealice.Main.hiveUsers;

@Log4j2
public class Users {
    private final List<User> users = new ArrayList<>();

    public  boolean createUser(Map<String, String> userData) {
        log.info("GET MAP: " + userData.entrySet());
        log.info("USERS: " + hiveUsers.getUsers().size());
        // Проверка обязательных полей
        if (!userData.containsKey("name")) {
            log.info("ERROR USER NO NAME");
            return false;
        }
        String name = userData.get("name");
        String email = userData.get("email");
        log.info("USER name: " + name);

        // Проверка уникальности
        if (!isUnique(name, email)) {
            return false;
        }

        // Создание пользователя
        User newUser = new User(
                name,
                email,
                userData.getOrDefault("login", null),
                userData.getOrDefault("password", null),
                userData.getOrDefault("topic", null)
        );
        users.add(newUser);

        Hive.hiveUserId = name;
        config.hiveUserId = name;
        config.writeConfig();

        log.info("FINISH USERS: " + hiveUsers.getUsers().size());
        return true;
    }

    private boolean isUnique(String name, String email) {
        for (User user : users) {
            if (user.getName().equals(name) || user.getEmail().equals(email)) {
                return false;
            }
        }
        return true;
    }

    // Метод для получения копии списка пользователей
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }


    class User {
        private final String name;
        private final String email;
        private final String login;
        private final String password;
        private final String topic;

        public User(String name, String email, String login, String password, String topic) {
            this.name = name;
            this.email = email;
            this.login = login;
            this.password = password;
            this.topic = topic;
        }

        // Геттеры
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getLogin() { return login; }
        public String getPassword() { return password; }
        public String getTopic() { return topic; }
    }
}