package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class Hierarchic implements IsSerializable {

    protected boolean equalsById = true;

    public abstract Hierarchic getParent();

    public abstract Long getId();

    public abstract void setId(Long newId);

    @Override
    public boolean equals(Object obj) {
        if (equalsById && obj instanceof Hierarchic) {
            return this.getId() == null && obj != null && ((Hierarchic) obj).getId() == null && this.getClass().equals(obj.getClass()) || obj != null
                    && this.getId() != null && this.getId().equals(((Hierarchic) obj).getId()) && this.getClass().equals(obj.getClass());
        }
        return super.equals(obj);
    }
}
