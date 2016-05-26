package ru.ppsrk.gwt.server.nestedset;

import org.hibernate.Session;

import ru.ppsrk.gwt.client.SettableParent;

public class NSMFactory {
    
    public NSMFactory() {
    }

    public <T extends NestedSetNode, D extends SettableParent> NestedSetManagerTS<T, D> createNSM(Class<T> entityClass,
            Class<D> dtoClass, Session session) {
        return new NestedSetManagerTS<T, D>(entityClass, dtoClass, session, this);
    }
    
    public <T extends NestedSetNodeNG> NestedSetManagerNG<T> createNSM(Class<T> entityClass, Session session) {
        return new NestedSetManagerNG<T>(entityClass, session, this);
    }
}