package ru.ppsrk.gwt.client;

import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ImpersonationWidget extends Composite {

    private static ImpersonationWidgetUiBinder uiBinder = GWT.create(ImpersonationWidgetUiBinder.class);

    @UiField
    TextBox tb_impersonate;

    interface ImpersonationWidgetUiBinder extends UiBinder<Widget, ImpersonationWidget> {
    }

    interface LegacyImpersonationWidgetUiBinder extends UiBinder<Widget, ImpersonationWidget> {
    }

    public ImpersonationWidget() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("b_impersonate")
    public void onImpersonateClick(ClickEvent e) {
        AuthService.Util.getInstance().impersonate(tb_impersonate.getValue(), new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                Location.reload();
            }

            @Override
            public void onSuccess(Void result) {
                Location.reload();
            }

        });
    }

    @UiHandler("tb_impersonate")
    public void onKeyPress(KeyPressEvent e) {
        if (e.getNativeEvent().getCharCode() == KeyCodes.KEY_ENTER) {
            onImpersonateClick(null);
        }
    }
}
