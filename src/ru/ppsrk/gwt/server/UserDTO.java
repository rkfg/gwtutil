package ru.ppsrk.gwt.server;

import java.io.Serializable;
import java.util.Set;

public class UserDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6557702564218055053L;
    Long id;
    String username;
    String password;
    String salt;
    Set<Role> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

}
