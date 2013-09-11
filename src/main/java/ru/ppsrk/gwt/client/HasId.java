package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public interface HasId extends IsSerializable {

    public abstract Long getId();

    public abstract void setId(Long newId);

}
