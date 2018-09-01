/**
 * Copyright (c) 2004-2012 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.slf4j.console;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.spi.LocationAwareLogger;


/**
 * <p>Simple implementation of a {@link Logger} that requires minimal 
 * configuration and sends all enabled log messages, for all defined loggers to the console.
 *  
 * This logger aims to provide developers with a zero-cost and instant benefit 
 * transition from using System.out.println() statements to logging. 
 * Developers only have to add this small jar and the standard SLF4J-api jar to 
 * their project. They don't have to configure loggers via property files or 
 * command line arguments; instead they can double-click the jar file 
 * and quickly set or deactivate any logger with minimal typing. To set the 
 * class MyClass in com.mypackage.MyClass to trace level, click the add button, 
 * select a sheet name for your logger configuration, add the following line 
 * and then click the save button:   
 * <pre>
 * com.mypackage.MyClass:trace
 * </pre>
 * The level can also be abbreviated by typing only the first letter. To set the 
 * log level to OFF, deactivating the logger use:
 * <pre>com.mypackage.MyClass:off</pre>
 * or
 * <pre>com.mypackage.MyClass:o</pre>
 * 
 * If no log level is specified on subsequent lines or no package name is specified on 
 * subsequent lines, then the log level of the most recent package will be assigned. 
 * 
 * Log levels can also be set via System properties. To set the logger 
 * <code>com.mypackage.MyClass</code> to the warn level, add a property with the key of 
 * <code>org.slf4j.console.com.mypackage.MyClass</code> and a value of <code>warn</code>
 * to System.getProperties(). This can also be set when invoking the Java Virtual Machine
 * by adding the argument
 * <pre>
 * -Dorg.slf4j.console.com.mypackage.MyClass=trace
 * </pre>
 * 
 * Log levels can also be set via a properties file with the name <code>consolelogger.properties</code>.
 * To set the logger <code>com.mypackage.MyClass</code> to the warn level, add the following 
 * line to this properties file (in contrast to most other logging properties files, no prefix to the 
 * class name is needed):
 * <pre>
 * com.mypackage.MyClass:warn
 * </pre> 
 * 
 * The log output squarely focuses on the message's content; contextual information is 
 * kept very short. The output can only be customized in two ways: 
 * 1) Modify the column width for the logger name. Default is 10 characters. Any logger names that are 
 * larger than the column width are cut off after this number of characters. Add the line  
 * <code>width=15</code> to the consolelogger.properties file or <code>-Dorg.slf4j.console.width=15</code> as a command line argument 
 * when invoking the Java Virtual Machine. 
 * 2) Add a time stamp to the log output. Add the following line: <code>time=true</code> to the 
 * consolelogger.properties file or <code>-Dorg.slf4j.console.time=true</code> as a command line argument 
 * when invoking the Java Virtual Machine.   
 * 
 * An example output is shown below. Logger names are cut off after 10 characters
 * and levels are abbreviated to a single character. 
 * 
 * By default, every logger is set to info level. All levels except error are sent 
 * to {@code System.out} and the error level is sent to ({@code System.err}). 
 *   
 * 
 *  <pre>
AppView    d| display dimensions 960x880
..         d| initializing to array size 1020000
..         d| channel caption 0 size 180
PulseChann d| started 2017-01-12T14:33:47 
GraphAnaly t| channel 1 size 114
..         t| channel 2 size 8597
..         t| channel 3 size 0
..         W| Channel 3 is empty!
..         d| retrieved 3 edges
ScopeFrame d| channels: 3
PulseChann d| sample rate 100000.0
..         d| series count 362
..         d| sample rate 100000.0
..         d| series count 8
</pre>
 * 
 *
 * <p>This implementation was derived from org.slf4j's SimpleLogger 
 * class (in package org.slf4j.impl) which was authored by</p>
 * <p>
 * @author Ceki G&uuml;lc&uuml;
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME<br>
 * and which in turn was heavily inspired by  
 *
 * <a href="http://commons.apache.org/logging/">Apache Commons Logging</a>'s SimpleLog.</p>
 * </p>
 */
