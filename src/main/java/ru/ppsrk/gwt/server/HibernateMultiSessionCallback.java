package ru.ppsrk.gwt.server;

import org.hibernate.Session;

import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.LogicException;

public interface HibernateMultiSessionCallback<T> {
    T run(Session[] sessions) throws LogicException, ClientAuthException;
}
