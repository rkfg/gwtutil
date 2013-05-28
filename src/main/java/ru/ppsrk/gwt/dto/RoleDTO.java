package ru.ppsrk.gwt.dto;

import java.io.Serializable;


public class RoleDTO implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 7122635498746563558L;
    Long id;
    UserDTO user;
    String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
