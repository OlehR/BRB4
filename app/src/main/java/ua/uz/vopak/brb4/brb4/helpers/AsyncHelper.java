package ua.uz.vopak.brb4.brb4.helpers;

import android.os.AsyncTask;

public class AsyncHelper<T> extends AsyncTask<Void,Void,T> {

    IAsyncHelper<T> BackgroundDelegate;
    IPostResult<T> PostResultDelegate;
    @Override
    protected T doInBackground(Void... voids) {
        return BackgroundDelegate.Invoke();
    }

    @Override
    protected void onPostExecute(T t) {
        if(t != null && !(t instanceof Void))
        PostResultDelegate.Invoke(t);
    }

    public AsyncHelper(IAsyncHelper dlg){ BackgroundDelegate = dlg;}
    public AsyncHelper(IAsyncHelper bDlg, IPostResult pDlg){ BackgroundDelegate = bDlg; PostResultDelegate = pDlg;}
}