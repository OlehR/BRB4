package ua.uz.vopak.brb4.brb4;

import android.os.AsyncTask;
import com.journeyapps.barcodescanner.BarcodeResult;

public class AsyncWorker extends  AsyncTask<BarcodeResult , Void, LabelInfo>
{
    Worker varWorker;
    @Override
    protected LabelInfo doInBackground(BarcodeResult... param)
    {

        return varWorker.Start(param[0]);
    }

    @Override
    protected void onPostExecute(LabelInfo parLI)
    {
        varWorker.scanerContext.setScanResult(parLI);
    };


    public AsyncWorker( Worker parWorker)
    {
        varWorker=parWorker;
    }

}
