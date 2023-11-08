package Logging;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

// save log to file in device
// you can find log file in directory returned by context.getFilesDir()
// you can navigate in folders of device in Android Studio -> View -> Tool Windows -> Device Explorer
// context.getFilesDir() returns path "/data/data/com.example.runusandroid/files/phenotype_storage_info"

public class FileLogger {

    public static void logToFileAndLogcat(Context context, String tag, String message) {
        // Log to Logcat
        Log.d(tag, message);

        // Log to a file
        try {
            File logFile = new File(context.getFilesDir(), "distance_log.txt");
            FileOutputStream outputStream = new FileOutputStream(logFile, true);
            String logMessage = message + "\n";
            outputStream.write(logMessage.getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
