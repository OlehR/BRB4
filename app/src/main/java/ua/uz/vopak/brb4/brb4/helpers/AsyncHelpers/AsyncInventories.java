package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.RevisionItemsActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncInventories extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    RevisionItemsActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.GetInventories(param[0],activity);
        return null;
    }


    public AsyncInventories( Worker parWorker, RevisionItemsActivity context)
    {
        activity = context;
        varWorker = parWorker;
    }

}