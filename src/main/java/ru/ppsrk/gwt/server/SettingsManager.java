package ru.ppsrk.gwt.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsManager implements Iterable<String> {

    Logger log = LoggerFactory.getLogger(getClass());

    private static SettingsManager instance = new SettingsManager();

    public static SettingsManager getInstance() {
        return instance;
    }

    private Map<String, String> defaults = new HashMap<>();

    Properties properties = new Properties();
    String filename = null;

    public SettingsManager() {
    }

    public SettingsManager(String filename) {
        this.filename = filename;
    }

    public Integer getIntegerSetting(String key) throws NumberFormatException {
        return Integer.valueOf(properties.getProperty(key, defaults.get(key)));
    }

    public Long getLongSetting(String key) {
        return Long.valueOf(properties.getProperty(key, defaults.get(key)));
    }

    public String getStringSetting(String key) {
        return properties.getProperty(key, defaults.get(key));
    }

    public SettingsManager loadSettings() throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new FileNotFoundException("Set filename first.");
        }
        try {
            loadProperties(properties, expandHome(filename));
        } catch (UnsupportedEncodingException e) {
            log.error("Couldn't load settings", e);
        }
        return this;
    }

    public void saveSettings() throws IOException {
        if (filename == null || filename.isEmpty()) {
            throw new FileNotFoundException("Set filename first.");
        }
        try {
            storeProperties(properties, expandHome(filename));
        } catch (UnsupportedEncodingException e) {
            log.error("Couldn't save settings", e);
        }
    }

    public static void loadProperties(Properties properties, String filename) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(new FileInputStream(createFileDirs(filename)), "utf-8")) {
            properties.load(isr);
        }
    }

    public static String expandHome(String path) {
        if (path.startsWith("~" + File.separator)) {
            return System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static String createFileDirs(String filename) throws IOException {
        File parentFile = new File(filename).getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        File config = new File(filename);
        if (!config.isFile() && !config.createNewFile()) {
            throw new IOException("Can't create config file " + filename);
        }
        return filename;
    }

    public static void storeProperties(Properties properties, String filename) throws IOException {
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(createFileDirs(filename)), "utf-8")) {
            properties.store(osw, "");
        }
    }

    public void setDefaults(Map<String, String> defaults) {
        this.defaults = defaults;
    }

    public SettingsManager setDefault(String key, String value) {
        defaults.put(key, value);
        return this;
    }

    public SettingsManager setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public void setIntegerSetting(String key, Integer val) {
        properties.setProperty(key, val.toString());
    }

    public void setLongSetting(String key, Long val) {
        properties.setProperty(key, val.toString());
    }

    public void setStringSetting(String key, String val) {
        properties.setProperty(key, val);
    }

    private class StringIteratorDecorator implements Iterator<String> {
        private Iterator<?> iter;

        public StringIteratorDecorator(Iterator<?> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public String next() {
            return (String) iter.next();
        }

        @Override
        public void remove() {
            iter.remove();
        }

    }

    @Override
    public Iterator<String> iterator() {
        return new StringIteratorDecorator(properties.keySet().iterator());
    }

    public void remove(String key) {
        properties.remove(key);
    }

}
