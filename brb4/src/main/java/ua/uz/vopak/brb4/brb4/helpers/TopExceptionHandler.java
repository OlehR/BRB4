package ua.uz.vopak.brb4.brb4.helpers;

import android.app.Activity;

import ua.uz.vopak.brb4.lib.helpers.Utils;

public class TopExceptionHandler implements Thread.UncaughtExceptionHandler {
    protected static final String TAG = "TopExceptionHandler";
    private Thread.UncaughtExceptionHandler defaultUEH;
    private Activity app = null;

    public TopExceptionHandler(Activity context) {
    }

    public void uncaughtException(Thread t, Throwable e) {
        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString()+"\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i=0; i<arr.length; i++) {
            report += "    "+arr[i].toString()+"\n";
        }
        report += "-------------------------------\n\n";

        // If the exception was thrown in a background thread inside
        // AsyncTask, then the actual exception can be found with getCause

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if(cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i=0; i<arr.length; i++) {
                report += "    "+arr[i].toString()+"\n";
            }
        }
        report += "-------------------------------\n\n";

        Utils.WriteLog("e",TAG,report);
        /*try {

            FileOutputStream trace = app.openFileOutput("stack.trace",
                    Context.MODE_PRIVATE);
            trace.write(report.getBytes());
            trace.close();
        } catch(IOException ioe) {
            // ...
        }*/

        defaultUEH.uncaughtException(t, e);
    }
}