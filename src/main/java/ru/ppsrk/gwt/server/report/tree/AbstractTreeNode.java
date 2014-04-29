package ru.ppsrk.gwt.server.report.tree;

import java.util.TreeSet;

public abstract class AbstractTreeNode<T> implements Comparable<T> {
    private AbstractTreeNode<T> parent;
    protected T value;
    private String path;
    private AbstractTreeNodeFactory<T, ? extends AbstractTreeNode<T>> factory = null;
    private TreeSet<AbstractTreeNode<T>> children = new TreeSet<AbstractTreeNode<T>>();

    protected AbstractTreeNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public AbstractTreeNode<T> getParent() {
        return parent;
    }

    public void setParent(AbstractTreeNode<T> parent) {
        this.parent = parent;
    }

    protected void setFactory(AbstractTreeNodeFactory<T, ? extends AbstractTreeNode<T>> factory) {
        this.factory = factory;
    }

    public AbstractTreeNode<T> addChild(T value, String childPathPart) {
        return factory.addChild(value, path, childPathPart);
    }

    protected void addChild(AbstractTreeNode<T> child) {
        if (!children.contains(child)) {
            child.setParent(this);
            children.add(child);
        }
    }

    public TreeSet<AbstractTreeNode<T>> getChildren() {
        return children;
    }

    public String getPath() {
        return path;
    }

    protected void setPath(String path) {
        this.path = path;
    }

}
