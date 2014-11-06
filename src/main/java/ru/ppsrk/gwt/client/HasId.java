package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface HasId extends IsSerializable {

    public Long getId();

    public void setId(Long newId);

}
