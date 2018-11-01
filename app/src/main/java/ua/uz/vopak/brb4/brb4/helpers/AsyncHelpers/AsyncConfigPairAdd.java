
package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;
import ua.uz.vopak.brb4.brb4.helpers.Worker;

public class AsyncConfigPairAdd extends AsyncTask<String , Void, Void> {
    Worker varWorker;
    @Override
    protected Void doInBackground(String... param)
    {
        varWorker.AddConfigPair(param[0], param[1]);
        return null;
    }


    public AsyncConfigPairAdd( Worker parWorker)
    {
        varWorker=parWorker;
    }

}
