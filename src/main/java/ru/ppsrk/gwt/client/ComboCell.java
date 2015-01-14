/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ru.ppsrk.gwt.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ru.ppsrk.gwt.client.ComboCell.ComboItem;
import ru.ppsrk.gwt.shared.SharedUtils;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A {@link Cell} used to render a drop-down list.
 */
public class ComboCell extends AbstractInputCell<ComboItem, ComboItem> {

    public static class ComboList<T extends ComboItem> extends LinkedList<T> {

        /**
         * 
         */
        private static final long serialVersionUID = 4003094284338608670L;

        public ComboItem getItemByEnum(Enum<?> enoom) {
            return SharedUtils.getObjectFromCollectionById(this, Long.valueOf(enoom.ordinal()));
        }

        public ComboItem getItemById(Long id) {
            return SharedUtils.getObjectFromCollectionById(this, id);
        }

        public static ComboList<ComboItem> fromEnums(Enum<?>[] enums, String[] text) {
            ComboList<ComboItem> result = new ComboList<ComboItem>();
            int i = 0;
            for (Enum<?> enumElement : enums) {
                result.add(new ComboItem(Long.valueOf(enumElement.ordinal()), text[i++]));
            }
            return result;
        }

        public static ComboList<ComboItem> fromHasListboxValues(List<? extends HasListboxValue> list) {
            ComboList<ComboItem> result = new ComboList<ComboItem>();
            for (HasListboxValue element : list) {
                result.add(new ComboItem(element.getId(), element.getListboxValue()));
            }
            return result;
        }
    }

    @SuppressWarnings("serial")
    public static class ComboItem implements HasListboxValue {

        Long id;
        String listboxValue;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        /**
         * @param id
         * @param listboxValue
         */
        public ComboItem(Long id, String listboxValue) {
            super();
            this.id = id;
            this.listboxValue = listboxValue;
        }

        public String getListboxValue() {
            return listboxValue;
        }

        public void setListboxValue(String listboxValue) {
            this.listboxValue = listboxValue;
        }

    }

    interface Template extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{1}</option>")
        SafeHtml deselected(Long id, String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{1}</option>")
        SafeHtml selected(Long id, String option);
    }

    private static Template template;

    private HashMap<Long, Integer> indexForOption = new HashMap<Long, Integer>();

    private final List<ComboItem> options;

    /**
     * Construct a new {@link SelectionCell} with the specified options.
     * 
     * @param options
     *            the options in the cell
     */
    public ComboCell(List<? extends ComboItem> options) {
        super("change");
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.options = new ArrayList<ComboItem>(options);
        refreshIndexes();
    }

    public List<ComboItem> getOptions() {
        return options;
    }

    public void addOptionWithoutRefresh(ComboItem newOp) {
        options.add(newOp);
    }

    public void addOption(ComboItem newOp) {
        options.add(newOp);
        refreshIndexes();
    }

    public void addOptions(Collection<? extends ComboItem> result) {
        options.addAll(result);
        refreshIndexes();
    }

    public void removeOption(Long op) {
        options.remove(indexForOption.get(op).intValue());
        refreshIndexes();
    }

    public void clearOptions() {
        options.clear();
        refreshIndexes();
    }

    public void refreshIndexes() {
        int index = 0;
        for (ComboItem option : options) {
            indexForOption.put(option.getId(), index++);
        }
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, ComboItem value, NativeEvent event, ValueUpdater<ComboItem> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if ("change".equals(type)) {
            Object key = context.getKey();
            SelectElement select = parent.getFirstChild().cast();
            ComboItem newValue = options.get(select.getSelectedIndex());
            setViewData(key, newValue);
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    @Override
    public void render(Context context, ComboItem value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        ComboItem viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        int selectedIndex = getSelectedIndex(viewData == null ? value : viewData);
        sb.appendHtmlConstant("<select tabindex=\"-1\">");
        int index = 0;
        for (ComboItem option : options) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(option.getId(), option.getListboxValue()));
            } else {
                sb.append(template.deselected(option.getId(), option.getListboxValue()));
            }
        }
        sb.appendHtmlConstant("</select>");
    }

    private int getSelectedIndex(ComboItem value) {
        if (value == null) {
            return -1;
        }
        Integer index = indexForOption.get(value.getId());
        if (index == null) {
            return -1;
        }
        return index.intValue();
    }

}