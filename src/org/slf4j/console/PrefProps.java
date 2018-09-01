/**
 * Copyright (c) 2017 alexander233
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

import java.util.Properties;
import java.util.prefs.Preferences;

/** A class that helps processing texts containing preferences information and writing 
 *  and reading them to / from the Java preferences store. 
 *  
 *  Rules for writing the properties configuration: 
 *  One line per logger (or package) definition
 *  Loggers inherit the log level defined above them (but only from loggers with fully qualified class names or from packages, not from short class names). 
 *  Log levels may either be written in full ('debug') or they may just use the first letter ('d').
 *  Log levels may be specified after the logger name they must be preceded by a ':'. 
 *   
 *  Comments may be added by prefixing a line with "#"
 *  Empty lines are allowed.
 *   
 *  If a logger is only specified as a short class name (i.e. "MyClass" instead of "com.mycompany.myapp.MyClass")
 *  then the logger is considered to be part of the nearest package defined in the lines above. 
 *  A package name that ends with a '.' is used only as package name for short-named classes in subsequent lines. No logger 
 *  will created for such a package.  
 *  
 *  Examples of the logger definitions. 
 *  The first three examples all have the same result: they set the logger for com.mypackage.MyClass to 
 *  the debug level. Example (4) introduces the package com.mypackage. The trailing '.' indicates that 
 *  this line is only used to set the package context for the short class names appearing in the next lines. 
 *  No logger named 'com.mypackage' will be created. The trace level specified for the package will 
 *  applied to all subsequent lines that don't have an explicit log level. Thus com.mypackage.MyClass,
 *  com.mypackage.MyThirdClass and the package or.myorganisation.ui will all be set to trace level. 
 *  com.mypackage.MyOtherClass will be set to error level. 
 *  (1) com.mypackage.MyClass: debug
 *  (2) com.mypackage.MyClass:d
 *  (3) com.mypackage.MyClass=DEBUG
 *  
 *  (4) #This sets com.mypackage.MyClass and com.mypackage.MyThirdClass to trace level
 *      #and com.mypackage.MyOtherClass is set to Error level. No com.mypackage logger is created. 
 *      com.mypackage. : trace
 *      MyClass
 *      MyOtherClass error
 *      MyThirdClass
 *      #This package level logger inherits the trace level from the com.mypackage line 
 *      org.myorganisation.ui 
 *      
 *  */
public class PrefProps {

	private String text; 
	
	/** Creates a preferences node from a text written by the user. Don't use this to load 
	 *  from Preferences from the Java preferences store.  */
	public PrefProps(String text) {
		this.text = processText(text);
	}
	
	/** For unit testing */
	public PrefProps() {
		this.text = ""; 
	}
	
	public PrefProps(Preferences node) {
		this.text = node.get(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_TEXT, "");
	}
	
	public void save(Preferences node) {
	     node.put(ConsoleLogger.KEY_PREFERENCES_ATTRIBUTE_TEXT, text);
	}
	
	/** Adds the specified properties to the target properties */
	public void toProperties(Properties props) {
		String[] lines = text.split("\n");
		String priorItem = "";
		String priorLevel = "info"; 
		for (String line : lines) {
			if (line.startsWith("#")) {
				continue;
			}
			String[] items = line.split(":");
			String level; 
			
			if (items.length >= 1) {
				if (items.length == 1) {
					level = priorLevel; 
				} else {
					level = items[1];
				}
			
				if (items[0].indexOf('.') < 0) {
					items[0] = replace(items[0],priorItem);
				} else {
					priorItem = items[0];
					priorLevel = level; 
				}
				
				if (!items[0].endsWith(".")) { //may be used for defining a default log level on a package, to be active for subsequent lines without setting all loggers in that package to this level
					props.put(ConsoleLogger.LOG_KEY_PREFIX+items[0], level);
				}
			}
		}
	}
	

	String processText(String text) {
		StringBuilder sb = new StringBuilder(); 
		String[] lines = text.split("\\r?\\n"); //preserves empty lines
		for (String line: lines) {
			if (line.length() > 2) {
				line = line.trim().replace("=",":");
			}
			
			if (line.startsWith("#")) {
				sb.append(line);
			} else {
				String[] items = line.split(":");
				if (items.length == 1) {
					sb.append(items[0].trim());
				} else if (items.length == 2) {
					sb.append(items[0].trim());
					
					String replace = items[1].trim();
					if (replace.length() > 0) {
					switch(replace.charAt(0)) {
						case 'd':
							case 'D': replace = "debug"; break;
							case 't':
							case 'T': replace = "trace"; break; 
							case 'e':
							case 'E': replace = "error"; break; 
							case 'w':
							case 'W': replace = "warn"; break; 
							case 'i':
							case 'I': replace = "info"; break;
							case 'o':
							case 'O': replace = "off"; break;
							default: replace = "";
						}
					}
					if (!replace.isEmpty()) {
						sb.append(":").append(replace);	
					} //else: level string is removed. Alternative: issue a warning?
				} else {
					sb.append(line);
				}
			}
			sb.append("\n"); 
		}
		
		return sb.toString();		
	}
	
	/** 
	 * Creates a formatted version, slightly better looking than when text is only typed. 
	 * Main difference is that lines with short class names are indented and that 
	 * paces before and after colon for better readability. 
 	 * @return A formatted version, slightly better looking than when text is only typed. */
	public String getFormatted() {
		String[] lines = text.split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			if (line.startsWith("#") || line.trim().length() == 0) {
				sb.append(line); 
			} else {
				String[] items = line.split(":");
					items[0] = items[0].trim();
					if (items[0].indexOf('.')<0) { //items[0] always exists, as line is not empty
						items[0] = "   " + items[0] ;
					}
					if (items.length == 1) {
						sb.append(items[0]);
					} else {
						sb.append(items[0]);
						sb.append(" : ");
						sb.append(items[1].trim());
					}
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	/** Used to process shorthand notation. If user enters a fully qualified class name in one 
	 *  line and ads only a class name in the next line, then this will be interpreted as 
	 *  referring to a class in the same package as the line before. 
	 *  In the example below, MySecondClass will be interpreted as belonging to package 
	 *  com.mypackage, too: 
	 *  
	 *  com.my.package.MyClass:debug 
 	 *  MySecondClass:debug
 	 *  
	 * */
	private String replace(String item, String priorItem) {
		if (priorItem.isEmpty()) {
			return item;
		}
		int posF = item.lastIndexOf('.');
		if (posF < 0) {
			posF = priorItem.lastIndexOf('.');
			if (posF == priorItem.length()-1) {
				return priorItem + item;
			} else if (Character.isUpperCase(priorItem.charAt(posF+1))) { //uppercase letter indicates class
				return priorItem.substring(0, posF) + '.' + item; 
			} else {
				return priorItem + '.' + item; 
			}
		} else {
			return item; 
		}
	}
	
}
