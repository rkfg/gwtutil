package ru.ppsrk.gwt.dto;

import ru.ppsrk.gwt.client.HasId;

@SuppressWarnings("serial")
public class BasicDTO implements HasId {
    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
