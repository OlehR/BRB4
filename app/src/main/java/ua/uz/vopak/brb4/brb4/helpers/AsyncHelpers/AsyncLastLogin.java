
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;

public class AsyncLastLogin extends AsyncTask<Void , Void, Void> {
    AuterizationsHelper auth;
    @Override
    protected Void doInBackground(Void... param)
    {
        auth.GetLastLogin();
        return null;
    }


    public AsyncLastLogin( AuterizationsHelper Auth)
    {
        auth=Auth;
    }


}
