package ru.ppsrk.gwt.server;

import org.hibernate.Session;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;

public interface HibernateCallback<T> {
    T run(Session session) throws LogicException, ClientAuthenticationException;
}
