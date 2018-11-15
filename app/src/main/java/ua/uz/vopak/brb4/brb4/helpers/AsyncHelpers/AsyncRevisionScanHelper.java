package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.RevisionScannerActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncRevisionScanHelper extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    RevisionScannerActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.GetRevisionScannerData(param[0], activity);
        return null;
    }


    public AsyncRevisionScanHelper(Worker parWorker, RevisionScannerActivity context)
    {
        activity=context;
        varWorker = parWorker;
    }

}
