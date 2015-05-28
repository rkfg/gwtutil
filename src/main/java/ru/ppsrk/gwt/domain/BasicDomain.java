package ru.ppsrk.gwt.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import ru.ppsrk.gwt.client.HasId;

@SuppressWarnings("serial")
@MappedSuperclass
public class BasicDomain implements HasId {
    @Id
    @GeneratedValue
    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
