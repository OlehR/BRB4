package ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers;

import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import ua.uz.vopak.brb4.brb4.R;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.HashMapHelper;
import ua.uz.vopak.brb4.brb4.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.WareListHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class AsyncWaresHelper extends AsyncTask<String , Void, WareListHelper>
{
    WareListHelper wares;
    Spinner warList;
    GlobalConfig config = GlobalConfig.instance();

    @Override
    protected WareListHelper doInBackground(String... param)
    {
        return wares.getWares();
    }

    @Override
    protected void onPostExecute(final WareListHelper wH)
    {
        warList = wH.activity.findViewById(R.id.wares);

        warList.setAdapter(wH.adapter);
        warList.setPrompt("Склад");
        try {
            warList.setSelection(wH.adapter.getPosition(HashMapHelper.getKeyFromValue(wH.map, config.CodeWarehouse).toString()));
        }
        catch (Exception e)
        {};
        warList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                config.CodeWarehouse = wH.map.get(warList.getSelectedItem().toString());
                //new AsyncConfigPairAdd(config.GetWorker()).execute("Warehouse", config.CodeWarehouse);
                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.AddConfigPair("Warehouse",config.CodeWarehouse);
                        return null;
                    }
                }).execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public  AsyncWaresHelper(WareListHelper War){
        wares = War;
    }
}
