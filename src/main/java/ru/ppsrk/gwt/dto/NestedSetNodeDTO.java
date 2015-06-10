package ru.ppsrk.gwt.dto;

import ru.ppsrk.gwt.client.Hierarchic;
import ru.ppsrk.gwt.client.TreeViewHelper.HasCellValue;

@SuppressWarnings("serial")
public abstract class NestedSetNodeDTO implements HasCellValue {
    
    Long id;
    Long leftnum;
    Long rightnum;
    Long depth;
    Hierarchic parent;
    Long children;
    Boolean root;

    public NestedSetNodeDTO(Long id) {
        this.id = id;
    }

    public NestedSetNodeDTO() {
        id = 0L;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeftnum() {
        return leftnum;
    }

    public void setLeftnum(Long leftnum) {
        this.leftnum = leftnum;
    }

    public Long getRightnum() {
        return rightnum;
    }

    public void setRightnum(Long rightnum) {
        this.rightnum = rightnum;
    }

    public Long getDepth() {
        return depth;
    }

    public void setDepth(Long depth) {
        this.depth = depth;
    }

    public Hierarchic getParent() {
        return parent;
    }

    @Override
    public void setParent(Hierarchic parent) {
        this.parent = parent;
    }

    public Long getChildren() {
        return children;
    }

    public void setChildren(Long children) {
        this.children = children;
    }

    public Boolean getRoot() {
        return root;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }

    public boolean isLeaf() {
        return rightnum - leftnum == 1;
    }

}
