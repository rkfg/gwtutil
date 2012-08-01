package ru.ppsrk.gwt.client;

import java.util.List;

import com.google.gwt.user.client.ui.ListBox;

public class ClientUtils {
	public static Long getListboxSelectedValue(ListBox listBox) {
		return listBox.getSelectedIndex() >= 0 ? Long.valueOf(listBox
				.getValue(listBox.getSelectedIndex())) : -1;
	}

	public static String getListboxSelectedText(ListBox listBox) {
		return listBox.getSelectedIndex() >= 0 ? listBox.getItemText(listBox
				.getSelectedIndex()) : "";
	}

	public static int getListboxIndexByValue(ListBox listBox, Long value) {
		String strValue = value.toString();
		for (int i = 0; i < listBox.getItemCount(); i++) {
			if (listBox.getValue(i).equals(strValue)) {
				return i;
			}
		}
		return -1;
	}

	public static void setListBoxSelectedItemByValue(ListBox listBox, Long value) {
		listBox.setSelectedIndex(getListboxIndexByValue(listBox, value));
	}

	public static String buildPath(Hierarchic dtoObject, boolean includeLeaf) {
		String path = new String();
		if (includeLeaf)
			path = dtoObject.getId().toString();
		
		Hierarchic currentObject = dtoObject.getParent();
		while (currentObject != null) {
			if (path.length() > 0) {
				path = currentObject.getId().toString() + ":" + path;
			} else {
				path = currentObject.getId().toString();
			}
			currentObject = currentObject.getParent();
		}
		return path;
	}

	public static boolean containsObject(List<? extends Hierarchic> objects, final Long id) {
		for (Hierarchic object: objects){
			if (object.getId().equals(id))
				return true;
		}
		return false;
	}

}
