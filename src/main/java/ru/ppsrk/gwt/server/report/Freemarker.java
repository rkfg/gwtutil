package ru.ppsrk.gwt.server.report;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.GenericServlet;
import javax.servlet.http.HttpServletResponse;

import net.lightoze.gwt.i18n.server.LocaleProxy;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Freemarker {

    private static Map<String, Configuration> cfgs = new HashMap<String, Configuration>();

    private static Configuration getCfg(GenericServlet servlet) throws IOException {
        String localeStr = "default";
        Locale gwtLocale = LocaleProxy.getLocale();
        if (gwtLocale != null) {
            localeStr = gwtLocale.toString();
        }
        Configuration cfg = cfgs.get(localeStr);
        if (cfg == null) {
            cfg = new Configuration(Configuration.getVersion());
            cfg.setDirectoryForTemplateLoading(new File(servlet.getServletContext().getRealPath("/WEB-INF/templates")));
            if (gwtLocale != null) {
                cfg.setLocale(gwtLocale);
            }
            cfgs.put(localeStr, cfg);
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
        processTemplate(servlet, templateName, params, resp.getWriter());
    }

    public static void processTemplate(GenericServlet servlet, String templateName, Map<String, Object> params, Writer writer)
            throws TemplateException, IOException {
        getCfg(servlet).getTemplate(templateName).process(params, writer);
    }

}
