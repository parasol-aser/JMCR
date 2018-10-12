package edu.tamu.aser.scheduling;

import java.util.Properties;

/**
 * Provides an interface for getting properties from a file, and exposing them
 * internally.
 * 
 * @author Matt Kirn <kirn1@illinois.edu>
 * 
 */
@SuppressWarnings("serial")
public class MCRProperties extends Properties {

    /* Instrumentation related properties */
    public static final String INSTRUMENTATION_PACKAGES_IGNORE_PREFIXES_KEY = "reex.instrumentation.packages.ignore.prefixes";
    public static final String INSTRUMENTATION_PACKAGES_IGNORE_KEY = "reex.instrumentation.packages.ignore";
    public static final String INSTRUMENTATION_CLASSES_IGNORE_PREFIXES_KEY = "reex.instrumentation.classes.ignore.prefixes";
    public static final String INSTRUMENTATION_CLASSES_IGNORE_KEY = "reex.instrumentation.classes.ignore";
    public static final String INSTRUMENTATION_PACKAGES_ALLOW_PREFIXES_KEY = "reex.instrumentation.packages.allow.prefixes";
    public static final String INSTRUMENTATION_PACKAGES_ALLOW_KEY = "reex.instrumentation.packages.allow";
    public static final String INSTRUMENTATION_CLASSES_ALLOW_PREFIXES_KEY = "reex.instrumentation.classes.allow.prefixes";
    public static final String INSTRUMENTATION_CLASSES_ALLOW_KEY = "reex.instrumentation.classes.allow";

    /* Scheduling related properties */
    public static final String SCHEDULING_STRATEGY_KEY = "reex.exploration.schedulingstrategy";
    public static final String SCHEDULING_REPRO_CHOICES_KEY = "reex.exploration.reprochoices";
    public static final String PREEMPTION_BOUND_KEY = "reex.exploration.preemptionbound";
    public static final String SEED_KEY = "reex.exploration.randomseed";
    public static final String MODE_KEY = "reex.locfiltering.mode";
    public static final String ALLOWED_LOCS_FILE_KEY = "reex.locfiltering.allowedfile";
    public static final String SCHEDULING_FILTER_KEY = "reex.exploration.schedulingfilter";
    public static final String STOP_ON_FIRST_ERROR_KEY = "reex.exploration.stoponfirsterror";
    public static final String RV_CAUSAL_FULL_TRACE = "reex.exploration.rvcausal.fulltrace";
    
    /* Listeners/Debugging related properties */
    public static final String LISTENERS_KEY = "reex.exploration.listeners";
    public static final String EXPLORATION_DEBUG_KEY = "reex.exploration.debug";
    public static final String INSTRUMENTATION_DEBUG_KEY = "reex.instrumentation.debug";
    public static final String EXPLORATION_TIMEOUT_KEY = "reex.exploration.timeout";

    /* Properties related property */
    public static final String PROPERTIES_KEY = "reex.properties";
    public static final String DEFAULT_PROPERTIES = "/default.properties";

    private static MCRProperties instance;

    private MCRProperties() {
        String propertiesFileLocation = System.getProperty(PROPERTIES_KEY);
        try {
            // Load defaults first
            
            if (this.getClass().getResourceAsStream(DEFAULT_PROPERTIES)==null) {
                System.err.println("No" + DEFAULT_PROPERTIES);
            }
            
            load(this.getClass().getResourceAsStream(DEFAULT_PROPERTIES));
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
        // then check reex.properties
        if (prop == null) {
            prop = super.getProperty(key);
        }
        return prop;
    }
}
