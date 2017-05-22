package ru.ppsrk.gwt.server.report;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.programmisty.numerals.Numerals;

import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.GwtUtilException;
import ru.ppsrk.gwt.client.LogicException;

public abstract class ReportServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = -9071759488979347272L;
    private Map<String, ReportHandler> reportHandlerMap = new HashMap<>(10);

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
                    Map<String, Object> params = new HashMap<>();
                    params.put("num2textru", new TemplateMethodModelEx() {

                        @Override
                        public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
                            return Numerals.russian(Long.valueOf(arguments.get(0).toString()));
                        }
                    });
                    params.put("num2rubles", new TemplateMethodModelEx() {

                        @Override
                        public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
                            return Numerals.russianRubles(Long.valueOf(arguments.get(0).toString()));
                        }
                    });

                    String template = handler.exec(new HttpServletRequestReportWrapper(req), resp, params);
                    if (template != null) {
                        Freemarker.processTemplate(this, template, params, resp);
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ClientAuthException e) {
            e.printStackTrace();
        } catch (GwtUtilException e) {
            resp.getWriter().write(e.getMessage());
        } catch (TemplateException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

}
