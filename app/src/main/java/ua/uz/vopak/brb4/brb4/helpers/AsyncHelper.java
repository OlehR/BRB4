package ua.uz.vopak.brb4.brb4.helpers;

import android.os.AsyncTask;

public class AsyncHelper extends AsyncTask<Void,Void,Void> {

    IAsyncHelper delegate;
    @Override
    protected Void doInBackground(Void... voids) {
        delegate.Invoke();
        return null;
    }

    public AsyncHelper(IAsyncHelper dlg){ delegate = dlg;}
}