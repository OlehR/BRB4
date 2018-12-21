package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.app.Activity;
import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncGetQuantity extends AsyncTask<String , Void, Void>{
    Worker worker;
    Activity context;

    @Override
    protected Void doInBackground(String... params) {
        worker.GetQuantity(params[0], params[1], params[2], context);
        return null;
    }

    public AsyncGetQuantity(Worker parWorker, Activity activity){
        worker = parWorker;
        context = activity;
    }
}
