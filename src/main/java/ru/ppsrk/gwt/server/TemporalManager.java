package ru.ppsrk.gwt.server;

import static ru.ppsrk.gwt.server.HibernateUtil.*;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.domain.SCDBase;

public class TemporalManager<T extends SCDBase> {

    private static final String START_DATE = "startDate";
    public static final String DATE_FORMAT = "%1$td-%1$tm-%1$tY";
    public static final Date MAX_DATE = new Date(Long.MAX_VALUE);

    private Session session;
    private Class<T> clazz;
    private String uniqField;

    /**
     * Create a temporal manager
     * @param session Hibernate session to use
     * @param clazz entity class
     * @param uniqField name of the field that allows to distinguish between histories (for example, a person's name) 
     */
    public TemporalManager(Session session, Class<T> clazz, String uniqField) {
        this.session = session;
        this.clazz = clazz;
        this.uniqField = uniqField;
    }

    @SuppressWarnings("unchecked")
    public synchronized void save(T element) throws LogicException {
        try {
            T priorObject = (T) makeCriteria(element.getUniqValue()).add(Restrictions.eq(START_DATE, element.getStartDate()))
                    .uniqueResult();
            if (priorObject != null) {
                session.delete(priorObject);
            }
            session.saveOrUpdate(element);
        } catch (NonUniqueResultException e) {
            throw LogicExceptionFormatted.format("Non-unique result when searching for objects at " + DATE_FORMAT, element.getStartDate());
        }
    }

    public synchronized void delete(Long id) throws LogicException {
        T element = tryGetObject(id, clazz, session, "object with id " + id + " not found.");
        session.delete(element);
    }

    @SuppressWarnings("unchecked")
    public List<T> list(Date from, Date to, Object uniqValue) throws LogicException {
        Criteria crit = makeCriteria(uniqValue).addOrder(Order.asc(START_DATE));
        T fromEntity = get(from, uniqValue);
        if (fromEntity != null) {
            crit.add(Restrictions.ge(START_DATE, fromEntity.getStartDate()));
        }
        T toEntity = get(to, uniqValue);
        if (toEntity != null) {
            crit.add(Restrictions.le(START_DATE, toEntity.getStartDate()));
        }
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public T get(Date at, Object uniqValue) throws LogicException {
        return (T) makeCriteria(uniqValue).add(Restrictions.le(START_DATE, at)).addOrder(Order.desc(START_DATE)).setMaxResults(1)
                .uniqueResult();
    }

    private Criteria makeCriteria(Object uniqValue) {
        return session.createCriteria(clazz).add(Restrictions.eq(uniqField, uniqValue));
    }
}
