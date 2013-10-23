package ru.ppsrk.gwt.shared;

import java.util.Collection;
import java.util.List;

import ru.ppsrk.gwt.client.HasId;

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

    /**
     * Replaces the list element with the new element that has the same id.
     * Retains the element position.
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
            }
        }
    }

    /**
     * Removes the collection element that has the id of the new element and
     * adds the new element to the end of the collection.
     * 
     * @param collection
     * @param newElement
     */
    public static <T extends HasId> void updateCollectionElement(Collection<T> collection, T newElement) {
        removeObjectFromCollectionById(collection, newElement.getId());
        collection.add(newElement);
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

    public static <T extends HasId> void removeObjectFromDataProvider(ListDataProvider<T> dataProvider, T object) {
        removeObjectFromCollectionById(dataProvider.getList(), object.getId());
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
}
