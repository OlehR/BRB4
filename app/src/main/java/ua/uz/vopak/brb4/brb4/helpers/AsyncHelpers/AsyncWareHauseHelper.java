package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;
import ua.uz.vopak.brb4.brb4.helpers.WareHauseHelper;

public class AsyncWareHauseHelper extends AsyncTask<String , Void, WareHauseHelper>
{
    WareHauseHelper wares;
    @Override
    protected WareHauseHelper doInBackground(String... param)
    {

        return wares.getWares();
    }

    @Override
    protected void onPostExecute(WareHauseHelper parLI)
    {
    }

    public AsyncWareHauseHelper(WareHauseHelper War){
        wares = War;
    }
}
