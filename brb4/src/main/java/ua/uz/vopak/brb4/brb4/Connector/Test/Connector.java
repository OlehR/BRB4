package ua.uz.vopak.brb4.brb4.Connector.Test;

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
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.HttpResult;
import ua.uz.vopak.brb4.lib.models.ParseBarCode;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector.PSU";
    Gson gson = new Gson();

    public Result Login(final String pLogin, final String pPassWord,final boolean pIsLoginCO) {

        try {

            if("test".equals(pLogin)) {
                config.Role= eRole.Admin;
                return new Result();
            }
            else
                return new Result(-1,"Error", "Неправильний логін або пароль");

        }catch (Exception e){
            return new Result(-1,e.getMessage());
        }

    }

    //Завантаження Списку складів (HTTP)
    public Warehouse[] LoadWarehouse() {
        Warehouse[] res = new Warehouse[1];
        res[0]= new Warehouse(1,"1","Тестовий склад","","127.0.0.1","127.0.0.1")
        return res;
    }

    //Завантаження довідників.
    public boolean LoadGuidData(boolean IsFull, ObservableInt pProgress) {
        if (pProgress != null)
            pProgress.set(5);

        String data = config.GetApiJson(150,BuildConfig.VERSION_CODE ,"\"TypeDoc\":-1");
        HttpResult result = Http.   HTTPRequest("http://api.spar.uz.ua/znp/", data, "application/json; charset=utf-8", "nov", "123");

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
    };

    //Завантаження документів в ТЗД (HTTP)
    //PSU Треба перенести в окремий конектор
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {

        return mDbHelper.LoadDataDoc("", pProgress);
    }

    //Вивантаження документів з ТЗД (HTTP)
    public Result SyncDocsData(int parTypeDoc, String NumberDoc, List<WaresItemModel> Wares, Date pDateOutInvoice, String pNumberOutInvoice, int pIsClose) {
       return new Result();
    }

    public Result SendLogPrice(List<LogPrice> pList) {
        return new Result();
    }

    // Друк на стаціонарному термопринтері
    public String printHTTP(List<String> codeWares) {
        //String listString = String.join(", ", codeWares);
        return "true";
    }

    public ParseBarCode ParsedBarCode(String pBarCode,boolean pIsOnlyBarCode) {
        ParseBarCode res =  new ParseBarCode();
        if(pBarCode==null)
            return res;
        pBarCode=pBarCode.trim();
        res.BarCode=pBarCode;
        res.IsOnlyBarCode=pIsOnlyBarCode;


        if (!pIsOnlyBarCode && pBarCode.length() <= 8 && !pBarCode.equals("")) {
            try{
                res.Article = "0000000000".substring(0,8-pBarCode.length())+pBarCode;
                res.BarCode = null;
                return res;
            }catch(Exception e)
            {
                Utils.WriteLog("e",TAG,"ParsedBarCode=> "+ pBarCode,e);
            }
        }

        if(pBarCode!=null  )
        {
            Utils.WriteLog( "e",TAG,"ParsedBarCode=> "+ pBarCode.charAt(0)+ " " + pBarCode.contains("|"));
            if( pBarCode.contains("|")&& pBarCode.charAt(0) == 'Б') {
                res.Code=200000000+ Integer.valueOf(pBarCode.substring(1,9));
                res.Quantity=1;
                res.BarCode=null;
            }
            else
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
                   Utils.WriteLog("e",TAG,"PriceBarCode", e);
                }
            }
            else
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

    @Override
    public Result CreateNewDoc(int pTypeDoc,int pCodeWarehouseFrom,int pCodeWarehouseTo) {
        return null;
    }

    public WaresItemModel GetWares(int pCodeWares,boolean IsSimpleDoc){return null;};
}
