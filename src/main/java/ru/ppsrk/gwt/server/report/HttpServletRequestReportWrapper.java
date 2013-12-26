package ru.ppsrk.gwt.server.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.shared.SharedUtils;

public class HttpServletRequestReportWrapper extends HttpServletRequestWrapper {

    public HttpServletRequestReportWrapper(HttpServletRequest request) {
        super(request);
    }

    public Long getLongParameter(String key) throws LogicException {
        try {
            return Long.valueOf(getParameter(key));
        } catch (NumberFormatException e) {
            throw new LogicException(e.getMessage());
        }
    }

    public Integer getIntegerParameter(String key) throws LogicException {
        try {
            return Integer.valueOf(getParameter(key));
        } catch (NumberFormatException e) {
            throw new LogicException(e.getMessage());
        }
    }

    public Date getDateParameter(String key, String format) throws LogicException {
        try {
            return new SimpleDateFormat(format).parse(getParameter(key));
        } catch (ParseException e) {
            throw new LogicException(e.getMessage());
        }
    }

    public Date getDateParameter(String key) throws LogicException {
        return getDateParameter(key, "dd.MM.yy");
    }

    public List<Long> getLongListParameter(String key) throws LogicException {
        try {
            return SharedUtils.splitToLong(getParameter(key));
        } catch (NumberFormatException e) {
            throw new LogicException(e.getMessage());
        }
    }

    public Boolean getBooleanParameter(String key) {
        return "1".equals(getParameter(key));
    }
    
    public boolean hasParameter(String key){
        return getParameter(key) != null;
    }
}
