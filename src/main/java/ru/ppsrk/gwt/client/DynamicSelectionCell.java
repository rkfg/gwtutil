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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A {@link Cell} used to render a drop-down list.
 */
public class DynamicSelectionCell extends AbstractInputCell<Pair<Long, String>, Pair<Long, String>> {

    interface Template extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{1}</option>")
        SafeHtml deselected(Long id, String option);

        @Template("<option value=\"{0}\" selected=\"selected\">{1}</option>")
        SafeHtml selected(Long id, String option);
    }

    private static Template template;

    private Map<Long, Integer> indexForOption = new HashMap<>();

    private final List<Pair<Long, String>> options;

    /**
     * Construct a new {@link SelectionCell} with the specified options.
     * 
     * @param options
     *            the options in the cell
     */
    public DynamicSelectionCell(List<Pair<Long, String>> options) {
        super("change");
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.options = new ArrayList<>(options);
        refreshIndexes();
    }

    public List<Pair<Long, String>> getOptions() {
        return options;
    }

    public void addOptionWithoutRefresh(Pair<Long, String> newOp) {
        options.add(newOp);
    }

    public void addOption(Pair<Long, String> newOp) {
        options.add(newOp);
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
        for (Pair<Long, String> option : options) {
            indexForOption.put(option.getLeft(), index++);
        }
    }

    @Override
    public void onBrowserEvent(Context context, Element parent, Pair<Long, String> value, NativeEvent event,
            ValueUpdater<Pair<Long, String>> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if ("change".equals(type)) {
            Object key = context.getKey();
            SelectElement select = parent.getFirstChild().cast();
            Pair<Long, String> newValue = options.get(select.getSelectedIndex());
            setViewData(key, newValue);
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    @Override
    public void render(Context context, Pair<Long, String> value, SafeHtmlBuilder sb) {
        // Get the view data.
        Object key = context.getKey();
        Pair<Long, String> viewData = getViewData(key);
        if (viewData != null && viewData.equals(value)) {
            clearViewData(key);
            viewData = null;
        }

        int selectedIndex = getSelectedIndex(viewData == null ? value : viewData);
        sb.appendHtmlConstant("<select tabindex=\"-1\">");
        int index = 0;
        for (Pair<Long, String> option : options) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(option.getLeft(), option.getRight()));
            } else {
                sb.append(template.deselected(option.getLeft(), option.getRight()));
            }
        }
        sb.appendHtmlConstant("</select>");
    }

    private int getSelectedIndex(Pair<Long, String> value) {
        Integer index = indexForOption.get(value.getLeft());
        if (index == null) {
            return -1;
        }
        return index.intValue();
    }
}