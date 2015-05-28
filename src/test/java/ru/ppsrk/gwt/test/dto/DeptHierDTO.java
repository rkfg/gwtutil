package ru.ppsrk.gwt.test.dto;

import ru.ppsrk.gwt.client.Hierarchic;
import ru.ppsrk.gwt.client.SettableParent;

@SuppressWarnings("serial")
public class DeptHierDTO implements SettableParent {
    Long id;
    String name;
    String dislocation;
    DeptHierDTO parent;
    Boolean root;

    public DeptHierDTO(Long id, String name, DeptHierDTO parent) {
        super();
        this.id = id;
        this.name = name;
        this.parent = parent;
    }

    public DeptHierDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDislocation() {
        return dislocation;
    }

    public void setDislocation(String dislocation) {
        this.dislocation = dislocation;
    }

    public DeptHierDTO getParent() {
        return parent;
    }

    public void setParent(DeptHierDTO parent) {
        this.parent = parent;
    }

    public Boolean getRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    @Override
    public String toString() {
        return String.format("Id: %d Name: %s Parent: %d", id, name, parent == null ? null : parent.getId());
    }

    @Override
    public void setParent(Hierarchic parent) {
        this.parent = (DeptHierDTO) parent;
    }

}
