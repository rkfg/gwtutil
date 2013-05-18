package ru.ppsrk.gwt.server;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Freemarker {

    private static Configuration cfg;

    private static Configuration getCfg(GenericServlet servlet) throws IOException {
        if (cfg == null) {
            cfg = new Configuration();
            cfg.setDirectoryForTemplateLoading(new File(servlet.getServletContext().getRealPath("/WEB-INF/templates")));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
        }
        return cfg;
    }

    public static Template getTemplate(GenericServlet servlet, String templateName) {
        try {
            return getCfg(servlet).getTemplate(templateName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static void processTemplate(GenericServlet servlet, String templateName, Map<String, Object> params, HttpServletResponse resp)
            throws TemplateException, IOException {
        resp.setContentType("text/html; charset=utf-8");
        resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        getCfg(servlet).getTemplate(templateName).process(params, resp.getWriter());
    }

}
