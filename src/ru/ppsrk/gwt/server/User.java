package ru.ppsrk.gwt.server;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    Long id;
    String username;
    String password;
    String salt;

    public User() {

    }
    public User(String username, String hashedPasswordBase64) {
        this.username = username;
        this.password = hashedPasswordBase64;
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() {
        return salt;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
