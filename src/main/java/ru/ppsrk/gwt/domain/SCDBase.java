package ru.ppsrk.gwt.domain;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.ppsrk.gwt.dto.BasicDTO;

@SuppressWarnings("serial")
@MappedSuperclass
public abstract class SCDBase extends BasicDTO {
    @Temporal(TemporalType.DATE)
    Date startDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public abstract Object getUniqValue();
    
}