package ru.ppsrk.gwt.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

import ru.ppsrk.gwt.client.event.ReloadDataEvent.ReloadDataHandler;

public class ReloadDataEvent extends GwtEvent<ReloadDataHandler> {

    public interface ReloadDataHandler extends EventHandler {
        public void onReloadData(ReloadDataEvent event);
    }

    public static final Type<ReloadDataHandler> TYPE = new Type<>();
    public Long groupId;

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ReloadDataHandler> getAssociatedType() {
        return TYPE;
    }

    public ReloadDataEvent() {
    }
    
    public ReloadDataEvent(Long groupId) {
        this.groupId = groupId;
    }
    
    @Override
    protected void dispatch(ReloadDataHandler handler) {
        handler.onReloadData(this);
    }

}
