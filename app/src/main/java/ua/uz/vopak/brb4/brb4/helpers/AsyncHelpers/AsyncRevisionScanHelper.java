package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncRevisionScanHelper extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    DocumentScannerActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.GetRevisionScannerData(param[0], activity);
        return null;
    }


    public AsyncRevisionScanHelper(Worker parWorker, DocumentScannerActivity context)
    {
        activity=context;
        varWorker = parWorker;
    }

}
