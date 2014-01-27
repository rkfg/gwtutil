package ru.ppsrk.gwt.server;

import java.util.List;

import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.realm.AuthorizingRealm;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.dto.UserDTO;

public abstract class GwtUtilRealm extends AuthorizingRealm {
    public abstract boolean login(final String username, String password, boolean remember) throws LogicException, ClientAuthException;

    public abstract Long register(final String username, final String password, RandomNumberGenerator rng) throws LogicException,
            ClientAuthException;

    public abstract List<String> getRoles(String principal) throws LogicException, ClientAuthException;

    public abstract List<String> getPerms(String principal) throws LogicException, ClientAuthException;

    public abstract UserDTO getUser(String principal) throws LogicException, ClientAuthException;
}
