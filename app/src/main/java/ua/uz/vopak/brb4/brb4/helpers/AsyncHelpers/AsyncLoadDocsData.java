package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncLoadDocsData extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    MainActivity context;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.LoadDocsData(param[0],context);
        return null;
    }

    public AsyncLoadDocsData(Worker parWorker, MainActivity parContext)
    {
        varWorker = parWorker;
        context = parContext;
    }
}
