package ru.ppsrk.gwt.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RoleDTO implements IsSerializable {
    Long id;
    UserDTO user;
    String role;

    public RoleDTO() {
    }

    public RoleDTO(UserDTO user, String role) {
        this.user = user;
        this.role = role;
    }

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
