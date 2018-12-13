
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncLoadDataDoc extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.LoadDataDoc(param[0],param[1]);
        return null;
    }
/*
    @Override
    protected void onPostExecute(Void)
    {
        //varWorker.scanerContext.setScanResult(parLI);
    };*/


    public AsyncLoadDataDoc( Worker parWorker)
    {
        varWorker=parWorker;
    }

}
