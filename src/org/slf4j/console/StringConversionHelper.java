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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

/** Improves the log output for the 'toString()' method 
 *  of some commonly used java classes. */
public class StringConversionHelper {

	private static SimpleDateFormat simpleDateFormat;
	private static SimpleDateFormat simpleTimeFormat; 

	private static DecimalFormat floatFormat;
	
	static { 
		floatFormat = ((DecimalFormat) NumberFormat.getInstance(Locale.US));
	    floatFormat.applyPattern("0.0#####"); 
	}
	
	private StringConversionHelper() { //Ensures that no public constructor is available 
	}
	
	private static SimpleDateFormat getIso8601DateFormat() {
		if (simpleDateFormat == null) {
			simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); //ISO Date
			
			//simpleTimeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, Locale.getDefault()); //usually shows hours, minutes, seconds. In US-like locales will show AM/PM. 			
			simpleTimeFormat = new SimpleDateFormat("HH:mm:ss"); //Time (24hour clock), like java.util.Date.toString() we ignore milliseconds and timezone here
		}
		return simpleDateFormat; 
	}
	
	public static void format(StringBuilder sbuf, Object o) {

		try {		
			switch (o.getClass().getName().hashCode()) {
			case -2088293497: //java.awt.Color
				Color color = (Color) o;
				if (color.getAlpha() != 255)  {
					sbuf.append(String.format("#%02x%02x%02x%02x", color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()));
				} else {
					sbuf.append(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));					
				}
				return;
			case 65575278: //java.util.Date   Dates with a midnight time component are formatted just as ISO date, without time.  
				Date date = (Date) o;
				sbuf.append(getIso8601DateFormat().format(date));
				String timePart = simpleTimeFormat.format(date);
				if (!timePart.equals("00:00:00")) { 
						sbuf.append("T").append(timePart); //The separator T is ISO standard format. 
				}	
				return;
			case 761287205: //java.lang.Double -> We limit this to 6 digits. If you really need more then use the String.valueOf()"
				sbuf.append(floatFormat.format((Double) o));   
				return; 
			case -527879800: //java.lang.Float  -> We limit this to 6 digits. If you really need more then use the String.valueOf()"
				sbuf.append(floatFormat.format((Float) o));				
				return; 
			case 372815754: //java.awt.Dimension
				Dimension dim = (Dimension) o;
				sbuf.append(dim.width).append("x").append(dim.height);
				return;
			case -141538478: //java.awt.Insets
				Insets insets = (Insets) o;
				sbuf.append("top=").append(insets.top)
				    .append(",left=").append(insets.left)
				    .append(",bottom=").append(insets.bottom)
				    .append(",right=").append(insets.right);
				return;
			case -2076290636:  //java.awt.Point
				Point pt = (Point) o; 
				sbuf.append(pt.x).append(":").append(pt.y);
				return;
			case 1662751049: //java.awt.geom.Point2D$Double
				Point2D.Double p2 = (Point2D.Double) o;
				sbuf.append(p2.x).append(":").append(p2.y);
				return; 
			case -1705838701:  //java.awt.Rectangle
				Rectangle rect = (Rectangle) o; 
				sbuf.append("x=").append(rect.x)
				    .append(",y=").append(rect.y)
				    .append(",w=").append(rect.width)
				    .append(",h=").append(rect.height);
				return;
			default: sbuf.append(o.toString());
			}

		} catch (ClassCastException ex) { //using hashCode will work almost all of the time, but in the rare case that it gets the wrong result, we just revert to the toString() method. 
			sbuf.append(o.toString());
		}
	}
	
}
