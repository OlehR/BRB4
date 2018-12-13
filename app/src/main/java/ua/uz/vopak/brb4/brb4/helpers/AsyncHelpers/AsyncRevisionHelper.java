
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.RevisionActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncRevisionHelper extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    RevisionActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.LoadListDoc(activity,param[0]);
        return null;
    }


    public AsyncRevisionHelper( Worker parWorker, RevisionActivity context)
    {
        activity = context;
        varWorker = parWorker;
    }

}
