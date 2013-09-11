package ru.ppsrk.gwt.client;

public abstract class EqualsById implements HasId {

    protected boolean equalsById = true;

    @Override
    public boolean equals(Object obj) {
        if (equalsById && obj instanceof Hierarchic) {
            return this.getId() == null && obj != null && ((Hierarchic) obj).getId() == null && this.getClass().equals(obj.getClass()) || obj != null
                    && this.getId() != null && this.getId().equals(((Hierarchic) obj).getId()) && this.getClass().equals(obj.getClass());
        }
        return super.equals(obj);
    }

}
