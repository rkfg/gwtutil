package ru.ppsrk.gwt.server;

import java.io.FileNotFoundException;
import java.io.IOException;
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

    public void loadSettings(String filename) {
        try {
            ServerUtils.loadProperties(properties, ServerUtils.expandHome(filename));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setDefaults(HashMap<String, String> defaults) {
        this.defaults = defaults;
    }

}
