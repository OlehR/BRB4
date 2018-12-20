package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncUpdateDocState extends AsyncTask<String , Void, Void> {
    Worker worker;
    DocumentItemsActivity activity;

    @Override
    protected Void doInBackground(String... param) {
        worker.UpdateDocState(param[0],param[1], param[2],activity);
        return null;
    }

    public AsyncUpdateDocState(Worker w, DocumentItemsActivity context){
        worker = w;
        activity = context;
    }
}