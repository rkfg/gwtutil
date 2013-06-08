package ru.ppsrk.gwt.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Properties;


public class SettingsManager {
    private static SettingsManager instance = new SettingsManager();

    private HashMap<String, String> defaults;
    
    Properties properties = new Properties();

    public static SettingsManager getInstance() {
        return instance;
    }

    private SettingsManager() {
    }

    public void loadSettings(String filename) {
        try {
            ServerUtils.loadProperties(properties, filename);
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

    public String getSetting(String key) {
        return properties.getProperty(key, defaults.get(key));
    }

    public void setDefaults(HashMap<String, String> defaults) {
        this.defaults = defaults;
    }

}
