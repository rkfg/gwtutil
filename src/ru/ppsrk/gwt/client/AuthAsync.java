package ru.ppsrk.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AuthAsync {

    void login(String username, String password, boolean remember, AsyncCallback<Boolean> callback);

    void register(String username, String password, AsyncCallback<Boolean> callback);

    void logout(AsyncCallback<Void> callback);

    void isRegistrationEnabled(AsyncCallback<Boolean> callback);

    void isLoggedIn(AsyncCallback<Boolean> callback);

}
