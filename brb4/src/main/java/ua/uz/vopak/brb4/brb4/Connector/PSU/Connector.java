package ua.uz.vopak.brb4.brb4.Connector.PSU;

import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.SQLiteAdapter;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector.PSU";


    //Завантаження Списку складів (HTTP)
    public Warehouse[] LoadWarehouse() {
        Warehouse[] res=null;
        String data = config.GetApiJson(210,"");
        try {
            String result = Http.HTTPRequest(config.ApiUrl, data);
            JSONObject jObject = new JSONObject(result);

            if (jObject.getInt("State") == 0) {
                JSONArray arrJson = jObject.getJSONArray("Warehouse");
                res = new Warehouse[arrJson.length()];

                for (int i = 0; i < arrJson.length(); i++) {
                    JSONArray innerArr = arrJson.getJSONArray(i);
                    int Code= innerArr.getInt(0);
                    String Name = innerArr.getString(1);
                    res[i]= new Warehouse(Code,Integer.toString(Code),Name,"","","");
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, "LoadWarehouse=>" + ex.getMessage());
        }
        return res;
    }

    //Завантаження довідників.
    public boolean LoadGuidData(boolean IsFull, ObservableInt pProgress){return true;};

    //Завантаження документів в ТЗД (HTTP)
    //PSU Треба перенести в окремий конектор
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {
        if(pProgress!=null)
            pProgress.set(5);

        String data = config.GetApiJson(150, "\"TypeDoc\":" + pTypeDoc);
        String result = Http.HTTPRequest(config.ApiUrl, data);
        Log.d(TAG, "Load=>"+result.length());
        if(Http.HttpState!= eStateHTTP.HTTP_OK) {
            if(pProgress!=null)
                pProgress.set(0);
            return false;
        }
        if(pProgress!=null)
            pProgress.set(45);
        return mDbHelper.LoadDataDoc(result,pProgress);
    }


    //Вивантаження документів з ТЗД (HTTP)
    public Result SyncDocsData(int parTypeDoc, String NumberDoc, List<WaresItemModel> Wares, Date pDateOutInvoice, String pNumberOutInvoice, int pIsClose) {
        List<String> wares = new ArrayList<String>();
        for (WaresItemModel ware : Wares) {
            String war = "";
            war += "[" + ware.GetOrderDoc() + ",";
            war += ware.GetCodeWares() + ",";
            war += ware.GetInputQuantityZero() + "]";
            wares.add(war);
        }
        String data = config.GetApiJson(153, "\"TypeDoc\":" + parTypeDoc + ",\"NumberDoc\":\"" + NumberDoc + "\",\"Wares\":[" + TextUtils.join(",", wares) + "]");
        try {
            String result = Http.HTTPRequest(config.ApiUrl, data);
            Gson gson = new Gson();
            Result res= gson.fromJson(result, Result.class);
            return  res;
        }
        catch(Exception e)
        {
            return new Result(-1,e.getMessage());
        }
    }

}
