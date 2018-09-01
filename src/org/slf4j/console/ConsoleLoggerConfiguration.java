package org.slf4j.console;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ConsoleLoggerConfiguration {

	private static final String CONFIGURATION_FILE = "consolelogger.properties";
	
    private final Properties properties = new Properties();	
	
    void loadProperties() {
    	
    	loadPreferences();
    	
        InputStream in = AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
            public InputStream run() {
                ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
                if (threadCL != null) {
                    return threadCL.getResourceAsStream(CONFIGURATION_FILE);
                } else {
                    return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
                }
            }
        });
        if (null != in) {
            try {
                Properties tempProp = new Properties(); 
                tempProp.load(in);
                for (Map.Entry<Object, Object> it : tempProp.entrySet()) {
                	if (it.getKey() != null && it.getValue() != null) {
                		properties.put(ConsoleLogger.LOG_KEY_PREFIX+it.getKey().toString(), it.getValue().toString());
                	}
                }
            } catch (java.io.IOException e) {
                // ignored
            } finally {
                try {
                    in.close();
                } catch (java.io.IOException e) {
                    // ignored
                }
            }
        }
    }
    
    /** Loads logger definitions from the properties (if any are available) */
    private void loadPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(ConsoleLoggerConfiguration.class);

		String group = prefs.get(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_SELECTED_GROUP, "");
		try {
		if (!group.isEmpty() && prefs.nodeExists(group)) {
			prefs = prefs.node(group);			
			PrefProps pp = new PrefProps(prefs);
			pp.toProperties(properties);

			boolean includeTime =  prefs.getBoolean(ConsoleLogger.KEY_INCLUDE_TIME,false);
			int colWidth = prefs.getInt(ConsoleLogger.KEY_WIDTH, ConsoleLogger.DEFAULT_LOGGER_COLUMN_WIDTH);
			//TODO: don't copy to system properties...
			if (includeTime) System.getProperties().put(ConsoleLogger.KEY_INCLUDE_TIME, "1");
			if (colWidth != ConsoleLogger.DEFAULT_LOGGER_COLUMN_WIDTH) {
				System.getProperties().put(ConsoleLogger.KEY_WIDTH, String.valueOf(colWidth));
			}
		}
		} catch (BackingStoreException e) {
			//nothing to do
		}
    }
    
    String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }
    
    String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
            ; // Ignore
        }
        return (prop == null) ? properties.getProperty(name) : prop;
    }    
    
	/** There is a Preferences bug in Java on Windows (before Java 9).
	 *  https://bugs.openjdk.java.net/browse/JDK-8139507 
	 *  Thus we can not use the Java Preferences on Windows Systems 
	 *  running Java 8 or less.  */
	static boolean canUsePreferences() {
		
		if (true) {  //TODO change the way in which 
			return true;
		}
		
		String version = System.getProperty("java.version");
		int posF = version.indexOf('.');
		try {
			if (9 <= Integer.parseInt(version.substring(0, posF))) {
				return true; 
			}
			
			return !System.getProperty("os.name").startsWith("Windows");
			
		} catch (NumberFormatException ex) {
			return false; //something is really wrong here...
		}
	}
    
	
}
