package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

        import android.os.AsyncTask;
        import ua.uz.vopak.brb4.brb4.helpers.Worker;
        import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class AsyncGetWarehouseConfig extends AsyncTask<Void , Void, Void> {

    @Override
    protected Void doInBackground(Void... param)
    {
        Worker varWorker =GlobalConfig.GetWorker();
        GlobalConfig.CodeWarehouse = varWorker.GetConfigPair("Warehouse");
        return null;
    }




}
