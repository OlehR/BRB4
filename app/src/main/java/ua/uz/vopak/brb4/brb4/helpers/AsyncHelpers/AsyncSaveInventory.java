package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.RevisionScannerActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncSaveInventory extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    RevisionScannerActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.SaveRevisionData(param[0], param[1], param[2], param[3], activity);
        return null;
    }


    public AsyncSaveInventory(Worker parWorker, RevisionScannerActivity context)
    {
        activity=context;
        varWorker = parWorker;
    }

}
