package ru.ppsrk.gwt.server.nestedset;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(name = "depthFilter", parameters = @ParamDef(name = "depth", type = "long"))
@Filter(name = "depthFilter", condition = "depth = :depth")
public class NestedSetNode {
    @GeneratedValue
    @Id
    Long id;
    Long leftnum;
    Long rightnum;
    Long depth;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLeftNum() {
        return leftnum;
    }

    public void setLeftNum(Long left) {
        this.leftnum = left;
    }

    public Long getRightNum() {
        return rightnum;
    }

    public void setRightNum(Long right) {
        this.rightnum = right;
    }

    public Long getDepth() {
        return depth;
    }

    public void setDepth(Long depth) {
        this.depth = depth;
    }

}
