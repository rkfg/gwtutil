package ru.ppsrk.gwt.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

public class CleanupContextListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        /*
         * Enumeration<Driver> drivers = DriverManager.getDrivers(); while
         * (drivers.hasMoreElements()) { Driver driver = drivers.nextElement();
         * ClassLoader driverclassLoader = driver.getClass().getClassLoader();
         * ClassLoader thisClassLoader = this.getClass().getClassLoader(); if
         * (driverclassLoader != null && thisClassLoader != null &&
         * driverclassLoader.equals(thisClassLoader)) { try {
         * System.out.println("Deregistering: " + driver);
         * DriverManager.deregisterDriver(driver); } catch (SQLException e) {
         * e.printStackTrace(); } } }
         */

        System.out.println("Cleaning up abandoned connection threads in listener...");
        try {
            AbandonedConnectionCleanupThread.shutdown();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        HibernateUtil.cleanup();
        ServerUtils.cleanup();
        System.out.println("Done cleaning.");

        /*
         * Set<Thread> threadSet = Thread.getAllStackTraces().keySet(); Thread[]
         * threadArray = threadSet.toArray(new Thread[threadSet.size()]); for
         * (Thread t : threadArray) { String threadName = t.getName(); if
         * (threadName.contains("Abandoned connection cleanup thread")) {
         * synchronized (t) { System.out.println("Killing " + t + " [" +
         * threadName + "]"); t.stop(); // don't complain, it works } } }
         */
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
//        try {
//            final ClassLoader active = Thread.currentThread().getContextClassLoader();
//            try {
//                // Find the root classloader
//                ClassLoader root = active;
//                while (root.getParent() != null) {
//                    root = root.getParent();
//                }
//                // Temporarily make the root class loader the active class
//                // loader
//                Thread.currentThread().setContextClassLoader(root);
//                // Force the AppContext singleton to be created and initialized
//                sun.awt.AppContext.getAppContext();
//            } finally {
//                // restore the class loader
//                Thread.currentThread().setContextClassLoader(active);
//            }
//        } catch (Throwable t) {
//            // Carry on if we get an error
//            System.out.println("Failed to address PermGen leak");
//        }
    }

}