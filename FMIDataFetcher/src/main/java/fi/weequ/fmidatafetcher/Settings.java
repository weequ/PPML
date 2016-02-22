package fi.weequ.fmidatafetcher;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {
    
    private static Properties properties = null;
    
    public static synchronized String getProperty(String key) {
        try {
            if (properties == null) {
                properties = new Properties();
                properties.load(new FileInputStream("/data/PPML/settings.properties"));
            }
            return properties.getProperty(key);
        } catch(Exception ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
