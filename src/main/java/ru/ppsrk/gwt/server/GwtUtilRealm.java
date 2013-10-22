package ru.ppsrk.gwt.server;

import java.util.List;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.realm.AuthorizingRealm;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.dto.UserDTO;

public abstract class GwtUtilRealm extends AuthorizingRealm {
    public abstract boolean login(final String username, String password, boolean remember) throws ClientAuthenticationException, ClientAuthorizationException,
            LogicException;

    public abstract Long register(final String username, final String password, RandomNumberGenerator rng) throws LogicException, ClientAuthenticationException;

    public abstract List<String> getRoles(String principal) throws LogicException, ClientAuthenticationException;

    public abstract List<String> getPerms(String principal) throws LogicException, ClientAuthenticationException;

    public abstract UserDTO getUser(String principal) throws LogicException, ClientAuthenticationException;
}
