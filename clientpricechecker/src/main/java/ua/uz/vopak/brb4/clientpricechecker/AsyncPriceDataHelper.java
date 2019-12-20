package ua.uz.vopak.brb4.clientpricechecker;

import android.os.AsyncTask;

import org.json.JSONObject;

import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class AsyncPriceDataHelper extends AsyncTask<String , Void, LabelInfo>
{
    ClientPriceCheckerActivity activity;
    @Override
    protected LabelInfo doInBackground(String... param)
    {
        LabelInfo LI = new LabelInfo(null);
        String BarCode = param[0].replace("\n",""),CodeWares="";


        Config config = Config.instance(activity);

        LI = new PricecheckerHelper().getPriceCheckerData(LI,BarCode,false,config);
        if(LI.resHttp!=null && !LI.resHttp.isEmpty())
        {
            try {
                LI.Init(new JSONObject(LI.resHttp));
            }catch (Exception e){
                e.getMessage();
            }
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