package ru.ppsrk.gwt.test.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.ppsrk.gwt.domain.NestedSetNodeNG;

@SuppressWarnings("serial")
@Entity
@Table(name = "terrdeptsNG")
public class DeptNG extends NestedSetNodeNG {
    String name;
    String dislocation;

    public DeptNG() {
    }

    public DeptNG(String name, String dislocation) {
        this.name = name;
        this.dislocation = dislocation;
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

    @Override
    public String toString() {
        return "Dept [name=" + name + ", dislocation=" + dislocation + ", left=" + getLeftNum() + ", right=" + getRightNum() + "]";
    }

}
