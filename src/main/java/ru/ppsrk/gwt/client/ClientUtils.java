package ru.ppsrk.gwt.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.ppsrk.gwt.client.ResultPopupPanel.ResultPopupPanelCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.CellTreeMessages;
import com.google.gwt.user.cellview.client.CellTree.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

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

    public static class RuCellTree extends CellTree {

        public RuCellTree(TreeViewModel viewModel) {
            super(viewModel, null, cellTreeResources, russianCellTreeMessages);
        }

    }

    public static class SelectionModelInvalidClassException extends SilentException {

        /**
         * 
         */
        private static final long serialVersionUID = -7104376777000165144L;

        public SelectionModelInvalidClassException(String invalidSelection) {
            super(invalidSelection);
        }

    }

    public static class SelectionModelNullException extends SilentException {

        /**
         * 
         */
        private static final long serialVersionUID = 6186225237335362700L;

        public SelectionModelNullException(String failText) {
            super(failText);
        }

    }

    public static class SilentException extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = -5304271527831179883L;

        public SilentException(String failText) {
            super(failText);
        }

    }

    public static class ValueBoxEmptyException extends SilentException {

        /**
         * 
         */
        private static final long serialVersionUID = -2721839951524306048L;

        public ValueBoxEmptyException(String failText) {
            super(failText);
            // TODO Auto-generated constructor stub
        }

    }

    /**
     * Tests the entered value against some condition and throws ValueConditionException if it hasn't been met.
     * 
     * @param <T>
     *            the type of the value
     * @throws ValueConditionException
     *             if the condition fails
     */
    public abstract static class ValueCondition<T> {

        /**
         * Tests if the number value isn't negative.
         * 
         * @param <T>
         *            value type
         * @throws ValueConditionException
         *             if the value is negative or isn't a number
         */
        public static class NonNegativeValueCondition<T extends Number> extends ValueCondition<T> {

            private String failText;

            public NonNegativeValueCondition(String failText) {
                super();
                this.failText = failText;
            }

            @Override
            public void test(T value) {
                try {
                    Number numVal = (Number) value;
                    if (numVal.longValue() < 0) {
                        throw new ValueConditionException(failText);
                    }
                } catch (ClassCastException e) {
                    throw new ValueConditionException("Введённое значение не является числом.");
                }
            }
        }

        public static class ValueConditionException extends RuntimeException {

            /**
             * 
             */
            private static final long serialVersionUID = 1644860720447547515L;

            public ValueConditionException(String string) {
                super(string);
            }

        }

        public abstract void test(T value);

    }

    static private HashMap<Hierarchic, ListDataProvider<? extends Hierarchic>> dataProviders = new HashMap<Hierarchic, ListDataProvider<? extends Hierarchic>>();

    static private HashMap<SelectionModel<? extends Hierarchic>, PathProvider> pathDataProviders = new HashMap<SelectionModel<? extends Hierarchic>, ClientUtils.PathProvider>();

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

    public static void fillListbox(Collection<? extends HasListboxValue> list, ListBox listBox) {
        for (HasListboxValue listboxValue : list) {
            addItemToListbox(listboxValue, listBox);
        }
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
    };

    public static ListDataProvider<? extends Hierarchic> getPathProviderByObject(Hierarchic object,
            SelectionModel<? extends Hierarchic> selectionModel) {
        return pathDataProviders.get(selectionModel).get(buildPath(object, true));
    };

    public static Hierarchic getRegisteredObjectBySample(Hierarchic sample) {
        for (Hierarchic hierarchic : dataProviders.keySet()) {
            if (hierarchic.getClass().equals(sample.getClass()) && hierarchic.getId().equals(sample.getId())) {
                return hierarchic;
            }
        }
        return null;
    }

    /**
     * Inserts a new object to the data provider specified by the parent object. If no such provider or parent object exists, creates them.
     * 
     * @param object
     *            object to insert
     * @param parentObjectToCreate
     *            parent object to add the <b>object</b> to
     * @param selectionModel
     *            selection model which is used in the target CellTree
     * @param createFirst
     *            if true, insert the parent object to the beginning of the parent's parent list. If false, add it to the end of the list.
     */

    @SuppressWarnings("unchecked")
    public static void insertObjectToParentProvider(Hierarchic object, Hierarchic parentObjectToCreate,
            SelectionModel<? extends Hierarchic> selectionModel, boolean createFirst) {
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

    public static void openPopupPanel(PopupPanel panel, FocusWidget focusWidget) {
        openPopupPanel(panel, focusWidget, true, true);
    }

    public static void openPopupPanel(PopupPanel panel, FocusWidget focusWidget, boolean animate, boolean modal) {
        panel.setGlassEnabled(true);
        panel.setAnimationEnabled(animate);
        panel.center();
        if (focusWidget != null) {
            focusWidget.setFocus(true);
        }
        panel.setModal(modal);
    }

    public static <T> void openPopupPanel(ResultPopupPanel<T> panel, ResultPopupPanelCallback<T> callback) {
        panel.setResultCallback(callback);
        openPopupPanel(panel, panel.getFocusWidget(), false, false);
    }

    public static void openWindow(String url) {
        openWindow(url, null, null, false);
    }

    public static void openWindow(String url, String name, String features, boolean rootRelative) {
        Window.open((rootRelative ? GWT.getHostPageBaseURL() : GWT.getModuleBaseURL()) + url, name, features);
    }

    public static void openWindowRootRelative(String url) {
        openWindow(url, null, null, true);
    }

    /**
     * Registers the objects with the provider. Used to find sibling nodes or the provider by object to remove that object or modify it.
     * Call this on adding or setting objects to the data provider.
     * 
     * @param list
     * @param listDataProvider
     */
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

    /**
     * Registers the supplied {@link ListDataProvider} with {@link SelectionModel} and parent object to which this data provider belongs to.
     * It's used to find the data provider later having the parent object, for example to add a new child node. Use after creating a new
     * data provider.
     * 
     * @param object
     *            parent object
     * @param selectionModel
     *            used to distinguish between trees
     * @param listDataProvider
     *            the data provider to register
     */
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

    public static <T, E extends T> void replaceListDataProviderContents(ListDataProvider<T> dataProvider, Collection<E> element) {
        dataProvider.getList().clear();
        dataProvider.getList().addAll(element);
    }

    public static void requireLogin() {
        AuthService.Util.getInstance().isLoggedIn(new MyAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    PopupPanel popupPanel = new Login(false, true);
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

    public static void setTextboxValueBySelectionModel(ValueBoxBase<String> textBox,
            SingleSelectionModel<? extends HasListboxValue> selectionModel) {
        HasListboxValue selected = selectionModel.getSelectedObject();
        if (selected == null) {
            textBox.setValue("");
        } else {
            textBox.setValue(selected.getListboxValue());
        }
    }

    public static void setupExceptionHandler() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void onUncaughtException(Throwable e) {
                if (e instanceof UmbrellaException) {
                    Window.alert(e.getCause().getMessage());
                } else {
                    if (e.getMessage() != null) {
                        Window.alert(e.getMessage());
                    } else {
                        if (e instanceof SilentException) {
                            return;
                        }
                        StackTraceElement[] stackTraceElements = e.getStackTrace();
                        StringBuilder trace = new StringBuilder();
                        trace.append(e).append('\n');
                        for (StackTraceElement element : stackTraceElements) {
                            trace.append(element.toString()).append('\n');
                        }
                        Window.alert(trace.toString());
                    }
                }
            }
        });

    }

    /**
     * Returns the selected object by selection model
     * 
     * @param selectionModel
     * @param failText
     *            text to throw in case of cast exception
     * @param selectedClass
     *            expected class
     * @return selectionModel's selected object
     * @throws SelectionModelInvalidClassException
     *             if the object has unexpected type.
     */
    public static <S, T extends S> T trySelectionModelValue(SingleSelectionModel<S> selectionModel, String failText, Class<T> selectedClass) {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) selectionModel.getSelectedObject();
            if (result == null) {
                throw new SelectionModelNullException(failText);
            }
            if (!result.getClass().getName().equals(selectedClass.getName())) {
                throw new SelectionModelInvalidClassException(failText);
            }
            return result;
        } catch (ClassCastException e) {
            throw new SelectionModelInvalidClassException(failText);
        }
    }

    public static <T> T tryValueBoxValue(ValueBoxBase<T> valueBox, String failText) {
        return tryValueBoxValue(valueBox, failText, null);
    }

    public static <T> T tryValueBoxValue(ValueBoxBase<T> valueBox, String onEmptyFailText, ValueCondition<T> condition) {
        T result = valueBox.getValue();
        if (result == null || result instanceof String && ((String) result).isEmpty()) {
            valueBox.setFocus(true);
            throw new ValueBoxEmptyException(onEmptyFailText);
        }
        if (condition != null) {
            condition.test(result);
        }
        return result;
    }

    public static class FormPanelLDP extends FormPanel {

        private List<ListDataProvider<?>> fpDataProviders = new LinkedList<ListDataProvider<?>>();

        @Override
        public void reset() {
            super.reset();
            for (ListDataProvider<?> dataProvider : fpDataProviders) {
                dataProvider.getList().clear();
            }
        }

        public void addDataProvider(ListDataProvider<?> listDataProvider) {
            fpDataProviders.add(listDataProvider);
        }

        public void addDataProviders(ListDataProvider<?>... listDataProviders) {
            for (ListDataProvider<?> dataProvider : listDataProviders) {
                fpDataProviders.add(dataProvider);
            }
        }
    }
}
