package ru.ppsrk.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ru.ppsrk.gwt.client.ResultPopupPanel.ResultPopupPanelCallback;
import ru.ppsrk.gwt.shared.SharedUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.AbstractCellTree;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.CellTree.CellTreeMessages;
import com.google.gwt.user.cellview.client.CellTree.Resources;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.view.client.HasRows;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

import fr.mikrosimage.gwt.client.ResizableDataGrid;

public class ClientUtils {

    public static class FormPanelLDP extends FormPanel {

        private List<ListDataProvider<?>> fpDataProviders = new LinkedList<ListDataProvider<?>>();
        private List<SelectionModel<?>> fpSelectionModels = new LinkedList<SelectionModel<?>>();

        public void addDataProvider(ListDataProvider<?> listDataProvider) {
            fpDataProviders.add(listDataProvider);
        }

        public void addDataProviders(ListDataProvider<?>... listDataProviders) {
            for (ListDataProvider<?> dataProvider : listDataProviders) {
                fpDataProviders.add(dataProvider);
            }
        }

        public void addSelectionModel(SelectionModel<?> selectionModel) {
            fpSelectionModels.add(selectionModel);
        }

        public void addSelectionModels(SelectionModel<?>... selectionModels) {
            for (SelectionModel<?> selectionModel : selectionModels) {
                fpSelectionModels.add(selectionModel);
            }
        }

        @Override
        public void reset() {
            reset(true, true);
        }

        public void reset(boolean resetDataProviders, boolean resetSelectionModels) {
            super.reset();
            if (resetDataProviders) {
                for (ListDataProvider<?> dataProvider : fpDataProviders) {
                    dataProvider.getList().clear();
                }
            }
            if (resetSelectionModels) {
                for (SelectionModel<?> selectionModel : fpSelectionModels) {
                    selectionModel.setSelected(null, true);
                }
            }
        }
    }

    public static abstract class LoadCellCallback<H extends Hierarchic> {
        public abstract void load(H parent, MyAsyncCallback<List<H>> callback);
    }

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

    public static class RuSimplePager extends SimplePager {

        public RuSimplePager() {
            super(TextLocation.CENTER, false, true);
            getElement().getStyle().setProperty("display", "inline-table");
        }

        protected String createText() {
            // Default text is 1 based.
            NumberFormat formatter = NumberFormat.getFormat("####");
            HasRows display = getDisplay();
            Range range = display.getVisibleRange();
            int pageStart = range.getStart() + 1;
            int pageSize = range.getLength();
            int dataSize = display.getRowCount();
            int endIndex = Math.min(dataSize, pageStart + pageSize - 1);
            endIndex = Math.max(pageStart, endIndex);
            boolean exact = display.isRowCountExact();
            return formatter.format(pageStart) + "-" + formatter.format(endIndex) + (exact ? " из " : " из более ")
                    + formatter.format(dataSize);
        }
    }

    public static class SelectionModelException extends AlertRuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = -9071020761687115990L;

