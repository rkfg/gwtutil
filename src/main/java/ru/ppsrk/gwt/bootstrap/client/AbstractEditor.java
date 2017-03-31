package ru.ppsrk.gwt.bootstrap.client;

public abstract class AbstractEditor<T> extends ModalDialogAdapter<T> {
    
    private T existing;

    public AbstractEditor(T initialValue) {
        if (initialValue == null) {
            initialValue = create();
        }
        this.existing = initialValue;
        init(initialValue);
    }

    @Override
    protected T getResult() {
        fillResult(existing);
        return existing;
    }
    
    protected abstract void fillResult(T result);

    protected abstract void init(T initialValue);

    protected abstract T create();
}
