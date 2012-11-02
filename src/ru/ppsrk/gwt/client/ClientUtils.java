package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

import fr.mikrosimage.gwt.client.ResizableDataGrid;

public class ClientUtils {
    static private HashMap<Hierarchic, ListDataProvider<? extends Hierarchic>> dataProviders = new HashMap<Hierarchic, ListDataProvider<? extends Hierarchic>>();
    static private HashMap<SelectionModel<? extends Hierarchic>, PathProvider> pathDataProviders = new HashMap<SelectionModel<? extends Hierarchic>, ClientUtils.PathProvider>();

    public static Long getListboxSelectedValue(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? Long.valueOf(listBox.getValue(listBox.getSelectedIndex())) : -1;
    }

    public static String getListboxSelectedTextValue(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? listBox.getValue(listBox.getSelectedIndex()) : null;
    }
    
    public static String getListboxSelectedText(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? listBox.getItemText(listBox.getSelectedIndex()) : "";
    }

    public static int getListboxIndexByValue(ListBox listBox, String value) {
        for (int i = 0; i < listBox.getItemCount(); i++) {
            if (listBox.getValue(i).equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public static int getListboxIndexByValue(ListBox listBox, Long value) {
        return getListboxIndexByValue(listBox, value.toString());
    }

    public static void setListBoxSelectedItemByValue(ListBox listBox, String value) {
        listBox.setSelectedIndex(getListboxIndexByValue(listBox, value));
    }

    public static void setListBoxSelectedItemByValue(ListBox listBox, Long value) {
        listBox.setSelectedIndex(getListboxIndexByValue(listBox, value));
    }

    public static String buildPath(Hierarchic dtoObject, boolean includeLeaf) {
        String path = new String();
        if (dtoObject != null) {
            if (includeLeaf)
                path = dtoObject.getId().toString();

            Hierarchic currentObject = dtoObject.getParent();
            while (currentObject != null && currentObject.getId() != 0) {
                if (path.length() > 0) {
                    path = currentObject.getId().toString() + ":" + path;
                } else {
                    path = currentObject.getId().toString();
                }
                currentObject = currentObject.getParent();
            }
        }
        return path;
    }

    public static boolean containsObject(List<? extends Hierarchic> objects, final Long id) {
        for (Hierarchic object : objects) {
            if (object.getId().equals(id))
                return true;
        }
        return false;
    }

    public static void registerObjectDataProvider(Hierarchic object, ListDataProvider<? extends Hierarchic> listDataProvider) {
        dataProviders.put(object, listDataProvider);
    }

    public static void registerListOfObjects(Collection<? extends Hierarchic> list, ListDataProvider<? extends Hierarchic> listDataProvider) {
        for (Hierarchic element : list) {
            registerObjectDataProvider(element, listDataProvider);
        }
    }

    public static void removeObjectFromDataProvider(Hierarchic object) {
        ListDataProvider<? extends Hierarchic> listDataProvider = dataProviders.get(object);
        if (listDataProvider == null) {
            System.out.println("Object " + object + " isn't registered");
            return;
        }
        listDataProvider.getList().remove(object);
        dataProviders.remove(object);
    }

    public static <T extends Hierarchic> void removeObjectFromDataProvider(T object, SingleSelectionModel<T> selectionModel) {
        selectionModel.setSelected(object, false);
        removeObjectFromDataProvider(object);
    }

    private static class PathProvider extends HashMap<String, ListDataProvider<? extends Hierarchic>> {

        /**
         * 
         */
        private static final long serialVersionUID = 970525863372346506L;

    };

    public static void registerPathProvider(Hierarchic object, SelectionModel<? extends Hierarchic> selectionModel,
            ListDataProvider<? extends Hierarchic> listDataProvider) {
        PathProvider pathProvider;
        if (pathDataProviders.containsKey(selectionModel)) {
            pathProvider = pathDataProviders.get(selectionModel);
        } else {
            pathProvider = new PathProvider();
            pathDataProviders.put(selectionModel, pathProvider);
        }
        pathProvider.put(buildPath(object, true), listDataProvider);
    }

    public static ListDataProvider<? extends Hierarchic> getProviderByObject(Hierarchic object, SelectionModel<? extends Hierarchic> selectionModel) {
        return pathDataProviders.get(selectionModel).get(buildPath(object, true));
    }

    @SuppressWarnings("unchecked")
    public static void insertObjectToParentProvider(Hierarchic object, Hierarchic parentObjectToCreate, SelectionModel<? extends Hierarchic> selectionModel,
            boolean createFirst) {
        if (selectionModel == null || parentObjectToCreate == null || object == null || pathDataProviders.get(selectionModel) == null) {
            return;
        }
        ListDataProvider<Hierarchic> listDataProvider = (ListDataProvider<Hierarchic>) pathDataProviders.get(selectionModel).get(
                buildPath(parentObjectToCreate, true));
        if (listDataProvider != null) {
            if (createFirst) {
                listDataProvider.getList().add(0, object);
            } else {
                listDataProvider.getList().add(object);
            }
            registerObjectDataProvider(object, listDataProvider);
        } else {
            ListDataProvider<Hierarchic> listParentDataProvider = (ListDataProvider<Hierarchic>) pathDataProviders.get(selectionModel).get(
                    buildPath(parentObjectToCreate.getParent(), true));
            if (listParentDataProvider != null) {
                if (!listParentDataProvider.getList().contains(parentObjectToCreate)) {
                    if (createFirst) {
                        listParentDataProvider.getList().add(0, parentObjectToCreate);
                    } else {
                        listParentDataProvider.getList().add(parentObjectToCreate);
                    }
                }
            }
        }
    }

    public static void clearProvidersMappings() {
        pathDataProviders.clear();
        dataProviders.clear();
    }

    public static void openWindow(String url, String name, String features) {
        Window.open(GWT.getModuleBaseURL() + url, name, features);
    }

    public static void openWindow(String url) {
        openWindow(url, null, null);
    }

    public static <T extends Hierarchic> void scrollToTheEnd(T newItem, SingleSelectionModel<T> selectionModel,
            ListDataProvider<? extends Hierarchic> listDataProvider, ResizableDataGrid<? extends Hierarchic> resizableDataGrid) {
        if (selectionModel.getSelectedObject() != null) {
            T selectedObject = selectionModel.getSelectedObject();
            selectionModel.setSelected(selectedObject, false);
        }
        Long oldId = newItem.getId();
        newItem.setId(-1L);
        selectionModel.setSelected(newItem, true);
        newItem.setId(oldId);
        int provSize = listDataProvider.getList().size();
        int pageSize = resizableDataGrid.getPageSize();
        int start = provSize < pageSize ? 0 : provSize - pageSize;
        resizableDataGrid.setVisibleRange(start, pageSize);
        listDataProvider.refresh();
        resizableDataGrid.getScrollPanel().scrollToBottom();
    }
}
