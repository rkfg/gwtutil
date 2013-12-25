package ru.ppsrk.gwt.server.report.tree;

public class StringTreeNodeFactory<T> extends AbstractTreeNodeFactory<T, StringTreeNode<T>> {

    @Override
    protected StringTreeNode<T> newNode(T value) {
        return new StringTreeNode<T>(value);
    }

}
