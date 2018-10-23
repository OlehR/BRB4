package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.SQLiteAdapter;
import ua.uz.vopak.brb4.brb4.helpers.WareListHelper;

public class AsyncSyncData extends AsyncTask<String , Void, SQLiteAdapter> {
    Context mContext;
    @Override
    protected SQLiteAdapter doInBackground(String... param)
    {
        SQLiteAdapter adapter = new SQLiteAdapter(mContext);
        List<ArrayList> list = adapter.GetSendData();

        return  adapter;
    }

    public AsyncSyncData(Context c){
        mContext = c;
    }
}
