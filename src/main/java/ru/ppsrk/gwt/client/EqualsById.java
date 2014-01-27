package ru.ppsrk.gwt.client;

public abstract class EqualsById implements HasId {

    protected boolean equalsById = true;

    @Override
    public boolean equals(Object obj) {
        if (equalsById && obj instanceof HasId) {
            return obj != null && this.getId() != null && this.getId().equals(((HasId) obj).getId())
                    && this.getClass().equals(obj.getClass());
        }
        return super.equals(obj);
    }

}
