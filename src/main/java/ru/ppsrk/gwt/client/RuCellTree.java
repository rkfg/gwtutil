package ru.ppsrk.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.view.client.TreeViewModel;

public class RuCellTree extends CellTree {

    interface RussianCellTreeMessages extends CellTreeMessages {

        @DefaultMessage("Пусто")
        public String emptyTree();

        @DefaultMessage("Ещё...")
        public String showMore();
    };

    public RuCellTree(TreeViewModel viewModel) {
        super(viewModel, null, GWT.create(Resources.class), GWT.create(RussianCellTreeMessages.class));
    }

    public RuCellTree(TreeViewModel viewModel, int size) {
        super(viewModel, null, GWT.create(Resources.class), GWT.create(RussianCellTreeMessages.class), size);
    }

}