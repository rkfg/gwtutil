package ru.ppsrk.gwt.client;

public abstract class Hierarchic {

    public abstract Hierarchic getParent();

    public abstract Long getId();

    public abstract void setId(Long id);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Hierarchic) {
            return this.getId() == null && obj != null && ((Hierarchic) obj).getId() == null && this.getClass().equals(obj.getClass()) || obj != null
                    && this.getId() != null && this.getId().equals(((Hierarchic) obj).getId()) && this.getClass().equals(obj.getClass());
        }
        return super.equals(obj);
    }

}
