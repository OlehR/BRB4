
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncLoadListDoc extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    DocumentActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.LoadListDoc(activity,param[0]);
        return null;
    }


    public AsyncLoadListDoc( Worker parWorker, DocumentActivity context)
    {
        activity = context;
        varWorker = parWorker;
    }

}
