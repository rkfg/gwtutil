package ru.ppsrk.gwt.server.report.tree;

import java.util.Arrays;
import java.util.HashMap;

import ru.ppsrk.gwt.shared.SharedUtils;

public abstract class AbstractTreeNodeFactory<T, TN extends AbstractTreeNode<T>> {

    HashMap<String, TN> paths = new HashMap<String, TN>();

    public TN getOrCreateNode(String path, T value) {
        TN result = paths.get(path);
        if (result == null) {
            result = newNode(value);
            paths.put(path, result);
            result.setPath(path);
        }
        return result;
    }

    protected abstract TN newNode(T value);

    public TN addChild(String parentPath, String childPathPart, T value) {
        TN result = getOrCreateNode(buildPath(parentPath, childPathPart), value);
        if (result.getValue() == value) {
            getOrCreateNode(parentPath, null).addChild(result);
        }
        return result;
    }

    public TN addChild(String[] parentPath, String childPathPart, T value) {
        return addChild(buildPath(parentPath), childPathPart, value);
    }

    public String buildPath(String... elems) {
        return SharedUtils.join(Arrays.asList(elems));
    }
}
