package com.deelock.wifilock.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2017\9\11 0011.
 */

public final class Logger {

    private static final Config CONFIG = new Config();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
            "MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private static final int LEVEL_DEBUG = 0;
    private static final int LEVEL_ERROR = 1;

    private final String mLabel;

    private Logger(String label) {
        mLabel = label;
    }

    private Logger(Object object) {
        mLabel = object.getClass().getSimpleName();
    }

    public static Config config() {
        return CONFIG;
    }

    public static Logger getLogger(Class<?> cls) {
        return new Logger(cls.getSimpleName());
    }

    public static Logger getLogger(Object object) {
        return new Logger(object);
    }

    public void debug(String format, Object... args) {
        final String message = mLabel + ": " +
                String.format(Locale.getDefault(), format, args);
        println(LEVEL_DEBUG, message);
    }

    public void error(String format, Object... args) {
        final String message = mLabel + ": " +
                String.format(Locale.getDefault(), format, args);
        println(LEVEL_DEBUG, message);
    }

    public void error(Throwable t, String format, Object... args) {
        final String message = mLabel + ": " +
                String.format(Locale.getDefault(), format, args)
                + "\n" + Log.getStackTraceString(t);
        println(LEVEL_DEBUG, message);
    }

    private void println(final int level, final String message) {
        if (!CONFIG.isEnabled) {
            return;
        }

        // Log to logcat
        if (level == LEVEL_ERROR) {
            Log.e(CONFIG.tag, message);
        } else {
            Log.d(CONFIG.tag, message);
        }

        if (!CONFIG.isWriteFile) {
            return;
        }

        // Log to logfile in sdcard
        EXECUTOR.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    writeFile(level, System.currentTimeMillis(), message);
                } catch (IOException e) {
                    // Ignore
                }
            }
        });
    }

    private void writeFile(int level, long time, String message) throws IOException {
        String levelString = level == LEVEL_ERROR? "ERROR" : "DEBUG";
        String log = String.format(Locale.getDefault(), "%s [%s] %s\n",
                FORMAT.format(time), levelString, message);
        File file = new File(Environment.getExternalStorageDirectory(), CONFIG.tag + ".log");
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        FileOutputStream output = new FileOutputStream(file, true);
        OutputStreamWriter writer = new OutputStreamWriter(output);
        writer.write(log);
        writer.flush();
        writer.close();
    }

    public final static class Config {
        private Config() {}
        public boolean isEnabled = false;
        public boolean isWriteFile = false;
        public String tag = Logger.class.getSimpleName();
    }
}
