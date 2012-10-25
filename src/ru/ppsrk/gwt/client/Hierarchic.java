package ru.ppsrk.gwt.client;

public abstract class Hierarchic {
    public abstract Hierarchic getParent();

    public abstract Long getId();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hierarchic) {
            return this.getId().equals(((Hierarchic) obj).getId()) && this.getClass().equals(obj.getClass());
        }
        return super.equals(obj);
    }

}
