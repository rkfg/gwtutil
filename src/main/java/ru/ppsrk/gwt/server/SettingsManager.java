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
import java.util.Properties;

public class SettingsManager {
    private static SettingsManager instance = new SettingsManager();

    public static SettingsManager getInstance() {
        return instance;
    }

    private HashMap<String, String> defaults = new HashMap<String, String>();

    Properties properties = new Properties();
    String filename = null;

    public SettingsManager() {
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

    public void loadSettings() throws FileNotFoundException, IOException {
        if (filename == null || filename.isEmpty()) {
            throw new FileNotFoundException("Set filename first.");
        }
        try {
            loadProperties(properties, expandHome(filename));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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

    public HashMap<String, String> getDefaults() {
        return defaults;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

}
