package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import ru.ppsrk.gwt.client.ResultPopupPanel.ResultPopupPanelCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTree.CellTreeMessages;
import com.google.gwt.user.cellview.client.CellTree.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

import fr.mikrosimage.gwt.client.ResizableDataGrid;

public class ClientUtils {
    static public abstract class MyAsyncCallback<T> implements AsyncCallback<T> {

        public void errorHandler(Throwable exception) {
            if (!(exception instanceof ClientAuthenticationException) && !(exception instanceof ClientAuthorizationException)) {
                if (exception instanceof LogicException) {
                    Window.alert("Ошибка: " + exception.getMessage());
                } else {
                    System.out.println("Stacktrace:");
                    // exception.printStackTrace();
                    System.out.println("--------------------------");
                    for (StackTraceElement ste : exception.getStackTrace()) {
                        System.out.println(ste);
                    }
                    System.out.println("--------------------------");
                    if (!exception.getMessage().startsWith("0")) {
                        Window.alert("Произошла непредвиденная ошибка. Технические данные: " + exception.getMessage());
                    }
                }
            }
        }

        @Override
        public void onFailure(Throwable caught) {
            errorHandler(caught);
        }
    }

    private static class PathProvider extends HashMap<String, ListDataProvider<? extends Hierarchic>> {

        /**
         * 
         */
        private static final long serialVersionUID = 970525863372346506L;

    }

    static private HashMap<Hierarchic, ListDataProvider<? extends Hierarchic>> dataProviders = new HashMap<Hierarchic, ListDataProvider<? extends Hierarchic>>();

    static private HashMap<SelectionModel<? extends Hierarchic>, PathProvider> pathDataProviders = new HashMap<SelectionModel<? extends Hierarchic>, ClientUtils.PathProvider>();

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

    public static void clearProvidersMappings() {
        pathDataProviders.clear();
        dataProviders.clear();
    }

    public static ListDataProvider<? extends Hierarchic> getDataProviderByObject(Hierarchic object) {
        return dataProviders.get(object);
    }

    public static int getListboxIndexByValue(ListBox listBox, Long value) {
        return getListboxIndexByValue(listBox, value.toString());
    }

    public static int getListboxIndexByValue(ListBox listBox, String value) {
        for (int i = 0; i < listBox.getItemCount(); i++) {
            if (listBox.getValue(i).equals(value)) {
                return i;
            }
        }
        return -1;
    }

    public static String getListboxSelectedText(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? listBox.getItemText(listBox.getSelectedIndex()) : "";
    }

    public static String getListboxSelectedTextValue(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? listBox.getValue(listBox.getSelectedIndex()) : null;
    }

    public static Long getListboxSelectedValue(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? Long.valueOf(listBox.getValue(listBox.getSelectedIndex())) : -1;
    }

    public static ListDataProvider<? extends Hierarchic> getPathProviderByObject(Hierarchic object, SelectionModel<? extends Hierarchic> selectionModel) {
        return pathDataProviders.get(selectionModel).get(buildPath(object, true));
    }

    public static Hierarchic getRegisteredObjectBySample(Hierarchic sample) {
        for (Hierarchic hierarchic : dataProviders.keySet()) {
            if (hierarchic.getClass().equals(sample.getClass()) && hierarchic.getId().equals(sample.getId())) {
                return hierarchic;
            }
        }
        return null;
    }

    /**
     * Inserts a new object to the data provider specified by the parent object.
     * If no such provider or parent object exists, creates them.
     * 
     * @param object
     *            object to insert
     * @param parentObjectToCreate
     *            parent object to add the <b>object</b> to
     * @param selectionModel
     *            selection model which is used in the target CellTree
     * @param createFirst
     *            if true, insert the parent object to the beginning of the
     *            parent's parent list. If false, add it to the end of the list.
     */

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

    public static Set<Hierarchic> listRegisteredObjects() {
        return dataProviders.keySet();
    }

