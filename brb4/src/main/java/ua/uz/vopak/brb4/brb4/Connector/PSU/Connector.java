package ua.uz.vopak.brb4.brb4.Connector.PSU;

import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.LogPrice;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.models.HttpResult;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector.PSU";
    Gson gson = new Gson();

    //Завантаження Списку складів (HTTP)
    public Warehouse[] LoadWarehouse() {
        Warehouse[] res = null;
        String data = config.GetApiJson(210, "");
        try {
            HttpResult result = Http.HTTPRequest(0, "", data, null, null, null);
            if (result.HttpState == eStateHTTP.HTTP_OK) {
                JSONObject jObject = new JSONObject(result.Result);

                if (jObject.getInt("State") == 0) {
                    JSONArray arrJson = jObject.getJSONArray("Warehouse");
                    res = new Warehouse[arrJson.length()];

                    for (int i = 0; i < arrJson.length(); i++) {
                        JSONArray innerArr = arrJson.getJSONArray(i);
                        int Code = innerArr.getInt(0);
                        String Name = innerArr.getString(1);
                        res[i] = new Warehouse(Code, Integer.toString(Code), Name, "", "", "");
                    }
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, "LoadWarehouse=>" + ex.getMessage());
        }
        return res;
    }

    //Завантаження довідників.
    public boolean LoadGuidData(boolean IsFull, ObservableInt pProgress) {
        return true;
    }

    ;

    //Завантаження документів в ТЗД (HTTP)
    //PSU Треба перенести в окремий конектор
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {
        if (pProgress != null)
            pProgress.set(5);

        String data = config.GetApiJson(150, "\"TypeDoc\":" + pTypeDoc);
        HttpResult result = Http.HTTPRequest(0, "", data, null, null, null);
        if (result.HttpState != eStateHTTP.HTTP_OK) {
            Log.e(TAG, "Load=>" + result.HttpState.toString());
            if (pProgress != null)
                pProgress.set(0);
            return false;
        }

        Log.d(TAG, "Load=>" + result.Result.length());
        if (pProgress != null)
            pProgress.set(45);
        return mDbHelper.LoadDataDoc(result.Result, pProgress);
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
            HttpResult result = Http.HTTPRequest(0, "", data, null, null, null);
            if (result.HttpState != eStateHTTP.HTTP_OK) {
                return new Result(result);
            }

            Result res = gson.fromJson(result.Result, Result.class);
            return res;
        } catch (Exception e) {
            return new Result(-1, e.getMessage());
        }
    }

    public Result SendLogPrice(List<LogPrice> pList) {
        List<String> ll = new ArrayList<>(pList.size());
        for (LogPrice el : pList)
            ll.add(el.GetJsonPSU());

        String a = new Gson().toJson(ll);
        String data = config.GetApiJson(141, "\"LogPrice\":" + a);

        HttpResult res = Http.HTTPRequest(0, "", data, null, null, null);
        if (res.HttpState == eStateHTTP.HTTP_OK) {
            try {
                return gson.fromJson(res.Result, Result.class);

            } catch (Exception e) {
                Log.e(TAG, "SendLogPrice  >>" + e.getMessage());
                return new Result(-1, e.getMessage());
            }
        } else {
            Log.e(TAG, "SendLogPrice  >>" + res.HttpState.toString());
            return new Result(res);
        }


    }

    // Друк на стаціонарному термопринтері
    public String printHTTP(List<String> codeWares) {
        //String listString = String.join(", ", codeWares);
        try {
            StringBuilder sb = new StringBuilder();
            for (String s : codeWares) {
                sb.append(s);
                sb.append(",");
            }
            String json = "{\"CodeWares\":\"" + sb.toString() + "\",\"CodeWarehouse\":" + config.getCodeWarehouse() + "}";
            HttpResult res = Http.HTTPRequest(1, "", json, "application/json;charset=UTF-8", null, null);//"http://znp.vopak.local:8088/Print"
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                return res.Result;
                //JSONObject jObject = new JSONObject(result.Result);
            }
            return res.HttpState.toString();
        }
        catch (Exception ex)
        {
            Log.e(TAG, "printHTTP  >>" + ex.getMessage() );
            return  ex.getMessage();
        }
    }
}
