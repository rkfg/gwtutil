package ru.ppsrk.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.TreeViewModel;

public class RuCellTree extends CellTree {

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

    public RuCellTree(TreeViewModel viewModel) {
        super(viewModel, null, cellTreeResources, russianCellTreeMessages);
    }

}