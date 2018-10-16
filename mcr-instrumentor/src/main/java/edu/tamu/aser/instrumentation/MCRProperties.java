package edu.tamu.aser.instrumentation;

import java.io.InputStream;
import java.util.Properties;

/**
 * Provides an interface for getting properties from a file, and exposing them
 * internally.
 * 
 */
@SuppressWarnings("serial")
public class MCRProperties extends Properties {

    /* Instrumentation related properties */
    public static final String INSTRUMENTATION_PACKAGES_IGNORE_PREFIXES_KEY = "mcr.instrumentation.packages.ignore.prefixes";
    public static final String INSTRUMENTATION_PACKAGES_IGNORE_KEY = "mcr.instrumentation.packages.ignore";
    public static final String INSTRUMENTATION_CLASSES_IGNORE_PREFIXES_KEY = "mcr.instrumentation.classes.ignore.prefixes";
    public static final String INSTRUMENTATION_CLASSES_IGNORE_KEY = "mcr.instrumentation.classes.ignore";
    public static final String INSTRUMENTATION_PACKAGES_ALLOW_PREFIXES_KEY = "mcr.instrumentation.packages.allow.prefixes";
    public static final String INSTRUMENTATION_PACKAGES_ALLOW_KEY = "mcr.instrumentation.packages.allow";
    public static final String INSTRUMENTATION_CLASSES_ALLOW_PREFIXES_KEY = "mcr.instrumentation.classes.allow.prefixes";
    public static final String INSTRUMENTATION_CLASSES_ALLOW_KEY = "mcr.instrumentation.classes.allow";

    public static final String SCHEDULING_REPRO_CHOICES_KEY = "mcr.exploration.reprochoices";
    public static final String SCHEDULING_STRATEGY_KEY = "mcr.exploration.scheduling.strategy";

    //memory model, whether using static dependency analysis
    public static final String memModel_KEY = "mm";
    
    /* Listeners/Debugging related properties */
    public static final String LISTENERS_KEY = "mcr.exploration.listeners";
    public static final String EXPLORATION_DEBUG_KEY = "mcr.exploration.debug";
    public static final String EXPLORATION_TIMEOUT_KEY = "mcr.exploration.timeout";

    /* Properties related property */
    public static final String PROPERTIES_KEY = "mcr.properties";
    public static final String DEFAULT_PROPERTIES = "/default.properties";

    private static MCRProperties instance;

    private MCRProperties() {
        String propertiesFileLocation = System.getProperty(PROPERTIES_KEY);
        try {
            // Load defaults first
            InputStream inputStream = null;
            inputStream = this.getClass().getResourceAsStream(DEFAULT_PROPERTIES);
            if (inputStream == null){
                System.err.println("No " + DEFAULT_PROPERTIES);
                throw new IllegalAccessException();
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

    public static MCRProperties getInstance() {
        if (instance == null) {
            instance = new MCRProperties();
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
