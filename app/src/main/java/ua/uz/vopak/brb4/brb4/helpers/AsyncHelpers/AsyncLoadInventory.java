
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncLoadInventory extends AsyncTask<Void , Void, Void> {
    Worker varWorker;
    @Override
    protected Void doInBackground(Void... param)
    {
        varWorker.LoadDataInventory();
        return null;
    }
/*
    @Override
    protected void onPostExecute(Void)
    {
        //varWorker.scanerContext.setScanResult(parLI);
    };*/


    public AsyncLoadInventory( Worker parWorker)
    {
        varWorker=parWorker;
    }

}
