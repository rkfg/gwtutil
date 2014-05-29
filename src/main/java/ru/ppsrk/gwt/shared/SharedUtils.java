package ru.ppsrk.gwt.shared;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ru.ppsrk.gwt.client.HasId;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.view.client.ListDataProvider;

public class SharedUtils {

    /**
     * Tests if the collection contains an element with the specified id.
     * 
     * @param objects
     * @param id
     * @return
     */
    public static boolean containsObject(Collection<? extends HasId> objects, final Long id) {
        for (HasId object : objects) {
            if (object.getId().equals(id))
                return true;
        }
        return false;
    }

    /**
     * Search the collection for the element by id.
     * 
     * @param collection
     * @param id
     * @return found element
     */
    public static <T extends HasId> T getObjectFromCollectionById(Collection<T> collection, Long id) {
        for (T item : collection) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Joins elements of a collection with specified delimiter to a String
     * 
     * @param objects
     * @param delim
     *            delimiter
     * @return String of concatenated elements
     */
    static public <T> String join(Collection<T> objects, String delim) {
        StringBuilder sb = new StringBuilder(objects.size() * (10 + delim.length()));
        for (Object object : objects) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(object);
        }
        return sb.toString();
    }

    /**
     * Joins elements of a collection with comma delimiter
     * 
     * @param objects
     * @return String of comma-separated elements
     */
    public static <T> String join(List<T> objects) {
        return join(objects, ",");
    }

    public static <T> String join(T[] objects, String delim) {
        StringBuilder sb = new StringBuilder(objects.length * (10 + delim.length()));
        for (Object object : objects) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(object);
        }
        return sb.toString();
    }

    /**
     * Removes the element with specified id from the collection.
     * 
     * @param collection
     * @param id
     */
    public static <T extends HasId> void removeObjectFromCollectionById(Collection<T> collection, Long id) {
        for (T item : collection) {
            if (item.getId().equals(id)) {
                collection.remove(item);
                break;
            }
        }
    }

    public static <T extends HasId> void removeObjectFromDataProvider(ListDataProvider<T> dataProvider, T object) {
        removeObjectFromCollectionById(dataProvider.getList(), object.getId());
    }

    public static List<Long> splitToLong(String line) {
        List<Long> list = new LinkedList<Long>();
        if (!line.isEmpty()) {
            for (String num : line.split(",")) {
                list.add(Long.valueOf(num));
            }
        }
        return list;
    }

    /**
     * Removes the collection element that has the id of the new element and adds the new element to the end of the collection.
     * 
     * @param collection
     * @param newElement
     */
    public static <T extends HasId> void updateCollectionElement(Collection<T> collection, T newElement) {
        removeObjectFromCollectionById(collection, newElement.getId());
        collection.add(newElement);
    }

    /**
     * Replaces the list element with the new element that has the same id. Retains the element position.
     * 
     * @param list
     * @param newElement
     */
    public static <T extends HasId> void updateListElement(List<T> list, T newElement) {
        Long id = newElement.getId();
        for (int i = 0; i < list.size(); i++) {
            T item = list.get(i);
            if (item.getId().equals(id)) {
                list.set(i, newElement);
                break;
            }
        }
    }

    public static String getTelemetryString(Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        StringBuilder trace = new StringBuilder();
        trace.append(e).append('\n');
        for (StackTraceElement element : stackTraceElements) {
            trace.append(element.toString()).append('\n');
        }
        String message = e.getMessage();
        if (message == null) {
            message = "---no message---";
        }
        String result = (GWT.isClient() ? "Client" : "Server") + " exception of type " + e.getClass().getName() + ". Message: " + message
                + "\n\n" + trace.toString();
        return result;
    }

}
