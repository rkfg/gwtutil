package ru.ppsrk.gwt.bootstrap.client;

public abstract class AbstractEditor<T> extends ModalDialogAdapter<T> {
    
    private T existing;

    public AbstractEditor(T value) {
        if (value == null) {
            value = create();
        }
        this.existing = value;
        initFields(value);
    }

    @Override
    protected T getResult() {
        fillValue(existing);
        return existing;
    }
    
    protected abstract void fillValue(T value);

    protected abstract void initFields(T value);

    protected abstract T create();
}
