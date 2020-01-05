package eu.jgdi.mc.map2mc.utils;

import java.text.MessageFormat;

public class Logger {

    private static Logger instance = new Logger();

    public static Logger logger() {
        return instance;
    }

    public void debug(String message, Object... params) {
        System.out.println("DEBUG: " + MessageFormat.format(message, params));
    }

    public void info(String message, Object... params) {
        System.out.println("INFO : " + MessageFormat.format(message, params));
    }

    public void warn(String message, Object... params) {
        System.out.println("WARN : " + MessageFormat.format(message, params));
    }

    public void error(String message, Object... params) {
        error(null, message, params);
    }

    public void error(Throwable th, String message, Object... params) {
        System.out.println("ERROR: " + MessageFormat.format(message, params));
        if (th != null) {
            th.printStackTrace();
        }
    }
}
