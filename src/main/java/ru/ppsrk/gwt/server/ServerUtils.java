package ru.ppsrk.gwt.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.dozer.DozerBeanMapper;
import org.hibernate.Session;

import ru.ppsrk.gwt.client.AlertRuntimeException;
import ru.ppsrk.gwt.client.ClientAuthException;
import ru.ppsrk.gwt.client.ClientAuthenticationException;
import ru.ppsrk.gwt.client.LogicException;
import ru.ppsrk.gwt.shared.SharedUtils;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.validation.client.AbstractValidationMessageResolver;

public class ServerUtils {

    private static Validator validator = null;
    private static DozerBeanMapper mapper = new DozerBeanMapper();
    private static TelemetryServiceImpl telemetryService = new TelemetryServiceImpl();

    public interface MapperHint {
        public Class<?>[][] getMapperHints();
    };

    protected static void cleanup() {
        System.out.println("Cleaning up ServerUtils...");
        mapper.destroy();
        mapper = null;
    }

    public static List<String> ean13(String code) {
        if (code.length() == 12) { // calc 13th number
            int n = 0, sum = 0;
            for (char c : code.toCharArray()) {
                sum += (c - 48) * (n++ % 2 != 0 ? 3 : 1);
            }
            code += (10 - sum % 10) % 10;
        } else if (code.length() != 13) {
            return new ArrayList<String>();
        }

        List<String> schemas = Arrays.asList("LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG", "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL",
                "LGGLGL");
        ArrayList<String> elements = new ArrayList<String>();
        elements.add("S");
        int pos = 0;
        for (char c : code.substring(1, 7).toCharArray()) {
            elements.add(schemas.get(code.charAt(0) - 48).charAt(pos++) + String.valueOf(c));
        }
        elements.add("SM");
        for (char c : code.substring(7, 13).toCharArray()) {
            elements.add("R" + String.valueOf(c));
        }
        elements.add("S");
        return elements;
    }

    public static <ST, DT> List<DT> mapArray(Collection<ST> list, Class<DT> destClass) {
        List<DT> result = new ArrayList<DT>();
        for (ST elem : list) {
            if (elem != null) {
                result.add(mapModel(elem, destClass));
            }
        }
        return result;
    }

    public static <ST, DT, H extends MapperHint> List<DT> mapArray(Collection<ST> list, Class<DT> destClass, Class<H> hintClass) {
        List<DT> result = new ArrayList<DT>();
        for (ST elem : list) {
            if (elem != null) {
                result.add(mapModel(elem, destClass, hintClass));
            }
        }
        return result;
    }

    public static <T> T mapModel(Object value, Class<T> classDTO) {
        if (value == null) {
            try {
                return classDTO.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mapper.map(value, classDTO);
    }

    @SuppressWarnings("unchecked")
    public static <T, H extends MapperHint> T mapModel(Object value, Class<T> classDTO, Class<H> hintClass) {
        try {
            Class<?>[][] mapping = hintClass.newInstance().getMapperHints();
            if (mapping.length != 2) {
                throw new AlertRuntimeException("Invalid MapperHint class size, expected 2, got: " + mapping.length);
            }
            if (mapping[0].length != mapping[1].length) {
                throw new AlertRuntimeException("Non-equal MapperHint class sizes: " + mapping[0].length + " / " + mapping[1].length);
            }
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < mapping[i].length; j++) {
                    if (mapping[i][j].equals(value.getClass())) {
                        return (T) mapper.map(value, mapping[1 - i][j]);
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new AlertRuntimeException("Couldn't find the valid mapping class for " + value.toString());
    }

    public static void printStackTrace() {
        System.out.println("--------------------------");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
        System.out.println("--------------------------");
    }

    public static void resetTables(final String[] tables) throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthenticationException {
                session.createSQLQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
                for (String table : tables) {
                    session.createSQLQuery("truncate table " + table).executeUpdate();
                    session.createSQLQuery("ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS id INT AUTO_INCREMENT").executeUpdate();
                    session.createSQLQuery("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH 1").executeUpdate();
                }
                session.createSQLQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
                return null;
            }
        });

    }

    public static void importSQL(final String sqlFilename) throws LogicException, ClientAuthException {
        HibernateUtil.exec(new HibernateCallback<Void>() {

            @Override
            public Void run(Session session) throws LogicException, ClientAuthenticationException {
                try {
                    session.createSQLQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
                    File sql = new File(Thread.currentThread().getContextClassLoader().getResource(sqlFilename).getPath());
                    Scanner scan;
                    scan = new Scanner(sql);
                    StringBuilder sb = new StringBuilder((int) sql.length());
                    String line;
                    while (scan.hasNextLine()) {
                        line = scan.nextLine();
                        sb.append(line);
                        if (line.endsWith(";")) {
                            session.createSQLQuery(sb.toString()).executeUpdate();
                            sb.setLength(0);
                        }
                        sb.append('\n');
                    }
                    scan.close();
                    session.createSQLQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public static void setMappingFiles(List<String> files) {
        mapper.setMappingFiles(files);
    }

    public static void setMappingFile(String filename) {
        setMappingFiles(Arrays.asList(filename));
    }

    public static void sendErrorTelemetry(Throwable e) throws LogicException, ClientAuthException {
        String result = SharedUtils.getTelemetryString(e.getCause());
        telemetryService.sendTelemetry(result);
    }

    public static Validator getValidator(final ConstantsWithLookup messages) {
        if (validator == null) {
            validator = Validation.byDefaultProvider().configure().messageInterpolator(new MessageInterpolator() {

                private AbstractValidationMessageResolver messagesResolver = new AbstractValidationMessageResolver(messages) {
                };

                @Override
                public String interpolate(String messageTemplate, Context context, Locale locale) {
                    return messagesResolver.get(messageTemplate.replaceAll("[{}]", ""));
                }

                @Override
                public String interpolate(String messageTemplate, Context context) {
                    return messagesResolver.get(messageTemplate.replaceAll("[{}]", ""));
                }
            }).buildValidatorFactory().getValidator();
        }
        return validator;
    }

    private static BeanUtilsBean beanMerger = new BeanUtilsBean() {
        public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException,
                java.lang.reflect.InvocationTargetException {
            if (value == null) {
                return;
            }
            super.copyProperty(bean, name, value);
        };
    };

    /**
     * Copies non-null fields from source bean to target (from <a href=
     * "http://stackoverflow.com/questions/1301697/helper-in-order-to-copy-non-null-properties-from-object-to-another-java"
     * >StackOverflow question</a>)
     * 
     * @param source
     *            source bean
     * @param target
     *            target bean
     * @throws LogicException
     *             if copying has failed.
     */

    public static void mergeBeans(Object source, Object target) throws LogicException {
        try {
            beanMerger.copyProperties(target, source);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new LogicException(e.getMessage());
        }
    }

}