        public SelectionModelException(String failText) {
            super(failText);
        }

    }

    public static class SelectionModelInvalidClassException extends SelectionModelException {

        /**
         * 
         */
        private static final long serialVersionUID = -7104376777000165144L;

        public SelectionModelInvalidClassException(String invalidSelection) {
            super(invalidSelection);
        }

    }

    public static class SelectionModelMultiException extends SelectionModelException {

        /**
         * 
         */
        private static final long serialVersionUID = -3454980779778151279L;

        public SelectionModelMultiException(String failText) {
            super(failText);
        }

    }

    public static class SelectionModelNullException extends SelectionModelException {

        /**
         * 
         */
        private static final long serialVersionUID = 6186225237335362700L;

        public SelectionModelNullException(String failText) {
            super(failText);
        }

    }

    public static class ValueBoxEmptyException extends AlertRuntimeException {

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

        public static class ValueConditionException extends AlertRuntimeException {

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

    public static <T> void autoSelect(SelectionModel<T> selectionModel, ListDataProvider<T> dataProvider) {
        if (dataProvider.getList().size() == 1) {
            selectionModel.setSelected(dataProvider.getList().get(0), true);
        }
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
        listBox.clear();
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
    };

    public static String getListboxSelectedText(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? listBox.getItemText(listBox.getSelectedIndex()) : "";
    };

    public static String getListboxSelectedTextValue(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? listBox.getValue(listBox.getSelectedIndex()) : null;
    }

    public static Long getListboxSelectedValue(ListBox listBox) {
        return listBox.getSelectedIndex() >= 0 ? Long.valueOf(listBox.getValue(listBox.getSelectedIndex())) : -1;
    }

    public static ListDataProvider<? extends Hierarchic> getPathProviderByObject(Hierarchic object,
            SelectionModel<? extends Hierarchic> selectionModel) {
        PathProvider pp = pathDataProviders.get(selectionModel);
        if (pp == null) {
            pathDataProviders.put(selectionModel, new PathProvider());
            return null;
        }
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

    @SuppressWarnings("unchecked")
    public static <H extends Hierarchic> H getReloadParent(AbstractCellTree cellTree, H parent) {
        if (parent == null || !cellTree.getTreeViewModel().isLeaf(parent)) {
            return parent;
        } else {
            H reloadParent = (H) parent.getParent();
            if (reloadParent != null && reloadParent.getId().equals(0L)) {
                reloadParent = null;
            }
            return reloadParent;
        }
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

    public static Set<Hierarchic> listRegisteredObjects() {
        return dataProviders.keySet();
    }

    public static <S extends Hierarchic, H extends SettableParent> ListDataProvider<H> loadCellTree(final H parent,
            SelectionModel<S> selectionModel, ListDataProvider<H> defaultDataProvider, LoadCellCallback<H> loadCallback) {
        final ListDataProvider<H> dataProvider;
        if (parent != null) {
            @SuppressWarnings("unchecked")
            ListDataProvider<H> tmpDataProvider = (ListDataProvider<H>) getPathProviderByObject(parent, selectionModel);
            if (tmpDataProvider != null) {
                dataProvider = tmpDataProvider;
            } else {
                dataProvider = new ListDataProvider<H>();
            }
        } else {
            dataProvider = defaultDataProvider;
        }
        if (parent != null) {
            registerPathProvider(parent, selectionModel, dataProvider);
        }
        loadCallback.load(parent, new MyAsyncCallback<List<H>>() {

            @Override
            public void onSuccess(List<H> result) {
                for (H elem : result) {
                    elem.setParent(parent);
                }
                registerListOfObjects(result, dataProvider);
                dataProvider.getList().clear();
                dataProvider.getList().addAll(result);
            }
        });
        return dataProvider;
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

    public static void openPopupPanel(PopupPanel panel) {
        openPopupPanel(panel, null);
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
        requireLogin(true);
    }

    public static void requireLogin(final boolean rememberMe) {
        AuthService.Util.getInstance().isLoggedIn(new MyAsyncCallback<Boolean>() {

            @Override
            public void onSuccess(Boolean result) {
                if (!result) {
                    PopupPanel popupPanel = new Login(rememberMe);
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
                while (e instanceof UmbrellaException) {
                    e = e.getCause();
                }
                if (e instanceof AlertRuntimeException) {
                    Window.alert(e.getMessage());
                } else {
                    StackTraceElement[] stackTraceElements = e.getStackTrace();
                    StringBuilder trace = new StringBuilder();
                    trace.append(e).append('\n');
                    for (StackTraceElement element : stackTraceElements) {
                        trace.append(element.toString()).append('\n');
                    }
                    Window.alert(trace.toString());
                }
            }
        });

    }

    public static void setupTelemetryExceptionHandler() {
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void onUncaughtException(Throwable e) {
                while (e instanceof UmbrellaException) {
                    e = e.getCause();
                }
                if (e instanceof AlertRuntimeException) {
                    Window.alert(e.getMessage());
                } else {
                    sendErrorTelemetry(e);
                    Window.alert("Произошла клиентская ошибка. Информация отправлена разработчику.");
                }
            }
        });
    }

    public static <S, T extends S> List<T> trySelectionModelValue(MultiSelectionModel<S> selectionModel, String failText,
            Class<T> selectedClass) {
        @SuppressWarnings("unchecked")
        List<T> result = new ArrayList<T>((Collection<? extends T>) selectionModel.getSelectedSet());
        if (result.isEmpty()) {
            throw new SelectionModelNullException(failText);
        }
        for (T elem : result) {
            if (!elem.getClass().getName().equals(selectedClass.getName())) {
                throw new SelectionModelInvalidClassException(failText);
            }
        }
        return result;
    }

    public static <S, T extends S> T trySelectionModelValue(MultiSelectionModel<S> selectionModel, String failText, String multiFailText,
            Class<T> selectedClass) {
        try {
            List<T> result = trySelectionModelValue(selectionModel, failText, selectedClass);
            if (multiFailText != null && result.size() > 1) {
                throw new SelectionModelMultiException(multiFailText);
            }
            return result.get(0);
        } catch (ClassCastException e) {
            throw new SelectionModelInvalidClassException(failText);
        }
    }

    public static <S, T extends S> T trySelectionModelValue(SingleSelectionModel<S> selectionModel, String failText, Class<T> selectedClass) {
        return trySelectionModelValue(selectionModel, failText, selectedClass, false);
    }

    /**
     * Returns the selected object by selection model
     * 
     * @param selectionModel
     * @param failText
     *            text to throw in case of cast exception
     * @param selectedClass
     *            expected class
     * @param allowSubclasses
     *            don't throw exception if the result is a subclass of the specified class
     * @return selectionModel's selected object
     * @throws SelectionModelInvalidClassException
     *             if the object has unexpected type.
     */
    public static <S, T extends S> T trySelectionModelValue(SingleSelectionModel<S> selectionModel, String failText,
            Class<T> selectedClass, boolean allowSubclasses) {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) selectionModel.getSelectedObject();
            if (result == null) {
                throw new SelectionModelNullException(failText);
            }
            if (!allowSubclasses && !result.getClass().getName().equals(selectedClass.getName())) {
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

    public static void sendErrorTelemetry(Throwable e) {
        String result = SharedUtils.getTelemetryString(e);
        TelemetryServiceAsync.Util.getInstance().sendTelemetry(result, new MyAsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {

            }
        });
    }
}
