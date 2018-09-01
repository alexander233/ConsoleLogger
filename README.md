# Console Logger for Java

Don't litter your code with **System.out.println()** statements. Use the powerful logging features of the Java universe instead. This [small jar file](http://www.inventivio.com/softblocks/slf4j-console-1.8.0-beta.jar) (less than 40kB) makes the transition painless: it requires minimal configuration, has only a single dependency (SLF4J-api.jar), can be removed at any time and can be replaced by any of the more powerful SLF4J logger implementations. 

## Benefits:
- Console-friendly output: Log statements don't clutter your console. Statements are optimized for console display and put the focus on the log message (by using a fixed column size for logger names). Statements are also visually grouped by logger. 
- Debug-friendly formatting: No more double numbers displaying 16 decimal places (now cut off at 6), dates are formatted in eye-friendly ISO format (2021-01-12) instead of long date strings such as Tue, Jan 12, 2021 00:00:00 CEST. Colors are displayed like #00ff00 instead of java.awt.Color[r=0,g=255,b=0]
- ERROR and WARN level messages are shown in red (if the console supports it). 
- Jar file includes a user interface for quickly configuring log levels. No need to worry about file paths 
 and no cumbersome logger definitions. Double-click the jar file and you can specify the logger with minimal 
 typing. There is no easier way to change log levels quickly while debugging. No more excuses for using System.out.println()!

## Example console output

The following example shows how log output appears in the console. Each line starts with the first few characters of the logger's name followed by the abbreviated log level (a single character 'i', 'd', 't', 'W', or, 'E' for INFO, DEBUG, TRACE, WARN and ERROR levels).      
    
The other lines contain the individual log statements. Two dots in the beginning of a line indicate that this line was reported by the same logger as the previous line.
  
```
AppFrame   d| display dimensions 960x880
..         d| initializing to array size 1020000
..         d| channel 0 size 180
PulseChann d| data from 2018-06-12T14:33:47
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
```

The above example does not show time stamps; but it is possible to prefix each log statement with a time stamp (time part only, to conserve space). See further below for command line options and properties file configuration. 

## Formatting examples

