package com.youngsee.mirrorplayer.util;

import android.util.Log;

public class Logger {

    private final static boolean logFlag = true;

    private final static String tag = "[MirrorPlayer]";
    private final static int logLevel = Log.VERBOSE;
    
    private final StringBuffer sb = new StringBuffer();

    /**
     * Get The Current Function Name
     * 
     * @return
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }

        for (StackTraceElement st : sts) {
            if (st.isNativeMethod() || st.getClassName().equals(Thread.class.getName())
            		|| st.getClassName().equals(this.getClass().getName())) {
                continue;
            }

            sb.setLength(0);
            sb.append("{Thread:");
            sb.append(Thread.currentThread().getName());
            sb.append("}[ ");
            sb.append(st.getFileName());
            sb.append(":");
            sb.append(st.getLineNumber());
            sb.append(" ");
            sb.append(st.getMethodName());
            sb.append(" ]");
            return sb.toString();
        }

        return null;
    }

    /**
     * The Log Level:i
     * 
     * @param str
     */
    public void i(Object str) {
        if (logFlag && logLevel <= Log.INFO) {
            String name = getFunctionName();
            if (name != null) {
            	sb.setLength(0);
            	sb.append(name);
            	sb.append(" - ");
            	sb.append(str);
                Log.i(tag, sb.toString());
            } else {
                Log.i(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:d
     * 
     * @param str
     */
    public void d(Object str) {
        if (logFlag && logLevel <= Log.DEBUG) {
            String name = getFunctionName();
            if (name != null) {
            	sb.setLength(0);
            	sb.append(name);
            	sb.append(" - ");
            	sb.append(str);
                Log.d(tag, sb.toString());
            } else {
                Log.d(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:V
     * 
     * @param str
     */
    public void v(Object str) {
        if (logFlag && logLevel <= Log.VERBOSE) {
            String name = getFunctionName();
            if (name != null) {
            	sb.setLength(0);
            	sb.append(name);
            	sb.append(" - ");
            	sb.append(str);
                Log.v(tag, sb.toString());
            } else {
                Log.v(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:w
     * 
     * @param str
     */
    public void w(Object str) {
        if (logFlag && logLevel <= Log.WARN) {
            String name = getFunctionName();
            if (name != null) {
            	sb.setLength(0);
            	sb.append(name);
            	sb.append(" - ");
            	sb.append(str);
                Log.w(tag, sb.toString());
            } else {
                Log.w(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:e
     * 
     * @param str
     */
    public void e(Object str) {
        if (logFlag && logLevel <= Log.ERROR) {
            String name = getFunctionName();
            if (name != null) {
            	sb.setLength(0);
            	sb.append(name);
            	sb.append(" - ");
            	sb.append(str);
                Log.e(tag, sb.toString());
            } else {
                Log.e(tag, str.toString());
            }
        }
    }

    /**
     * The Log Level:e
     * 
     * @param ex
     */
    public void e(Exception ex) {
        if (logFlag && logLevel <= Log.ERROR) {
            Log.e(tag, "error", ex);
        }
    }

    /**
     * The Log Level:e
     * 
     * @param log
     * @param tr
     */
    public void e(String log, Throwable tr) {
        if (logFlag) {
            String line = getFunctionName();
            sb.setLength(0);
            sb.append("{Thread:");
            sb.append(Thread.currentThread().getName());
            sb.append("}[ ");
            sb.append(line);
            sb.append(":] ");
            sb.append(log);
            sb.append("\n");
            Log.e(tag, sb.toString(), tr);
        }
    }

}
