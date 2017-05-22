package ru.ppsrk.gwt.server.report;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import freemarker.template.TemplateException;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.LogicException;

public interface ReportHandler {
    /**
     * Callback for report dispatching
     * 
     * @param req
     *            servlet wrapped request
     * @param resp
     *            servlet response
     * @param empty
     *            params template parameters which should be filled by this method
     * @return template filename
     * @throws TemplateException
     * @throws IOException
     * @throws LogicException
     * @throws ClientAuthException
     */
    public String exec(HttpServletRequestReportWrapper req, HttpServletResponse resp, Map<String, Object> params)
            throws TemplateException, IOException, GwtUtilException;

    public String[] getRequiredParams();

    public String getType();
}