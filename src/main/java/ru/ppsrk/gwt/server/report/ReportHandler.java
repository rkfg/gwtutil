package ru.ppsrk.gwt.server.report;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.LogicException;
import freemarker.template.TemplateException;

public interface ReportHandler {
    /**
     * Callback for report dispatching
     * @param req servlet wrapped request
     * @param resp servlet response
     * @param empty params template parameters which should be filled by this method
     * @return template filename
     * @throws TemplateException
     * @throws IOException
     * @throws LogicException
     * @throws ClientAuthenticationException
     * @throws ClientAuthorizationException
     */
    public String exec(HttpServletRequestReportWrapper req, HttpServletResponse resp, HashMap<String, Object> params) throws TemplateException, IOException, LogicException,
            ClientAuthenticationException, ClientAuthorizationException;

    public String[] getRequiredParams();

    public String getType();
}