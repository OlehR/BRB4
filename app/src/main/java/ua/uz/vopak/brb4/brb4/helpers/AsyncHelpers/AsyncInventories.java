package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.content.Context;
import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncInventories extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    IIncomeRender activity;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.GetDoc(param[0],param[1],activity);
        return null;
    }


    public AsyncInventories( Worker parWorker, IIncomeRender context)
    {
        activity = context;
        varWorker = parWorker;
    }

}