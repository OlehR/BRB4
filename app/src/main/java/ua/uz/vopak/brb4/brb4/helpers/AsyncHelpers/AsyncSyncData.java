package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncSyncData extends AsyncTask<Void , Void, Void> {
    Worker varWorker;
    @Override
    protected Void doInBackground(Void... param)
    {
        varWorker.SendLogPrice();
        return null;
    }
/*
    @Override
    protected void onPostExecute(Void)
    {
        //varWorker.scanerContext.setScanResult(parLI);
    };*/


    public AsyncSyncData( Worker parWorker)
    {
        varWorker=parWorker;
    }

}
