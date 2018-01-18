package ru.ppsrk.gwt.test.domain;

import javax.persistence.Entity;

import ru.ppsrk.gwt.domain.SCDBase;
import ru.ppsrk.gwt.server.TemporalManager;

@SuppressWarnings("serial")
@Entity
public class TestEntity extends SCDBase {
    String name;

    public TestEntity() {
    }

    public TestEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Object getUniqValue() {
        return getName();
    }

    @Override
    public String toString() {
        return String.format(TemporalManager.DATE_FORMAT + ": %3$s", getStartDate(), getName());
    }

}
