package ru.ppsrk.gwt.bootstrap.client;

import java.util.LinkedList;
import java.util.List;

import ru.ppsrk.gwt.client.AlertRuntimeException;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.Tooltip;
import com.github.gwtbootstrap.client.ui.base.ComplexWidget;
import com.github.gwtbootstrap.client.ui.base.ValueBoxBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.Placement;
import com.github.gwtbootstrap.client.ui.constants.Trigger;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class BootstrapUtils {
    @SuppressWarnings("rawtypes")
    public static void tooltipify(Widget myWidget, String tooltipText) {
        myWidget.getElement().setAttribute("data-original-title", tooltipText);
        Tooltip.configure(myWidget, true, Placement.TOP, Trigger.HOVER, 0, 0, null);
        if (myWidget instanceof ValueBoxBase) {
            ((ValueBoxBase) myWidget).setPlaceholder(tooltipText);
        }
    }

    private static Panel notificationsPanel = new ComplexWidget("div");
    private static List<Alert> alerts = new LinkedList<Alert>();
    private static double gap = 45;

    static {
        RootLayoutPanel.get().add(notificationsPanel);
        notificationsPanel.getElement().getParentElement().getStyle().setZIndex(-10000);
    }
    
    public static void setNotificationsSettings(Panel panel, double alertsGap) {
        notificationsPanel = panel;
        gap = alertsGap;
    }

    public static void showNotification(String text) {
        showNotification(text, 5000, AlertType.INFO);
    }

    public static void showNotification(String text, int timeout, AlertType type) {
        if (notificationsPanel == null) {
            throw new AlertRuntimeException("Не задана панель для вывода уведомлений.");
        }
        final Alert alert = new Alert(text, type);
        Style style = alert.getElement().getStyle();
        style.setPosition(Position.FIXED);
        style.setTop(5, Unit.PX);
        style.setZIndex(10000);
        alert.setAnimation(true);
        for (Alert existingAlert : alerts) {
            String top = existingAlert.getElement().getStyle().getTop();
            top = top.substring(0, top.length() - 2);
            existingAlert.getElement().getStyle().setTop(Double.valueOf(top) + gap, Unit.PX);
        }
        notificationsPanel.add(alert);
        alerts.add(alert);
        style.setLeft(Window.getClientWidth() / 2 - alert.getOffsetWidth() / 2, Unit.PX);
        new Timer() {

            @Override
            public void run() {
                alert.close();
                alerts.remove(alert);
            }
        }.schedule(timeout);
    }

}
