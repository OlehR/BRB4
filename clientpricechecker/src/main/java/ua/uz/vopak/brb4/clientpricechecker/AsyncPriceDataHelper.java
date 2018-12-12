package ua.uz.vopak.brb4.clientpricechecker;

import android.os.AsyncTask;

import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class AsyncPriceDataHelper extends AsyncTask<String , Void, LabelInfo>
{
    ClientPriceCheckerActivity activity;
    @Override
    protected LabelInfo doInBackground(String... param)
    {
        GetDataHTTP Http = new GetDataHTTP();
        LabelInfo LI = new LabelInfo(null);
        String BarCode = param[0].replace("\n","");
        Config config = Config.instance(activity);

        String resHttp=Http.GetData(config.getCodeWarehouse(),BarCode,"");
        resHttp=resHttp.replace("&amp;","&");

        LI.InfoHTTP= Http.HttpState.name();
        if(resHttp!=null && !resHttp.isEmpty())
        {
            LI.Init(resHttp);
        }

        return LI;
    }

    @Override
    protected void onPostExecute(LabelInfo parLI)
    {
        activity.setScanResult(parLI);
    };


    public AsyncPriceDataHelper( ClientPriceCheckerActivity context)
    {
        activity=context;
    }

}