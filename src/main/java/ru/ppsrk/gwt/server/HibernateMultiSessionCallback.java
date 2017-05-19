package ru.ppsrk.gwt.server;

import org.hibernate.Session;

import ru.ppsrk.gwt.client.GwtUtilException;

public interface HibernateMultiSessionCallback<T> {
    T run(Session[] sessions) throws GwtUtilException;
}