public class ConsoleLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = -632788891211436180L;

    private static final int LOG_LEVEL_TRACE = LocationAwareLogger.TRACE_INT;
    private static final int LOG_LEVEL_DEBUG = LocationAwareLogger.DEBUG_INT;
    private static final int LOG_LEVEL_INFO = LocationAwareLogger.INFO_INT;
    private static final int LOG_LEVEL_WARN = LocationAwareLogger.WARN_INT;
    private static final int LOG_LEVEL_ERROR = LocationAwareLogger.ERROR_INT;
    
    // The OFF level can only be used in configuration files to disable logging. It has
    // no printing method associated with it in o.s.Logger interface.
    private static final int LOG_LEVEL_OFF = LOG_LEVEL_ERROR + 10;

    private static int DEFAULT_LOG_LEVEL = LOG_LEVEL_INFO;

    public static final String LOG_KEY_PREFIX = "org.slf4j.console.";
    
    /** Prefix for a property that determines that time values (minutes, seconds and nanos) should 
     * be included in the log output. Existence of this key (no matter what the value is) 
     * will lead to time being displayed.  */
    public static final String KEY_INCLUDE_TIME = LOG_KEY_PREFIX+"time";
    
    /** Prefix for a property that determines the fixed width used to display the logger name. 
     *  Default value is set to 12.  */
    public static final String KEY_WIDTH = LOG_KEY_PREFIX+"width";
    
    /** This key is used for storing logger definitions to preferences via {@link ConfigFrame} */
    static final String KEY_PREFERENCES_LOGGER_BLOB = LOG_KEY_PREFIX+"loggers";

    /** This key determines which preferences group is active */
	static final String KEY_PREFERENCES_ATTRIBUTE_SELECTED_GROUP = "selected";
	
	/** The text attribute of a logger group (refers to the text which contains 
	 *  the various logger definition statements). */
	static final String KEY_PREFERENCES_ATTRIBUTE_TEXT = "text";
	
	static final String KEY_PREFERENCES_ATTRIBUTE_TIMESTAMP = "";	
    
    static final int DEFAULT_LOGGER_COLUMN_WIDTH = 10;

    
    private static boolean includeTime;
    private static DateTimeFormatter timeStampFormatter;
    private static int loggerNameLength = DEFAULT_LOGGER_COLUMN_WIDTH;
    private static String pad =     "          ";
    private static String padDots = "..        ";
    
    /** The current log level */
    protected int currentLogLevel = LOG_LEVEL_INFO;

    /** Package access: allows only {@link ConsoleLoggerFactory} to instantiate
     * ConsoleLogger instances.     */
    ConsoleLogger(String name) {
        this.name = name;

        String levelString = recursivelyComputeLevelString();
        if (levelString != null) {
            this.currentLogLevel = stringToLevel(levelString);
        } else {
            this.currentLogLevel = DEFAULT_LOG_LEVEL;
        }
    }

    private static boolean INITIALIZED = false;   
    static ConsoleLoggerConfiguration CONFIG_PARAMS = null;    
    
    static void lazyInit() {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        init();
    }
    
    // external software might be invoking this method directly. Do not rename
    // or change its semantics.
    static void init() {
    	CONFIG_PARAMS = new ConsoleLoggerConfiguration();
    	CONFIG_PARAMS.loadProperties();
    	includeTime = ( System.getProperty(ConsoleLogger.KEY_INCLUDE_TIME) != null );
    	if (includeTime) {
    		timeStampFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS ");
    	}
    	String w = System.getProperty(ConsoleLogger.KEY_WIDTH); 
    	if (w != null) {
    		pad = "";
    		try {
    			loggerNameLength = Integer.parseInt(w); 
    			for (int i=0;i< (loggerNameLength==0 ? 0 : loggerNameLength+1);i++) {
    				pad += " ";
    			}
    		} catch (NumberFormatException ex) {
    			System.err.println("Console logger width ("+w+") is not a valid integer value (NumberFormatException). Using default width instead.");
    		}
    		
			if (pad.length() > 2) {
				padDots = ".." + pad.substring(2);
			} else {
				padDots = pad;
			}
    	}
    }    
    
    private transient String veryShortName; 
    private transient int longNameHashCode; 
    
    /** Last hash code used when printing to console. */
    static AtomicInteger lastHashCode = new AtomicInteger(); 
    
    private void computeVeryShortName() { 
    	int posF = name.lastIndexOf('.')+1;
    	if (name.endsWith("_T") || name.endsWith("_D")) {
    		veryShortName = name.substring(posF,Math.min(name.length()-2, posF+loggerNameLength));
    	} else {
    		veryShortName = name.substring(posF,Math.min(name.length(), posF+loggerNameLength)); 	
    	}
   		veryShortName += pad.substring(0, pad.length()-veryShortName.length()); //one empty space after short name
   		this.longNameHashCode = name.hashCode();
    }
    
    String recursivelyComputeLevelString() {
    	//allows us to quickly set loggers into debug or trace mode when debugging
//    	if (name.endsWith("_T")) {
//    		reportLevelEscalation(true);
//    		return "trace";
//    	} else if (name.endsWith("_D")) {
//    		reportLevelEscalation(false);
//    		return "debug"; 
//    	}
    	
        String tempName = name;        
        String levelString = null;
        int indexOfLastDot = tempName.length();
        while ((levelString == null) && (indexOfLastDot > -1)) {
            tempName = tempName.substring(0, indexOfLastDot);
            levelString = CONFIG_PARAMS.getStringProperty(LOG_KEY_PREFIX + tempName, null);
            indexOfLastDot = String.valueOf(tempName).lastIndexOf('.');
        }
        
        return levelString;
    }
    
    /** Write information to log that we have a level escalation where developer has 
     *  appended _D or _T to the name of the logger, which switches the logger into a 
     *  debug or trace mode.  */
    private void reportLevelEscalation(boolean traceLevel) {
    	int posF = name.lastIndexOf('.')+1;
    	String shortName = name.substring(posF, name.length()-2);
    	String txt = name.substring(0,posF-1); 
    	if (txt.startsWith("class ")) txt = txt.substring(6);
    	log(LOG_LEVEL_INFO, (traceLevel ? "+\"_T\" => TRACE " : "+\"_D\" => DEBUG ") + shortName + " ("+txt+")",null);
    			
    }

    private static int stringToLevel(String levelStr) {
        if ("trace".equalsIgnoreCase(levelStr)) {
            return LOG_LEVEL_TRACE;
        } else if ("debug".equalsIgnoreCase(levelStr)) {
            return LOG_LEVEL_DEBUG;
        } else if ("info".equalsIgnoreCase(levelStr)) {
            return LOG_LEVEL_INFO;
        } else if ("warn".equalsIgnoreCase(levelStr)) {
            return LOG_LEVEL_WARN;
        } else if ("error".equalsIgnoreCase(levelStr)) {
            return LOG_LEVEL_ERROR;
        } else if ("off".equalsIgnoreCase(levelStr)) {
            return LOG_LEVEL_OFF;
        }
        // assume INFO by default
        return LOG_LEVEL_INFO;
    }

    /**
     * This is our internal implementation for logging regular (non-parameterized)
     * log messages.
     *
     * @param level   One of the LOG_LEVEL_XXX constants defining the log level
     * @param message The message itself
     * @param t       The exception whose stack trace should be logged
     */
    private void log(int level, String message, Throwable t) {
        if (!isLevelEnabled(level)) {
            return;
        }

        StringBuilder buf = new StringBuilder(32);
        
        if (includeTime) {
        	buf.append(timeStampFormatter.format(LocalTime.now()));
        }
        
        if (veryShortName == null) computeVeryShortName(); 
        
        //TODO: synchronization issues can play a role here!
        if (this.longNameHashCode == ConsoleLogger.lastHashCode.getAndSet(longNameHashCode)) {
        	buf.append(padDots);
        } else {
        	buf.append(veryShortName); 
        }


        // Append a readable representation of the log level
        switch (level) {
        case LOG_LEVEL_TRACE:
            buf.append(" t| ");
            break;
        case LOG_LEVEL_DEBUG:
            buf.append(" d| ");
            break;
        case LOG_LEVEL_INFO:
            buf.append(" i| "); 
            break;
        case LOG_LEVEL_WARN:
            buf.append(" W| ");
            break;
        case LOG_LEVEL_ERROR:
            buf.append(" E| ");
            break;
        }

        // Append the message
        buf.append(message);

        write(LOG_LEVEL_WARN <= level ? System.err : System.out, buf, t); 

    }

    void write(PrintStream stream, StringBuilder buf, Throwable t) {
        stream.println(buf.toString());
        if (t != null) {
            t.printStackTrace(stream);
        }
        stream.flush();
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level
     * @param format
     * @param arg1
     * @param arg2
     */
    private void formatAndLog(int level, String format, Object arg1, Object arg2) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatterEx.format(format, arg1, arg2);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level
     * @param format
     * @param arguments a list of 3 ore more arguments
     */
    private void formatAndLog(int level, String format, Object... arguments) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatterEx.arrayFormat(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    protected boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }

    /** Are {@code trace} messages currently enabled? */
    public boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    /**
     * A simple implementation which logs messages of level TRACE according
     * to the format outlined above.
     */
    public void trace(String msg) {
        log(LOG_LEVEL_TRACE, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * TRACE according to the format outlined above.
     */
    public void trace(String format, Object param1) {
        formatAndLog(LOG_LEVEL_TRACE, format, param1, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * TRACE according to the format outlined above.
     */
    public void trace(String format, Object param1, Object param2) {
        formatAndLog(LOG_LEVEL_TRACE, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * TRACE according to the format outlined above.
     */
    public void trace(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_TRACE, format, argArray);
    }

    /** Log a message of level TRACE, including an exception. */
    public void trace(String msg, Throwable t) {
        log(LOG_LEVEL_TRACE, msg, t);
    }

    /** Are {@code debug} messages currently enabled? */
    public boolean isDebugEnabled() {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    /**
     * A simple implementation which logs messages of level DEBUG according
     * to the format outlined above.
     */
    public void debug(String msg) {
        log(LOG_LEVEL_DEBUG, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * DEBUG according to the format outlined above.
     */
    public void debug(String format, Object param1) {
        formatAndLog(LOG_LEVEL_DEBUG, format, param1, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * DEBUG according to the format outlined above.
     */
    public void debug(String format, Object param1, Object param2) {
        formatAndLog(LOG_LEVEL_DEBUG, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * DEBUG according to the format outlined above.
     */
    public void debug(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_DEBUG, format, argArray);
    }

    /** Log a message of level DEBUG, including an exception. */
    public void debug(String msg, Throwable t) {
        log(LOG_LEVEL_DEBUG, msg, t);
    }

    /** Are {@code info} messages currently enabled? */
    public boolean isInfoEnabled() {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    /**
     * A simple implementation which logs messages of level INFO according
     * to the format outlined above.
     */
    public void info(String msg) {
        log(LOG_LEVEL_INFO, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * INFO according to the format outlined above.
     */
    public void info(String format, Object arg) {
        formatAndLog(LOG_LEVEL_INFO, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * INFO according to the format outlined above.
     */
    public void info(String format, Object arg1, Object arg2) {
        formatAndLog(LOG_LEVEL_INFO, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * INFO according to the format outlined above.
     */
    public void info(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_INFO, format, argArray);
    }

    /** Log a message of level INFO, including an exception. */
    public void info(String msg, Throwable t) {
        log(LOG_LEVEL_INFO, msg, t);
    }

    /** Are {@code warn} messages currently enabled? */
    public boolean isWarnEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    /**
     * A simple implementation which always logs messages of level WARN according
     * to the format outlined above.
     */
    public void warn(String msg) {
        log(LOG_LEVEL_WARN, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * WARN according to the format outlined above.
     */
    public void warn(String format, Object arg) {
        formatAndLog(LOG_LEVEL_WARN, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * WARN according to the format outlined above.
     */
    public void warn(String format, Object arg1, Object arg2) {
        formatAndLog(LOG_LEVEL_WARN, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * WARN according to the format outlined above.
     */
    public void warn(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_WARN, format, argArray);
    }

    /** Log a message of level WARN, including an exception. */
    public void warn(String msg, Throwable t) {
        log(LOG_LEVEL_WARN, msg, t);
    }

    /** Are {@code error} messages currently enabled? */
    public boolean isErrorEnabled() {
        return isLevelEnabled(LOG_LEVEL_ERROR);
    }

    /**
     * A simple implementation which always logs messages of level ERROR according
     * to the format outlined above.
     */
    public void error(String msg) {
        log(LOG_LEVEL_ERROR, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * ERROR according to the format outlined above.
     */
    public void error(String format, Object arg) {
        formatAndLog(LOG_LEVEL_ERROR, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * ERROR according to the format outlined above.
     */
    public void error(String format, Object arg1, Object arg2) {
        formatAndLog(LOG_LEVEL_ERROR, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * ERROR according to the format outlined above.
     */
    public void error(String format, Object... argArray) {
        formatAndLog(LOG_LEVEL_ERROR, format, argArray);
    }

    /** Log a message of level ERROR, including an exception. */
    public void error(String msg, Throwable t) {
        log(LOG_LEVEL_ERROR, msg, t);
    }

    public void log(LoggingEvent event) {
        int levelInt = event.getLevel().toInt();

        if (!isLevelEnabled(levelInt)) {
            return;
        }
        FormattingTuple tp = MessageFormatterEx.arrayFormat(event.getMessage(), event.getArgumentArray(), event.getThrowable());
        log(levelInt, tp.getMessage(), event.getThrowable());
    }

}
