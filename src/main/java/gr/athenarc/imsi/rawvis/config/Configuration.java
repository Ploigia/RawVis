package gr.athenarc.imsi.rawvis.config;

import gr.athenarc.imsi.rawvis.common.Rectangle;
import gr.athenarc.imsi.rawvis.util.Utils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Configuration {
    private static Configuration instance;
    private PropertiesConfiguration propertiesConfig;

    private Configuration() throws ConfigurationException {
        propertiesConfig = new PropertiesConfiguration("config.properties");
    }

    public static Configuration getInstance() {
        if (null == instance) {
            try {
                instance = new Configuration();
            } catch (ConfigurationException ex) {
                throw new RuntimeException(ex);
            }
        }
        return instance;
    }

    public PropertiesConfiguration getPropertiesConfig() {
        return propertiesConfig;
    }

    public void setPropertiesConfig(PropertiesConfiguration propertiesConfig) {
        this.propertiesConfig = propertiesConfig;
    }

    public static String getDelimiter() {
        return getInstance().getPropertiesConfig().getString("delimiter");
    }

    public static String getCsvFilePath() {
        return getInstance().getPropertiesConfig().getString("path");
    }

    public static String getThreshold() {
        return getInstance().getPropertiesConfig().getString("threshold");
    }
    
    public static String getLineSeparator() {
        return getInstance().getPropertiesConfig().getString("line_separator");
    }

    public static boolean isWithHeader() {
        return getInstance().getPropertiesConfig().getBoolean("with_header", false);
    }

    public static Rectangle getBounds() {
        return new Rectangle(Utils.convertToRange(getInstance().getPropertiesConfig().getString("x_range")),
                Utils.convertToRange(getInstance().getPropertiesConfig().getString("y_range")));
    }
}