    public static void logout() {
        AuthService.Util.getInstance().logout(new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                Window.Location.reload();
            }

            @Override
            public void onSuccess(Void result) {
                Window.Location.reload();
            }
        });
    }

    public static void openWindow(String url) {
        openWindow(url, null, null, false);
    };

    public static void openWindowRootRelative(String url) {
        openWindow(url, null, null, true);
    };

    public static void openWindow(String url, String name, String features, boolean rootRelative) {
        Window.open((rootRelative ? GWT.getHostPageBaseURL() : GWT.getModuleBaseURL()) + url, name, features);
    }

    public static void registerListOfObjects(Collection<? extends Hierarchic> list, ListDataProvider<? extends Hierarchic> listDataProvider) {
        for (Hierarchic element : list) {
            registerObjectDataProvider(element, listDataProvider);
        }
    }

    public static void registerListWithParentPath(Collection<? extends Hierarchic> list, Hierarchic parent,
            SelectionModel<? extends Hierarchic> selectionModel, ListDataProvider<? extends Hierarchic> listDataProvider) {
        registerPathProvider(parent, selectionModel, listDataProvider);
        registerListOfObjects(list, listDataProvider);
    }

    public static void registerObjectDataProvider(Hierarchic object, ListDataProvider<? extends Hierarchic> listDataProvider) {
        dataProviders.put(object, listDataProvider);
    }

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

    public static void removeObjectFromDataProvider(Hierarchic object) {
        ListDataProvider<? extends Hierarchic> listDataProvider = dataProviders.get(object);
        if (listDataProvider == null) {
            // this is to search by equality (i.e. Hierarchic.getId()) instead
            // of hashCode
            Hierarchic key = getRegisteredObjectBySample(object);
            if (key != null) {
                listDataProvider = dataProviders.get(key);
            } else {
                return;
            }
        }
        listDataProvider.getList().remove(object);
        dataProviders.remove(object);
    }

    public static <T extends Hierarchic> void removeObjectFromDataProvider(T object, SingleSelectionModel<T> selectionModel) {
        selectionModel.setSelected(object, false);
        removeObjectFromDataProvider(object);
    }

    public static void requireLogin() {
        requireLogin(RealmType.HIBERNATE);
    }

    public static void requireLogin(final RealmType realmType) {
        AuthService.Util.getInstance().isLoggedIn(new MyAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    PopupPanel popupPanel = new Login(false, true, realmType);
                    popupPanel.center();
                }
            }
        });
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
        int start = provSize < pageSize ? 0 : provSize - pageSize + 1;
        resizableDataGrid.setVisibleRange(start, pageSize);
        listDataProvider.refresh();
        resizableDataGrid.getScrollPanel().scrollToBottom();
    }

    public static void setListBoxSelectedItemByValue(ListBox listBox, Long value) {
        listBox.setSelectedIndex(getListboxIndexByValue(listBox, value));
    }

    public static void setListBoxSelectedItemByValue(ListBox listBox, String value) {
        listBox.setSelectedIndex(getListboxIndexByValue(listBox, value));
    }

    public static void openPopupPanel(PopupPanel panel, FocusWidget focusWidget) {
        panel.setGlassEnabled(true);
        panel.setAnimationEnabled(true);
        panel.center();
        if (focusWidget != null) {
            focusWidget.setFocus(true);
        }
        panel.setModal(true);
    }

    public static <T> void openPopupPanel(ResultPopupPanel<T> panel, ResultPopupPanelCallback<T> callback) {
        panel.setResultCallback(callback);
        openPopupPanel(panel, panel.getFocusWidget());
    }

    public static Resources cellTreeResources = GWT.create(Resources.class);
    public static CellTreeMessages russianCellTreeMessages = new CellTreeMessages() {

        @Override
        public String emptyTree() {
            return "Пусто";
        }

        @Override
        public String showMore() {
            return "Ещё...";
        }
    };

    public static void addItemToListbox(HasListboxValue value, ListBox listBox) {
        listBox.addItem(value.getListboxValue(), value.getId().toString());
    }

    public static void fillListbox(Collection<? extends HasListboxValue> list, ListBox listBox) {
        for (HasListboxValue listboxValue : list) {
            addItemToListbox(listboxValue, listBox);
        }
    }

    public static void setTextboxValueBySelectionModel(ValueBoxBase<String> textBox, SingleSelectionModel<HasListboxValue> selectionModel) {
        HasListboxValue selected = selectionModel.getSelectedObject();
        if (selected == null) {
            textBox.setValue("");
        } else {
            textBox.setValue(selected.getListboxValue());
        }
    }

    public static <T, E extends T> void replaceListDataProviderContents(ListDataProvider<T> dataProvider, Collection<E> element) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(element);
    }
}
