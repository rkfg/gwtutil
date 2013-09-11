package ru.ppsrk.gwt.domain;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import ru.ppsrk.gwt.client.Hierarchic;

@Entity
@Table(name = "users")
public class User implements Hierarchic {
    @Id
    @GeneratedValue
    Long id;
    String username;
    String password;
    String salt;
    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    Set<Role> roles;

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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public Hierarchic getParent() {
        return null;
    }
}
