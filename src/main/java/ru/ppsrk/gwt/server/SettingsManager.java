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

public class SettingsManager implements Iterable<String> {
    private static SettingsManager instance = new SettingsManager();

    public static SettingsManager getInstance() {
        return instance;
    }

    private Map<String, String> defaults = new HashMap<String, String>();

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

    public SettingsManager loadSettings() throws FileNotFoundException, IOException {
        if (filename == null || filename.isEmpty()) {
            throw new FileNotFoundException("Set filename first.");
        }
        try {
            loadProperties(properties, expandHome(filename));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public void saveSettings() throws FileNotFoundException, IOException {
        if (filename == null || filename.isEmpty()) {
            throw new FileNotFoundException("Set filename first.");
        }
        try {
            storeProperties(properties, expandHome(filename));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void loadProperties(Properties properties, String filename) throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        properties.load(new InputStreamReader(new FileInputStream(createFileDirs(filename)), "utf-8"));
    }

    public static String expandHome(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }

    public static String createFileDirs(String filename) {
        File parentFile = new File(filename).getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();
        }
        File config = new File(filename);
        if (!config.isFile()) {
            try {
                config.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return filename;
    }

    public static void storeProperties(Properties properties, String filename) throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        properties.store(new OutputStreamWriter(new FileOutputStream(createFileDirs(filename)), "utf-8"), "");
    }

    public void setDefaults(HashMap<String, String> defaults) {
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

}
