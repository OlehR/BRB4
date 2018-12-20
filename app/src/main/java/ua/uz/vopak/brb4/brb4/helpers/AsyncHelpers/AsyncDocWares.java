package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncDocWares extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    DocumentScannerActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.SaveDocWares(param[0], param[1], param[2], param[3], param[4], activity);
        return null;
    }


    public AsyncDocWares(Worker parWorker, DocumentScannerActivity context)
    {
        activity=context;
        varWorker = parWorker;
    }

}
