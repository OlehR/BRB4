package ua.uz.vopak.brb4.brb4.helpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.models.LabelInfo;

public class AsyncWorker extends  AsyncTask<String , Void, LabelInfo>
{
    Worker varWorker;
    @Override
    protected LabelInfo doInBackground(String... param)
    {

        return varWorker.Start(param[0]);
    }

    @Override
    protected void onPostExecute(LabelInfo parLI)
    {
        varWorker.scanerContext.setScanResult(parLI);
    };


    public AsyncWorker( Worker parWorker)
    {
        varWorker=parWorker;
    }

}
