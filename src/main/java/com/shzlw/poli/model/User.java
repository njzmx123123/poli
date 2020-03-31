package com.shzlw.poli.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class User {

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String NAME = "name";
    public static final String PASSWORD = "password";
    public static final String TEMP_PASSWORD = "temp_password";
    public static final String SESSION_KEY = "session_key";
    public static final String SESSION_TIMEOUT = "session_timeout";
    public static final String SYS_ROLE = "sys_role";
    public static final String API_KEY = "api_key";
    public static final String TYPE = "type";

    private long id;
    private String username;
    private String name;
    private String password;
    private String tempPassword;
    private String sessionKey;
    private LocalDateTime sessionTimeout;
    private String sysRole;
    /**
     * 0：本地用户
     * 1：UC用户
     */
    private Integer type;
    private String apiKey;

    private List<Long> userGroups = new ArrayList<>();

    private List<UserAttribute> userAttributes = new ArrayList<>();

    public User() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                username.equals(user.username) &&
                sysRole.equals(user.sysRole);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, sysRole);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", sessionTimeout=" + sessionTimeout +
                ", sysRole='" + sysRole + '\'' +
                ", apiKey='" + apiKey + '\'' +
                '}';
    }
}
