package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;
import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;

public class AsyncAuthHelper extends AsyncTask<String , Void, AuterizationsHelper>
{
    AuterizationsHelper auth;
    @Override
    protected AuterizationsHelper doInBackground(String... param)
    {

        return auth.Start(param[0]);
    }

    @Override
    protected void onPostExecute(AuterizationsHelper parLI)
    {
    }

    public  AsyncAuthHelper(AuterizationsHelper authH){
        auth = authH;
    }
}