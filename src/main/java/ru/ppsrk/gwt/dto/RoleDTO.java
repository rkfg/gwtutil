package ru.ppsrk.gwt.dto;

@SuppressWarnings("serial")
public class RoleDTO extends BasicDTO {
    UserDTO user;
    String role;

    public RoleDTO() {
    }

    public RoleDTO(UserDTO user, String role) {
        this.user = user;
        this.role = role;
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
