package ru.ppsrk.gwt.client;

import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.HelperType;
import gwtquery.plugins.draggable.client.DraggableOptions.RevertOption;
import gwtquery.plugins.droppable.client.DroppableOptions;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropNodeInfo;
import ru.ppsrk.gwt.client.TreeViewHelper.HasCellValue;

public abstract class DNDTreeViewHelper<T extends HasCellValue> extends TreeViewHelper<T> {

    @Override
    public <E> NodeInfo<T> getNodeInfo(E value) {
        @SuppressWarnings("unchecked")
        DragAndDropNodeInfo<T> dragAndDropNodeInfo = new DragAndDropNodeInfo<T>(loadChildren((T) value), cell, selectionModel, null);
        DroppableOptions dropOptions = dragAndDropNodeInfo.getDroppableOptions();
        dropOptions.setDroppableHoverClass("dropcell-highlight");
        dropOptions.setOnDrop(new DroppableFunction() {

            @Override
            public void f(DragAndDropContext context) {
                onDrop(context);
            }
        });
        DraggableOptions dragOptions = dragAndDropNodeInfo.getDraggableOptions();
        dragOptions.setHelper(HelperType.CLONE);
        dragOptions.setRevert(RevertOption.ON_INVALID_DROP);
        dragOptions.setDistance(10);
        dragOptions.setOpacity(0.7f);
        dragOptions.setRevertDuration(200);
        return dragAndDropNodeInfo;
    }

    protected abstract void onDrop(DragAndDropContext context);

}
