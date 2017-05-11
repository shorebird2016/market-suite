package org.marketsuite.framework.model;

import org.marketsuite.framework.model.type.LoggingSource;
import org.marketsuite.framework.resource.FrameworkConstants;
import org.marketsuite.framework.util.Props;
import org.marketsuite.framework.model.type.LoggingSource;

import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;

//general purpose message object for logging
public class LogMessage {
    public LogMessage(LoggingSource source, String message, Exception exception, Calendar time) {
        this.source = source;
        this.message = message;
        this.exception = exception;
        this.time = time;
    }
    public LogMessage(LoggingSource source, String message, Exception exception) {
        this.source = source;
        this.message = message;
        this.exception = exception;
        this.time = Calendar.getInstance();
    }

    public static void logSingleMessage(String msg, LoggingSource src) {
        logSingleMessage(msg, src, null);
    }
    public static void logSingleMessage(String msg, LoggingSource src, Exception e) {
        LogMessage lm = new LogMessage(src, msg, e);
        ArrayList<LogMessage> msgs = new ArrayList<>(); msgs.add(lm);
        Props.Log.setValue(null, msgs);
    }
    public static void logSingleMessageInEdt(final String msg, final LoggingSource src) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                logSingleMessage(msg, src);
            }
        });
    }

    private LoggingSource source;
    public LoggingSource getSource() { return source; }

    private String message;
    public String getMessage() { return message != null ? message : ""; }

    private Exception exception;
    public Exception getException() { return exception /*!= null ? exception : new Exception()*/; }

    private Calendar time;
    public Calendar getTime() { return time; }

}
