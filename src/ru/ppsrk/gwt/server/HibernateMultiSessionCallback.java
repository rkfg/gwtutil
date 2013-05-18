package ru.ppsrk.gwt.server;

import org.hibernate.Session;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public interface HibernateMultiSessionCallback<T> {
    T run(Session[] sessions) throws LogicException, ClientAuthenticationException;
}
