package ru.ppsrk.gwt.client;

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
        DroppableOptions options = dragAndDropNodeInfo.getDroppableOptions();
        options.setDroppableHoverClass("dropcell-highlight");
        dragAndDropNodeInfo.getDraggableOptions().setHelper(HelperType.CLONE);
        dragAndDropNodeInfo.getDraggableOptions().setRevert(RevertOption.ON_INVALID_DROP);
        dragAndDropNodeInfo.getDraggableOptions().setDistance(10);
        dragAndDropNodeInfo.getDraggableOptions().setOpacity(0.7f);
        dragAndDropNodeInfo.getDraggableOptions().setRevertDuration(200);
        options.setOnDrop(new DroppableFunction() {

            @Override
            public void f(DragAndDropContext context) {
                onDrop(context);
            }
        });
        return dragAndDropNodeInfo;
    }

    protected abstract void onDrop(DragAndDropContext context);

}
