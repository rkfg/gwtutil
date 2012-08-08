package ru.ppsrk.gwt.server;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {

	private static List<SessionFactory> sessionFactory = new ArrayList<SessionFactory>();
	private static ServiceRegistry serviceRegistry;

	public static void initSessionFactory(String cfgFilename){
		try {
			// Create the SessionFactory from hibernate.cfg.xml
			Configuration configuration = new Configuration();
			configuration.configure(cfgFilename);
			serviceRegistry = new ServiceRegistryBuilder().applySettings(
					configuration.getProperties()).buildServiceRegistry();
			sessionFactory.add(configuration.buildSessionFactory(serviceRegistry));
		} catch (Throwable ex) {
			// Make sure you log the exception, as it might be swallowed
			System.err.println("Initial SessionFactory creation failed: " + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static SessionFactory getSessionFactory(int nIndex) {
		return sessionFactory.get(nIndex);
	}
	public static SessionFactory getSessionFactory() {
		return getSessionFactory(0);
	}
	public static Session getSession(int nIndex){
        Session session = getSessionFactory(nIndex).openSession();
        session.beginTransaction();
        return session;
	}
	
    public static Session getSession(){
        return getSession(0);
    }
    public static void endSession(Session session){
        session.getTransaction().commit();
        session.close();
    }
}