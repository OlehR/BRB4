package ua.uz.vopak.brb4.brb4.Connector.PSU;

import android.app.Activity;
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
import ua.uz.vopak.brb4.brb4.models.ParseBarCode;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.HttpResult;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector.PSU";
    Gson gson = new Gson();

    public Result Login(final String pLogin, final String pPassWord,final boolean pIsLoginCO) {
        final String data = "{\"CodeData\": \"1\"" + ", \"Login\": \"" + pLogin + "\"" + ", \"PassWord\": \"" + pPassWord + "\"}";
        HttpResult result = Http.HTTPRequest(0, "",data,"application/json; charset=utf-8",null,null);

        if (result.HttpState!= eStateHTTP.HTTP_OK )
            return new Result(result,"Ви не підключені до мережі " + config.Company.name());
         else
        try {
            JSONObject jObject = new JSONObject(result.Result);
            if(jObject.getInt("State") == 0) {
                config.Role= eRole.Admin;
                return new Result();
            }
            else
                return new Result(jObject.getInt("State"),jObject.getString("TextError"), "Неправильний логін або пароль");

        }catch (Exception e){
            return new Result(-1,e.getMessage());
        }

    }


    //Завантаження Списку складів (HTTP)
    public Warehouse[] LoadWarehouse() {
        Warehouse[] res = null;
        String data = config.GetApiJson(210, "");
        try {
            HttpResult result = Http.HTTPRequest(0, "", data, "application/json; charset=utf-8", null, null);
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

        } catch (Exception e) {
            Utils.WriteLog("e",TAG, "LoadWarehouse=>" + e.getMessage());
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
        HttpResult result = Http.HTTPRequest(0, "", data, "application/json; charset=utf-8", null, null);
        if (result.HttpState != eStateHTTP.HTTP_OK) {
            Utils.WriteLog("e",TAG, "Load=>" + result.HttpState.toString());
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
            if(ware.InputQuantity>0) {
                String war = "";
                war += "[" + ware.GetOrderDoc() + ",";
                war += ware.GetCodeWares() + ",";
                war += ware.GetInputQuantityZero() + "]";
                wares.add(war);
            }
        }
        String data = config.GetApiJson(153, "\"TypeDoc\":" + parTypeDoc + ",\"NumberDoc\":\"" + NumberDoc + "\",\"Wares\":[" + TextUtils.join(",", wares) + "]");
        try {
            HttpResult result = Http.HTTPRequest(0, "", data, "application/json; charset=utf-8", null, null);
            if (result.HttpState != eStateHTTP.HTTP_OK) {
                return new Result(result);
            }

            Result res = gson.fromJson(result.Result, Result.class);
            return res;
        } catch (Exception e) {
            Utils.WriteLog("e",TAG, "SyncDocsData=>" +e.getMessage()+" " +data);
            return new Result(-1, e.getMessage()+data);
        }
    }

    public Result SendLogPrice(List<LogPrice> pList) {
        List<String> ll = new ArrayList<>(pList.size());
        for (LogPrice el : pList)
            ll.add(el.GetJsonPSU());

        String a = new Gson().toJson(ll);
        String data = config.GetApiJson(141, "\"LogPrice\":" + a);

        HttpResult res = Http.HTTPRequest(0, "", data, "application/json; charset=utf-8", null, null);
        if (res.HttpState == eStateHTTP.HTTP_OK) {
            try {
                return gson.fromJson(res.Result, Result.class);

            } catch (Exception e) {
                Utils.WriteLog("e",TAG, "SendLogPrice  >>" + e.getMessage());
                return new Result(-1, e.getMessage());
            }
        } else {
            Utils.WriteLog("e",TAG, "SendLogPrice  >>" + res.HttpState.toString());
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
            String json = config.GetApiJson(999, "\"CodeWares\":\"" + sb.toString() + "\"");
            HttpResult res = Http.HTTPRequest(1, "", json, "application/json;charset=UTF-8", null, null);//"http://znp.vopak.local:8088/Print"
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                return res.Result;
                //JSONObject jObject = new JSONObject(result.Result);
            }
            return res.HttpState.toString();
        }
        catch (Exception ex)
        {
            Utils.WriteLog("e",TAG, "printHTTP  >>" + ex.getMessage() );
            return  ex.getMessage();
        }
    }

    public ParseBarCode ParsedBarCode(String pBarCode,boolean pIsOnlyBarCode) {
        ParseBarCode res =  new ParseBarCode();
        if(pBarCode==null)
            return res;
        pBarCode=pBarCode.trim();
        res.BarCode=pBarCode;
        res.IsOnlyBarCode=pIsOnlyBarCode;
        /*if(pIsOnlyBarCode)
            return res;*/

        if (!pIsOnlyBarCode && pBarCode.length() <= 8 && !pBarCode.equals("")) {
            try{
                res.Article = "0000000000".substring(0,8-pBarCode.length())+pBarCode;
                res.BarCode = null;
                return res;
            }catch(Exception e)
            {
                Utils.WriteLog("e",TAG,"ParsedBarCode=> "+ pBarCode+" "+e.getMessage());
            }
        }

        if((config.Company== eCompany.SparPSU || config.Company== eCompany.VopakPSU) && pBarCode!=null  )
        {
            if( pBarCode.contains("-")) {
                try {
                    String[] str = pBarCode.split("-");
                    switch (str.length) {
                        case 3:
                            res.PriceOpt = Integer.parseInt(str[2]) / 100d;
                        case 2:
                            res.Price = Integer.parseInt(str[1]) / 100d;
                            res.Code = Integer.parseInt(str[0]);
                            res.BarCode=null;
                            break;
                    }
                } catch (Exception e) {
                    Log.e("PriceBarCode", e.getMessage());
                }
            }
            if(pBarCode.length()==13)
            {
              //  Log.e("XXX",number+' ' +number.substring(0,1));
                if(pBarCode.substring(0,2).equals("22"))
                {
                    res.Article=pBarCode.substring(2,8);
                    String Quantity=pBarCode.substring(8,12);
                    res.Quantity=Double.parseDouble(Quantity)/1000d;
                   // Log.e("XXX",Article+" "+ Quantity );
                }

                if(pBarCode.substring(0,3).equals("111"))
                {

                    //isBarCode=false;
                    res.Article=pBarCode.substring(3,9);
                    String Quantity=pBarCode.substring(9,12);
                    res.Quantity=Double.parseDouble(Quantity);
                    //Log.e("XXX",Article+" "+ Quantity );
                }

                if(res.Article!=null) {
                    res.Article = "00" + res.Article;
                    res.BarCode=null;
                }
            }

        }
        return res;
    }
}
