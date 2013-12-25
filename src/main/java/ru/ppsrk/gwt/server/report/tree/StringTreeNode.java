package ru.ppsrk.gwt.server.report.tree;

public class StringTreeNode<T> extends AbstractTreeNode<T> {

    protected StringTreeNode(T value) {
        super(value);
    }

    @Override
    public int compareTo(T o) {
        if (o == null) {
            return 1;
        }
        return toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
