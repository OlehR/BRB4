package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.app.Activity;
import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncDocWares extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    Activity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.SaveDocWares(param[0], param[1], param[2], param[3], param[4],param[5], activity);
        return null;
    }


    public AsyncDocWares(Worker parWorker, Activity context)
    {
        activity=context;
        varWorker = parWorker;
    }

}