The table below shows how ConsoleLogger's formatting differs from Java's standard toString() method:  
<table>
<tr><th>Class</th><th>toString()</th><th>ConsoleLogger</th><th>Comment</th></tr>
<tr><td>Double</td><td>0.05263157894736842</td><td>0.052632</td><td>Double and float values are limited to 6 digits of precision because overly long doubles make reading the values harder and mostly mess up the output. It is extremely rare that we are really interested in higher precision  (just don't use the format option in those case).</td></tr>
<tr><td>Date</td><td>Fri Sep 29 00:00:00 CEST 2017</td><td>2017-09-29</td><td>Time zone and time part ignored for dates with time of 00:00:00</td></tr>
<tr><td>Date</td><td>Sat Jan 13 21:25:36 CET 2018</td><td>2018-01-13T21:25:36</td><td>Date instances don't contain information about a time zone. Therefore the time zone string is not displayed (and all Date values are formatted for the default time zone).</td></tr>
<tr><td>Rectangle</td><td>java.awt.Rectangle[x=10,y=20,width=30,height=40]</td><td>x=10,y=20,w=30,h=40</td><td></td></tr>
<tr><td>Point</td><td>java.awt.Point[x=11,y=12]</td><td>11:12</td><td></td></tr>
<tr><td>Point2D.Double</td><td>Point2D.Double[1.2233, 4.5566]</td><td>1.2233:4.5566</td><td></td></tr>
<tr><td>Dimension</td><td>java.awt.Dimension[width=20,height=30]</td><td>20x30</td><td></td></tr>
<tr><td>Insets</td><td>java.awt.Insets[top=1,left=2,bottom=3,right=4]</td><td>top=1,left=2,bottom=3,right=4</td><td></td></tr>
<tr><td>Color</td><td>java.awt.Color[r=0,g=0,b=255]</td><td>#0000ff</td><td></td></tr>
</table>

## Using ConsoleLogger 
 
Add the ConsoleLogger jar file to your classpath. Then add loggers to any of your classes using the following statement (most Java development environments already have a code template for this which you can execute with a quick keyboard shortcut):
 
```
  static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MyClass.class);
```

Now you can write log statements such as

```
  double factor = Math.sqrt(2);
  log.trace("Factor: {} ",factor);      
```

By default, this logger will be set to the INFO log level and any message on this level or higher (WARN, ERROR) will be sent to the console. Therefore, when you run the code, the information will not be printed to the console. 
 
To activate the TRACE level for this logger, execute the console logger jar file, add the logger 
to the active configuration by adding a line with the logger name and the first letter of the desired 
log level (t for trace) and then clicking the Save button: 
 
```
com.mycompany.MyClass : t
```

## User interface for logger configuration 

Launch the user interface by executing the jar file (usually a double-click should be sufficient). 
Use one line for each logger definition. Specify loggers according to the following format: 

```
[<package>[.]][<class>] [:<level>]
```

An example logger definition is shown below:

```
#  -- Console Logger Configuration Example --
com.myapp.Maps : trace
   Controller
   MapImage : debug
   QueryGenerator
com.myapp.cache. : debug
   MapCache
   MapInfo
```
 
Classes can be specified without package name. They inherit the package from the preceding line (Controller in line 3 refers to com.myapp.Controller). Packages ending with '.' (line 6) are only used for determining package names on the following lines. They do not become their own logger!  
 
Levels can be abbreviated (e.g. d=debug, or  o=off). Lines without level inherit their level from the nearest preceding package name: Therefore com.myapp.Controller and com.myApp.QueryGenerator are both set to trace level. 

'#' refers to a comment line.

Only one configuration page is active at any time. The active configuration appears at startup and is shown in bold in the drop down list. If the **-- (No logger active)--** page appears at startup then no loggers are enabled for console logging. To disable any of the other logger configuration pages, switch to the -- (No Loggers Active) -- page and then click the Save button. 

## Command line and properties file configuration

Loggers can also be configured using a properties file named **consolelogger.properties** that contains the fully qualified name of the logger class and the log level, for example:

``` 
com.mypackage.MyClass:warn
```

To prefix log statements with a time stamp using a single line with

```
time=true
```

Note that the log configuration file must be placed in the classes directory, or (to still to be implemented) ~~in the working directory or in the parent directory of the working directory.~~ 

Loggers can also be configured as command line parameters. In such cases, the fully qualified class name must be prefixed with **org.slf4j.console.**. The class com.mypackage.MyClass can be set to WARN level using: 

``` 
-Dorg.slf4j.console.com.mypackage.MyClass:warn
```
 
Time stamps can be prepended to each log statement by adding the following term to the command line:
 
```
-Dorg.slf4j.console.time=true
```
 
It is also possible to adjust the column width for the logger name (default is 10) using the **width** property. To set the column width to 15 add the following line in the consolelogger.properties file:

```
width=15
```

If you don't want to display the logger name, set the column width to 0, for example via the command line: 
 
```
-Dorg.slf4j.console.width=0
```


## Download
 
Download the jar file at: 
[http://www.inventivio.com/softblocks/slf4j-console-1.8.0-beta.jar](http://www.inventivio.com/softblocks/slf4j-console-1.8.0-beta.jar)

##  Transitioning from System.out.println() practices to ConsoleLogger

ConsoleLogger does not force you to change your debugging style. By outputting to the logger you have more fine-grained control over what appears on your console. You can turn your console output on and off by just changing the level of your loggers. Differentiating the importance of your logging statements is a great advantage. You can keep messages that you need only for current debugging on the trace level, assign debug level to those message that may help in further debugging in the future and assign info level to those message that are useful while running the program in production mode. One approach for differentiating between TRACE and DEBUG levels is in the following way: TRACE levels is only used for log statements that are necessary for the current debugging activity and have no longer-time relevance. TRACE levels will be removed before releasing the product. DEBUG levels are used for log statements that may be useful in future debugging efforts. They remain part of software releases. 

When working with ConsoleLogger it is useful to use one Logger per class with log statements. If you share the same logger across many classes, it becomes more difficult to debug using log statements because the origin of log output is harder to find and it is harder to enable / disable log statements selectively. 

ConsoleLogger is useful for small and large projects. 

##  Limitations

If you need different formatting (i.e. more than 6 decimals for double values or millisecond precision for Date values, convert your value to String yourself and then pass it to the log statement. Wrap in an if (log.isDebugEnabled()) block to make sure that conversion only happens when the log statement is actually displayed.

If you require additional common classes to be formatted more conveniently, let us know by adding an issue.

ConsoleLogger is not optimized for heavily multi-threaded applications. Log statements are written to System.out and System.err depending on log level but the order at which System.out and System.err statements appear on your debug console may not always be precise.  

On Windows with a JRE version 8 or below the Java Runtime Engine may output two messages to the 
error console complaining about the Java Preferences initialization. These messages are misleading (because they imply a problem where none exists), have been recognized as a Java bug (see [https://bugs.openjdk.java.net/browse/JDK-8139507](https://bugs.openjdk.java.net/browse/JDK-8139507)) and these messages no longer appear on Java 9 and above. To prevent these annoying messages from occurring for JRE 8 on Windows, the following node needs to be added to the Windows registry via the Windows **regedit** command (this requires Administrator privileges):

```
HKEY_CURRENT_MACHINE/Software/JavaSoft/Prefs
```

## Dependencies 

 SLF4J-API
         
## License

This code is made available under the QOS.ch MIT-style license. ConsoleLogger was forked from the [SLF4J-Simple logger implementation](https://github.com/qos-ch/slf4j/tree/master/slf4j-simple).
