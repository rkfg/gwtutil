package ru.ppsrk.gwt.shared;

import java.util.Collection;
import java.util.List;

import ru.ppsrk.gwt.client.HasId;

import com.google.gwt.view.client.ListDataProvider;

public class SharedUtils {

    public static boolean containsObject(Collection<? extends HasId> objects, final Long id) {
        for (HasId object : objects) {
            if (object.getId().equals(id))
                return true;
        }
        return false;
    }

    public static <T extends HasId> void removeObjectFromCollectionById(Collection<T> collection, Long id) {
        for (T item : collection) {
            if (item.getId().equals(id)) {
                collection.remove(item);
                break;
            }
        }
    }

    public static <T extends HasId> void updateCollectionElement(Collection<T> collection, T newElement) {
        removeObjectFromCollectionById(collection, newElement.getId());
        collection.add(newElement);
    }

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

    static public <T> String join(List<T> objects, String delim) {
        StringBuilder sb = new StringBuilder(objects.size() * (10 + delim.length()));
        for (Object object : objects) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(object);
        }
        return sb.toString();
    }

    public static <T> String join(List<T> objects) {
        return join(objects, ",");
    }
}
