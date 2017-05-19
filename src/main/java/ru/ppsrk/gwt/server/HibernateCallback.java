package ru.ppsrk.gwt.server;

import org.hibernate.Session;

import ru.ppsrk.gwt.client.GwtUtilException;

public interface HibernateCallback<T> {
    T run(Session session) throws GwtUtilException;
}
