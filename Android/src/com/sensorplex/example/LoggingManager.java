package com.sensorplex.example;

import android.content.Context;
import android.net.Uri;

import java.io.*;
import java.nio.channels.FileChannel;

public class LoggingManager {
    private Context context;
    private static final String FILE_NAME = "data.csv";
    private static final String FILE_SENDING_NAME = "data_sp.csv";

    private FileOutputStream outputStream = null;
    private PrintWriter pw;
    private boolean enabled = false;

    public LoggingManager(Context context) {
        this.context = context;
    }

    public void init() {
        try {
            outputStream = context.openFileOutput(FILE_NAME, Context.MODE_MULTI_PROCESS);
            pw = new PrintWriter(outputStream);
        } catch (FileNotFoundException e) {

        }
    }

    public void enable(boolean enabled) {
        this.enabled = enabled;
    }

    public void writeLine(String line) {
        if (enabled && null != pw)
            pw.println(line);
    }

    public void close() {
        pw.close();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Uri prepareToSend() {
        close();
        Uri result = null;

        try {
            File old = new File(context.getFilesDir(), FILE_NAME);
            File nf = new File(context.getCacheDir(), FILE_SENDING_NAME);
            copyFile(old, nf);
            result = Uri.fromFile(nf);
        } catch (Exception e) {

        }

        init();

        return result;
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
            destFile.setReadable(true, false);
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
