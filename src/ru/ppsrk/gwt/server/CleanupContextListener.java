package ru.ppsrk.gwt.server;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CleanupContextListener implements ServletContextListener {
    @SuppressWarnings("deprecation")
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Cleaning up drivers in listener...");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            ClassLoader driverclassLoader = driver.getClass().getClassLoader();
            ClassLoader thisClassLoader = this.getClass().getClassLoader();
            if (driverclassLoader != null && thisClassLoader != null && driverclassLoader.equals(thisClassLoader)) {
                try {
                    System.out.println("Deregistering: " + driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread t : threadArray) {
            String threadName = t.getName();
            if (threadName.contains("Abandoned connection cleanup thread")) {
                synchronized (t) {
                    System.out.println("Killing " + t + " [" + threadName + "]");
                    t.stop(); // don't complain, it works
                }
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

}