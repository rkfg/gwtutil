package ru.ppsrk.gwt.server;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.ClientAuthorizationException;
import ru.ppsrk.gwt.client.LogicException;
import freemarker.template.TemplateException;

public class ReportServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -9071759488979347272L;
    private HashMap<String, ReportHandler> reportHandlerMap = new HashMap<String, ReportHandler>(10);

    protected ReportServlet(ReportHandler... reportHandlers) {
        for (ReportHandler handler : reportHandlers) {
            reportHandlerMap.put(handler.getType(), handler);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("text/html; charset=utf-8");
            resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
            if (req.getParameter("type") != null) {
                ReportHandler handler = reportHandlerMap.get(req.getParameter("type"));
                if (handler != null) {
                    if (handler.getRequiredParams() != null) {
                        for (String reqParam : handler.getRequiredParams()) {
                            if (req.getParameter(reqParam) == null) {
                                throw new LogicException("Не все необходимые параметры заданы.");
                            }
                        }
                    }
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    String template = handler.exec(req, resp, params);
                    if (template != null) {
                        Freemarker.processTemplate(this, template, params, resp);
                    }
                }
            }
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (LogicException e) {
            resp.getWriter().write(e.getMessage());
        } catch (TemplateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientAuthenticationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClientAuthorizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

}
