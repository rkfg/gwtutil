package ru.ppsrk.gwt.server;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.domain.SCDBase;

@SuppressWarnings("deprecation")
public class TemporalManager<T extends SCDBase> {

    private static final String END_DATE = "endDate";
    private static final String START_DATE = "startDate";
    public static final String DATE_FORMAT = "%1$td-%1$tm-%1$tY";
    public static final String DATE_FORMAT2 = "%2$td-%2$tm-%2$tY";
    public static Date MAX_DATE = new Date(Long.MAX_VALUE / 1000 * 1000);

    static {
        MAX_DATE.setHours(0);
        MAX_DATE.setMinutes(0);
        MAX_DATE.setSeconds(0);
    }

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
        Criteria crit = null;
        try {
            crit = makeCriteria(element.getUniqValue()).add(Restrictions.gt(END_DATE, element.getStartDate()))
                    .add(Restrictions.le(START_DATE, element.getStartDate()));
            T priorObject = (T) crit.uniqueResult();
            if (priorObject != null) {
                if (priorObject.getStartDate().equals(element.getStartDate())) {
                    session.delete(priorObject);
                }
                element.setEndDate(priorObject.getEndDate());
                priorObject.setEndDate(element.getStartDate());
            } else {
                Date minDate = (Date) makeCriteria(element.getUniqValue()).setProjection(Projections.min(START_DATE)).uniqueResult();
                if (minDate == null) {
                    minDate = MAX_DATE;
                }
                element.setEndDate(minDate);
            }
            session.saveOrUpdate(element);
        } catch (NonUniqueResultException e) {
            throw LogicExceptionFormatted.format("Non-unique result when searching for objects at " + DATE_FORMAT, element.getStartDate());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void delete(Long id) throws LogicException {
        T element = (T) session.get(clazz, id, LockOptions.UPGRADE);
        try {
            DetachedCriteria detcrit = DetachedCriteria.forClass(clazz).add(Restrictions.idEq(id))
                    .setProjection(Projections.property(uniqField));
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq(END_DATE, element.getStartDate()))
                    .add(Subqueries.propertyEq(uniqField, detcrit));
            T priorObject = (T) crit.uniqueResult();
            if (priorObject != null) {
                // prolong the timespan of the object prior to us
                priorObject.setEndDate(element.getEndDate());
            }
            session.delete(element);
        } catch (NonUniqueResultException e) {
            throw LogicExceptionFormatted.format(
                    "Non-unique result when searching for objects between " + DATE_FORMAT + " to " + DATE_FORMAT2, element.getStartDate(),
                    element.getEndDate());
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> list(Date from, Date to, Object uniqValue) {
        Criteria crit = makeCriteria(uniqValue).addOrder(Order.asc(START_DATE));
        if (from != null) {
            crit.add(Restrictions.gt(END_DATE, from));
        }
        if (to != null) {
            crit.add(Restrictions.le(START_DATE, to));
        }
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public T get(Date at, Object uniqValue) throws LogicException {
        Criteria crit = null;
        try {
            crit = makeCriteria(uniqValue).add(Restrictions.le(START_DATE, at)).add(Restrictions.gt(END_DATE, at));
            return (T) crit.uniqueResult();
        } catch (NonUniqueResultException e) {
            throw LogicExceptionFormatted.format("Non-unique result when getting object at " + DATE_FORMAT, at);
        }
    }

    private Criteria makeCriteria(Object uniqValue) {
        return session.createCriteria(clazz).add(Restrictions.eq(uniqField, uniqValue));
    }
}
