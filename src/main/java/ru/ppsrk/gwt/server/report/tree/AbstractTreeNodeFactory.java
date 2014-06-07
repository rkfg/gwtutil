package ru.ppsrk.gwt.server.report.tree;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractTreeNodeFactory<T, TN extends AbstractTreeNode<T>> {

    HashMap<String, TN> paths = new HashMap<String, TN>();

    public TN getOrCreateNode(T value, String... path) {
        return getOrCreateNode(value, buildPath(path));
    }

    public TN getOrCreateNode(T value, String path) {
        TN result = paths.get(path);
        if (result == null) {
            result = newNode(value);
            paths.put(path, result);
            result.setPath(path);
            result.setFactory(this);
        }
        return result;
    }

    protected abstract TN newNode(T value);

    public TN addChild(T value, String parentPath, String childPathPart) {
        TN result = getOrCreateNode(value, buildPath(parentPath, childPathPart));
        if (result.getValue() == value) {
            getOrCreateNode(null, parentPath).addChild(result);
        }
        return result;
    }

    public TN addChild(T value, String... path) {
        return addChild(value, buildPath(Arrays.copyOf(path, path.length - 1)), path[path.length - 1]);
    }

    public String buildPath(String... elems) {
        return StringUtils.join(elems, ',');
    }
}
