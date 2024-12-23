package ua.uz.vopak.brb4.brb4.Connector.PSU;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.BuildConfig;
import ua.uz.vopak.brb4.brb4.helpers.LogPrice;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeControlDoc;
import ua.uz.vopak.brb4.lib.enums.eTypeCreate;
import ua.uz.vopak.brb4.lib.models.ParseBarCode;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.HttpResult;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector.PSU";
    Gson gson = new Gson();
    public DocSetting[] GenSettingDocs(eCompany pCompany, eRole pProfile) {
        DocSetting[] Setting = new DocSetting[7];
        Setting[0] = new DocSetting(1, "Ревізія", eTypeControlDoc.Ask, false, false, false, false, true, 1, 1, 0, false, true, false, false, false, 0, eTypeCreate.None, false);
        Setting[1] = new DocSetting(2, "Прихід", eTypeControlDoc.Control, false, false, false, true, true, 1, 5, 3, true, true, true, false, false, 0, eTypeCreate.None, false);
        Setting[2] = new DocSetting(3, "Переміщення Вих", eTypeControlDoc.Ask, false, false, false, true, true, 1, 5, 3, true, true, true, false, false, 0, eTypeCreate.None, false);
        Setting[3] = new DocSetting(4, "Списання");
        Setting[4] = new DocSetting(5, "Повернення");
        Setting[5] = new DocSetting(7, "Ревізія ОЗ", eTypeControlDoc.Ask, true, false, false, false, false, 1, 6, 0, false, false, true, false, true, 0, eTypeCreate.WithWarehouseTo, false);
        Setting[6] = new DocSetting(8, "Переміщення Вх", eTypeControlDoc.Ask, false, false, true, true, true, 1, 5, 3, true, true, true, false, false, 0, eTypeCreate.None, false);
        return Setting;
    }
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
        String data = config.GetApiJson(210, BuildConfig.VERSION_CODE,"");
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
            Utils.WriteLog("e",TAG, "LoadWarehouse=>" , e);
        }
        return res;
    }

    //Завантаження довідників.
    public boolean LoadGuidData(boolean IsFull, ObservableInt pProgress) {
        return true;
    };

    //Завантаження документів в ТЗД (HTTP)
    //PSU Треба перенести в окремий конектор
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {
        if (pProgress != null)
            pProgress.set(5);

        String data = config.GetApiJson(150,BuildConfig.VERSION_CODE ,"\"TypeDoc\":" + pTypeDoc);
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
                war += ware.GetInputQuantityZero() +",";
                war +=Integer.toString(ware.CodeReason) + "]";
                wares.add(war);
            }
        }
        String data = config.GetApiJson(153, BuildConfig.VERSION_CODE,"\"TypeDoc\":" + parTypeDoc + ",\"NumberDoc\":\"" + NumberDoc + "\",\"Wares\":[" + TextUtils.join(",", wares) + "]");
        try {
            HttpResult result = Http.HTTPRequest(0, "", data, "application/json; charset=utf-8", null, null);
            if (result.HttpState != eStateHTTP.HTTP_OK) {
                return new Result(result);
            }

            Result res = gson.fromJson(result.Result, Result.class);
            return res;
        } catch (Exception e) {
            Utils.WriteLog("e",TAG, "SyncDocsData=>" +data,e);
            return new Result(-1, e.getMessage()+data);
        }
    }

    public Result SendLogPrice(List<LogPrice> pList) {
        List<String> ll = new ArrayList<>(pList.size());

        StringBuilder a = new StringBuilder("");

        for (LogPrice el : pList)
            a.append(","+el.GetJsonPSU());

        if(a.length()<=1)
            return new Result(-1, "Відсутні дані на відправку");
        //String a = new Gson().toJson(ll);
        String data = config.GetApiJson(141, BuildConfig.VERSION_CODE,"\"LogPrice\":[" +a.substring(1).toString()+"]");

        HttpResult res = Http.HTTPRequest(0, "", data, "application/json; charset=utf-8", null, null);
        if (res.HttpState == eStateHTTP.HTTP_OK) {
            try {
                return gson.fromJson(res.Result, Result.class);

            } catch (Exception e) {
                Utils.WriteLog("e",TAG, "SendLogPrice  >>",  e);
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
            String json = config.GetApiJson(999, BuildConfig.VERSION_CODE,"\"CodeWares\":\"" + sb.toString() + "\"");
            HttpResult res = Http.HTTPRequest(1, "", json, "application/json;charset=UTF-8", null, null);//"http://znp.vopak.local:8088/Print"
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                return res.Result;
                //JSONObject jObject = new JSONObject(result.Result);
            }
            return res.HttpState.toString();
        }
        catch (Exception e)
        {
            Utils.WriteLog("e",TAG, "printHTTP  >>" , e);
            return  e.getMessage();
        }
    }

    public ParseBarCode ParsedBarCode(String pBarCode,boolean pIsOnlyBarCode) {
        ParseBarCode res = new ParseBarCode();
        if (pBarCode == null)
            return res;
        pBarCode = pBarCode.trim();
        res.BarCode = pBarCode;
        res.IsOnlyBarCode = pIsOnlyBarCode;
        /*if(pIsOnlyBarCode)
            return res;*/

        if (!pIsOnlyBarCode && pBarCode.length() <= 8 && !pBarCode.equals("")) {
            try {
                res.Article = "0000000000".substring(0, 8 - pBarCode.length()) + pBarCode;
                res.BarCode = null;
            } catch (Exception e) {
                Utils.WriteLog("e", TAG, "ParsedBarCode=> " + pBarCode, e);
            }
            return res;
        }

        Utils.WriteLog("e", TAG, "ParsedBarCode=> "+pBarCode + "  " + String.valueOf(pBarCode.length()));
        if (pBarCode.contains("|") && pBarCode.charAt(0) == 'Б') {
            res.Code = 200000000 + Integer.valueOf(pBarCode.substring(1, 9));
            res.Quantity = 1;
            res.BarCode = null;
            return res;
        }

        if (pBarCode.contains("-")) {
            try {
                String[] str = pBarCode.split("-");
                switch (str.length) {
                    case 3:
                        res.PriceOpt = Integer.parseInt(str[2]) / 100d;
                    case 2:
                        res.Price = Integer.parseInt(str[1]) / 100d;
                        res.Code = Integer.parseInt(str[0]);
                        res.BarCode = null;
                        break;
                }
            } catch (Exception e) {
                Utils.WriteLog("e", TAG, "PriceBarCode", e);
            }
            return res;
        }

        if (pBarCode.length() == 13) {
            //  Log.e("XXX",number+' ' +number.substring(0,1));
            if (pBarCode.substring(0, 2).equals("22")) {
                res.Article = pBarCode.substring(2, 8);
                String Quantity = pBarCode.substring(8, 12);
                res.Quantity = Double.parseDouble(Quantity) / 1000d;
                // Log.e("XXX",Article+" "+ Quantity );
            }

            if (pBarCode.substring(0, 3).equals("111")) {
                //isBarCode=false;
                res.Article = pBarCode.substring(3, 9);
                String Quantity = pBarCode.substring(9, 12);
                res.Quantity = Double.parseDouble(Quantity);
                //Log.e("XXX",Article+" "+ Quantity );
            }
        }
        if (pBarCode.length() == 16 && pBarCode.startsWith("22")) {
            res.Article = pBarCode.substring(2, 8);
            String Quantity = pBarCode.substring(11, 16);
            res.Quantity = Double.parseDouble(Quantity)/ 1000d;
        }

        if (res.Article != null) {
            res.Article = "00" + res.Article;
            res.BarCode = null;
        }

        Utils.WriteLog("I", TAG, "Article=> "+res.Article + "  " + String.valueOf(res.Quantity));
        return res;
    }

    @Override
    public Result CreateNewDoc(int pTypeDoc,int pCodeWarehouseFrom,int pCodeWarehouseTo) {
        return null;
    }

    public WaresItemModel GetWares(int pCodeWares,boolean IsSimpleDoc){return null;};
}
