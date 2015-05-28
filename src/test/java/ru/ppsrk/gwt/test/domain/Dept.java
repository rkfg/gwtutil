package ru.ppsrk.gwt.test.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.ppsrk.gwt.server.nestedset.NestedSetNode;

@SuppressWarnings("serial")
@Entity
@Table(name = "terrdepts")
public class Dept extends NestedSetNode {
    String name;
    String dislocation;

    public Dept() {
        super();
    }

    public Dept(String name, String dislocation) {
        super();
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
