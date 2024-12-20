package frc.robot;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import edu.wpi.first.wpilibj.DriverStation;

/* Thanks guys for this super code:
 * http://stemrobotics.cs.pdx.edu/node/7150?root=7150
 * 
 * This is sooo cool, found this on the 'net
 * Basically we can log anything we cant to disk on roboRIO and sftp
 * it off to a PC for analysis.  Great for monitoring all kids of stuff
 * 
 * To do this:
 * 1. Reference this cool logging class
 * 2. Put this in Robot() constructor:
 * // Set up our custom logger.
 *       try
 *       {
 *           Logging.CustomLogger.setup();
 *       }
 *      catch (Throwable e) { Logging.logException(e);} 
 *
 *	Now drop something like this where you want to monitor elevator Height:
 *	Logging.consoleLog("Elevator Height: " + elevatorHeight);
 *
 * Now use fileZilla in Win or gftp (SSH2) in Linux, connect sftp to RoboRIO
 * host: roboRIO-384-frc.local
 * port: 22
 * user: lvuser
 * password: (non password)
 * 
 * Once there, look for Logging.txt.0-99.  After 99 reached, logging will fail,
 * files will have tp be deleted.
 * 
 *  Note that this logs very rapidly, can we slow down so as not to hog CPU?
 */
public class Logging
{
    /**
     * Open print stream that writes to the log file. Example of use:
     * exception.printStackTrace(Util.logPrintStream);
     */
    public static final PrintStream logPrintStream = new PrintStream(new LoggingOutputStream());

    /**
     * Logging class for use by other classes to log though this custom logging scheme. All
     * logging should be done by calls to methods on this class instance or with the 
     * convenience methods of the Logging class.
     */
    public final static Logger logger = Logger.getGlobal();
        
    // Private constructor means this class cannot be instantiated. All access is static.
        
    private Logging()
    {
              
    }
                
    /**
     * Configures and holds (static) classes for our custom logging system. 
     * Call setup() method to initialize logging.
     */
    public static class CustomLogger 
    {
        static private FileHandler              fileTxt;
        static private LogFormatter             logFormatter;
        
        /**
         *  Initializes our logging system.
         *  Call before using any logging methods.
         */
        static public void setup() throws IOException 
        {
                // get the global logger to configure it and add a file handler.
                Logger logger = Logger.getGlobal();
                   
                logger.setLevel(Level.ALL);

                // If we decide to redirect system.out to our log handler, then following
                // code will delete the default log handler for the console to prevent
                // a recursive loop. We would only redirect system.out if we only want to
                // log to the file. If we delete the console hanlder we can skip setting
                // the formatter...otherwise we set our formatter on the console logger.
            
                Logger rootLogger = Logger.getLogger("");

            Handler[] handlers = rootLogger.getHandlers();
            
//            if (handlers[0] instanceof ConsoleHandler) 
//            {
//                rootLogger.removeHandler(handlers[0]);
//                return;
//            }

            logFormatter = new LogFormatter();

            // Set our formatter on the console log handler.
            if (handlers[0] instanceof ConsoleHandler) handlers[0].setFormatter(logFormatter);

            // Now create a handler to log to a file on roboRio "disk".
            
            //if (true) throw new IOException("Test Exception");
            
            if (new File("  /Logging.txt.99").exists() != true)
                fileTxt = new FileHandler("/home/lvuser/Logging.txt");
            else
                throw new IOException("Max number of log files reached.");
            
            fileTxt.setFormatter(logFormatter);

            logger.addHandler(fileTxt);
        }
    }
        
    // Our custom formatter for logging output.
        
    private static class LogFormatter extends Formatter 
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:S");
        
        public LogFormatter()
        {
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        }

        public String format(LogRecord rec) 
        {
            StringBuffer buf = new StringBuffer(1024);
            
            buf.append(String.format("<%d>", rec.getThreadID()));
            buf.append(dateFormat.format(new Date(rec.getMillis())));
            buf.append(" ");
            buf.append(formatMessage(rec));
            buf.append("\n");
        
            return buf.toString();
        }
    }
        
    // An output stream that writes to our logging system. Writes data with flush on
    // flush call or on a newline character in the stream.
        
    private static class LoggingOutputStream extends OutputStream 
    {
        private static final int    DEFAULT_BUFFER_LENGTH = 2048;
        private boolean             hasBeenClosed = false;
        private byte[]              buf;
        private int                 count, curBufLength;

        public LoggingOutputStream()
        {
            curBufLength = DEFAULT_BUFFER_LENGTH;
            buf = new byte[curBufLength];
            count = 0;
        }

        public void write(final int b) throws IOException 
        {
            if (hasBeenClosed) {throw new IOException("The stream has been closed.");}
                
            // don't log nulls
            if (b == 0) return;
                
            // force flush on newline character, dropping the newline.
            if ((byte) b == '\n') 
            {
                flush();
                return;
            }
                
            // would this be writing past the buffer?
            if (count == curBufLength) 
            {
                // grow the buffer
                final int newBufLength = curBufLength + DEFAULT_BUFFER_LENGTH;
                final byte[] newBuf = new byte[newBufLength];
                System.arraycopy(buf, 0, newBuf, 0, curBufLength);
                buf = newBuf;
                curBufLength = newBufLength;
            }

            buf[count] = (byte) b;
               
            count++;
        }

        public void flush() 
        {
            if (count == 0) return;
                
            final byte[] bytes = new byte[count];

            System.arraycopy(buf, 0, bytes, 0, count);
                
            String str = new String(bytes);
                
            consoleLog(str);
                
            count = 0;
        }

        public void close() 
        {
            flush();
            hasBeenClosed = true;
        }
    }

    /**
     * Returns program location where call to this method is located.
     */
    public static String currentMethod()
    {
        return currentMethod(2);
    }

    private static String currentMethod(Integer level)
    {
        StackTraceElement stackTrace[];
    
        stackTrace = new Throwable().getStackTrace();

        // This scheme depends on having one level in the package name between
        // team1111 and the class name, ie: team1111.robot.Logging.method. New levels
        // will require rewrite.
        
        try
        {
            String method = stackTrace[level].toString().split("1111.")[1];
            
            int startPos = method.indexOf(".") + 1;
            
            return method.substring(startPos);
        }
        catch (Throwable e)
        {
            return "method not found";
        }
    }

    /**
     * Write message to console log with optional formatting and program location.
     * @param message Message with optional format specifiers for listed parameters.
     * @param parms Parameter list matching format specifiers.
     */
    public static void consoleLog(String message, Object... parms)
    {
        // logs to the console as well as our log file on RR disk.
        logger.log(Level.INFO, String.format("robot: %s: %s", currentMethod(2), String.format(message, parms)));
    }
    
    /**
     * Write blank line with program location to the console log.
     */
    public static void consoleLog()
    {
        // logs to the console as well as our log file on RR disk.
        logger.log(Level.INFO, String.format("robot: %s", currentMethod(2)));
    }

    /**
     * Write exception message to DS console window and exception stack trace to
     * log file.
     * @param e The exception to log.
     */
    public static void logException(Throwable e)
    {
        DriverStation.reportError(e.toString(), false);
                
        e.printStackTrace(Logging.logPrintStream);
    }
}