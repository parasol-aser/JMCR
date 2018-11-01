package edu.tamu.aser.config;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by Alan on 7/18/18.
 */

public class ConstraintsProperties extends Properties {
    private static ConstraintsProperties instance = new ConstraintsProperties();

    /* Properties related property */
    public static final String PROPERTIES_KEY = "mcr.properties";
    public static final String DEFAULT_PROPERTIES = "/default.properties";
    private ConstraintsProperties(){
        String propertiesFileLocation = System.getProperty(PROPERTIES_KEY);
        try {
            // Load defaults first
            InputStream inputStream = null;
            inputStream = this.getClass().getResourceAsStream(DEFAULT_PROPERTIES);
            if (inputStream == null){
                System.err.println("No" + DEFAULT_PROPERTIES);
                System.exit(-1);
            }
            load(inputStream);
            inputStream.close();
            // Load user provided properties
            if (propertiesFileLocation != null) {
                load(this.getClass().getResourceAsStream(propertiesFileLocation));
            }
        } catch (Exception e) {
            System.err.println("Unable to load properties file from " + propertiesFileLocation);
            e.printStackTrace();
        }

    }

    public static ConstraintsProperties getInstance() {
        if (instance == null) {
            instance = new ConstraintsProperties();
        }
        return instance;
    }

    @Override
    public String getProperty(String key) {

        // check system properties first
        String prop = System.getProperty(key);
        // then check mcr.properties
        if (prop == null) {
            prop = super.getProperty(key);
        }
        return prop;
    }
}
