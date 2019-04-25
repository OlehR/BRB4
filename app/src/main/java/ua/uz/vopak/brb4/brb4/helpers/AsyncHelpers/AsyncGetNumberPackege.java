package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

 import android.os.AsyncTask;

 import java.text.*;
 import java.util.*;

 import ua.uz.vopak.brb4.brb4.helpers.Worker;
 import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class AsyncGetNumberPackege extends AsyncTask<Void , Void, Void> {

    @Override
    protected Void doInBackground(Void... param)
    {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);

        Worker varWorker =GlobalConfig.GetWorker();
        String var  = varWorker.GetConfigPair("NumberPackege");
        String varNumberPackege="1";
        if(var.length()>8 && var.substring(0,8).equals(todayAsString))
        {
            varNumberPackege = var.substring(8);
        }
        else
          varWorker.AddConfigPair("NumberPackege",todayAsString+ varNumberPackege);

        GlobalConfig.NumberPackege=Integer.valueOf(varNumberPackege);
        return null;
    }

}
