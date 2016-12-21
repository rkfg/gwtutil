package ru.ppsrk.gwt.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Login extends PopupPanel {
    private final VerticalPanel verticalPanel = new VerticalPanel();
    private final HorizontalPanel horizontalPanel = new HorizontalPanel();
    private final InlineLabel inlineLabel = new InlineLabel("Имя пользователя");
    private final TextBox textBox_login = new TextBox();
    private final HorizontalPanel horizontalPanel_1 = new HorizontalPanel();
    private final InlineLabel inlineLabel_1 = new InlineLabel("Пароль");
    private final PasswordTextBox textBox_password = new PasswordTextBox();
    private final HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
    private final Button button_login = new Button("Вход");
    private final AuthServiceAsync authservice = AuthService.Util.getInstance();
    private final Button button_register = new Button("Регистрация");
    private final HorizontalPanel horizontalPanel_3 = new HorizontalPanel();
    private final CheckBox checkBox_remember = new CheckBox((String) null);

    public Login(boolean rememberMe, boolean showRememberMe) {
        super(false, true);
        setWidget(verticalPanel);
        verticalPanel.setSpacing(5);
        horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        verticalPanel.add(horizontalPanel);
        horizontalPanel.setWidth("100%");
        horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        horizontalPanel.setSpacing(5);
        inlineLabel.setWordWrap(false);

        horizontalPanel.add(inlineLabel);
        horizontalPanel.setCellVerticalAlignment(inlineLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        textBox_login.addKeyPressHandler(new TextBoxKeyPressHandler());

        horizontalPanel.add(textBox_login);
        textBox_login.setWidth("180px");
        textBox_login.setStyleName("login_username");
        horizontalPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        verticalPanel.add(horizontalPanel_1);
        horizontalPanel_1.setSpacing(5);
        horizontalPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        horizontalPanel_1.setWidth("100%");

        horizontalPanel_1.add(inlineLabel_1);
        horizontalPanel_1.setCellWidth(inlineLabel_1, "100%");
        horizontalPanel_1.setCellHorizontalAlignment(inlineLabel_1, HasHorizontalAlignment.ALIGN_RIGHT);
        inlineLabel_1.setWidth("");
        horizontalPanel_1.setCellVerticalAlignment(inlineLabel_1, HasVerticalAlignment.ALIGN_MIDDLE);
        textBox_password.addKeyPressHandler(new TextBox_passwordKeyPressHandler());

        horizontalPanel_1.add(textBox_password);
        textBox_password.setWidth("180px");
        textBox_password.setStyleName("login_password");

        verticalPanel.add(horizontalPanel_3);
        horizontalPanel_3.setWidth("100%");
        checkBox_remember.setWordWrap(false);
        checkBox_remember.setValue(rememberMe);
        checkBox_remember.setHTML("запомнить меня");
        checkBox_remember.setStyleName("rememberme");

        if (showRememberMe) {
            horizontalPanel_3.add(checkBox_remember);
        }
        horizontalPanel_2.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.add(horizontalPanel_2);
        horizontalPanel_2.setSpacing(5);
        horizontalPanel_2.setWidth("100%");
        button_login.addClickHandler(new Button_loginClickHandler());

        horizontalPanel_2.add(button_login);
        horizontalPanel_2.setCellWidth(button_login, "50%");
        button_register.addClickHandler(new Button_registerClickHandler());

        AuthService.Util.getInstance().isRegistrationEnabled(new AsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    horizontalPanel_2.add(button_register);
                    horizontalPanel_2.setCellWidth(button_register, "50%");
                    horizontalPanel_2.setCellHorizontalAlignment(button_register, HasHorizontalAlignment.ALIGN_RIGHT);
                } else {
                    horizontalPanel_2.setCellHorizontalAlignment(button_login, HasHorizontalAlignment.ALIGN_CENTER);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }
        });
    }

    @Override
    public void center() {
        setGlassEnabled(true);
        setAnimationEnabled(true);
        super.center();
        textBox_login.setFocus(true);
    }

    private class TextBoxKeyPressHandler implements KeyPressHandler {
        public void onKeyPress(KeyPressEvent event) {
            if (event.getNativeEvent().getKeyCode() == 13)
                button_login.click();
        }
    }

    private class TextBox_passwordKeyPressHandler implements KeyPressHandler {
        public void onKeyPress(KeyPressEvent event) {
            if (event.getNativeEvent().getKeyCode() == 13)
                button_login.click();
        }
    }

    private class Button_loginClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean result) {
                    if (result) {
                        hide();
                        Window.Location.reload();
                    } else {
                        textBox_password.setText("");
                        Window.alert("Неверное имя пользователя или пароль.");
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    textBox_password.setText("");
                    System.out.println(caught.getMessage());
                    Window.alert("Неверное имя пользователя или пароль.");
                }
            };
            authservice.login(textBox_login.getText(), textBox_password.getText(), checkBox_remember.getValue(), callback);
        }
    }

    private class Button_registerClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            authservice.register(textBox_login.getText(), textBox_password.getText(), new AsyncCallback<Long>() {

                @Override
                public void onSuccess(Long result) {
                    if (result > 0) {
                        Window.alert("Вы успешно зарегистрированы!");
                    } else {
                        Window.alert("Ошибка регистрации.");
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                }
            });
        }
    }
}
