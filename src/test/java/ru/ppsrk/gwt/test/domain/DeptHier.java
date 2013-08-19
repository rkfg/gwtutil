package ru.ppsrk.gwt.test.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "depthier")
public class DeptHier {
    @Id
    @GeneratedValue
    Long id;
    String name;
    String dislocation;
    @ManyToOne(targetEntity = DeptHier.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    DeptHier parent;
    Boolean root;

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

    public DeptHier getParent() {
        return parent;
    }

    public void setParent(DeptHier parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return String.format("Id: %d Name: %s Parent: %d", id, name, parent == null ? null : parent.getId());
    }

    public Boolean getRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

}
