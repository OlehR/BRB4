
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncLoadDataDoc extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    DocumentActivity activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.LoadDataDoc(param[0],param[1]);
        activity.AfterLoadData(param[1]);
        return null;
    }
/*
    @Override
    protected void onPostExecute(Void)
    {
        //varWorker.scanerContext.setScanResult(parLI);
    };*/


    public AsyncLoadDataDoc( Worker parWorker, DocumentActivity context)
    {
        varWorker=parWorker;
        activity = context;
    }

}
