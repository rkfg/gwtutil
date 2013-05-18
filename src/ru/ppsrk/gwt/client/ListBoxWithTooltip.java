package ru.ppsrk.gwt.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.user.client.ui.ListBox;

public class ListBoxWithTooltip extends ListBox {
    @Override
    public void insertItem(String item, Direction dir, String value, int index) {
        SelectElement select = getElement().cast();
        OptionElement option = Document.get().createOptionElement();
        setOptionText(option, item, dir);
        option.setValue(value);
        option.setTitle(item);

        int itemCount = select.getLength();
        if (index < 0 || index > itemCount) {
            index = itemCount;
        }
        if (index == itemCount) {
            select.add(option, null);
        } else {
            OptionElement before = select.getOptions().getItem(index);
            select.add(option, before);
        }
    }

}
