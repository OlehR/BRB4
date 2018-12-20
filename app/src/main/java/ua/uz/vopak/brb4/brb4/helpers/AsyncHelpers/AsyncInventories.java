package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncInventories extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    DocumentItemsActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.GetDoc(param[0],param[1],activity);
        return null;
    }


    public AsyncInventories( Worker parWorker, DocumentItemsActivity context)
    {
        activity = context;
        varWorker = parWorker;
    }

}