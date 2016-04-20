package ru.ppsrk.gwt.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@SuppressWarnings("serial")
@Entity
@Table(name = "role")
public class Role extends BasicDomain {
    @ManyToOne
    User user;
    String role;

    public Role() {

    }

    public Role(User user, String role) {
        this.user = user;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public int hashCode() {
        return getUser().hashCode() * getRole().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Role) {
            return getRole().equals(((Role) obj).getRole());
        }
        return super.equals(obj);
    }
}
