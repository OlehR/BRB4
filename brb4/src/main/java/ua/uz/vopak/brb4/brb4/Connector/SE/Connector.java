package ua.uz.vopak.brb4.brb4.Connector.SE;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.LogPrice;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
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

    protected static final String TAG = "BRB4/Connector.SE";

    public Result Login(final String pLogin, final String pPassWord, final boolean pIsLoginCO) {
        HttpResult res = Http.HTTPRequest(pIsLoginCO ? 1 : 0, "login", "{\"login\" : \"" + pLogin + "\"}", "application/json;charset=utf-8", pLogin, pPassWord);
        if (res.HttpState == eStateHTTP.HTTP_UNAUTHORIZED || res.HttpState == eStateHTTP.HTTP_Not_Define_Error) {
            Utils.WriteLog("e", TAG, "Login >>" + res.HttpState.toString());
            return new Result(-1, res.HttpState.toString(), "Неправильний логін або пароль");
        } else if (res.HttpState != eStateHTTP.HTTP_OK)
            return new Result(res, "Ви не підключені до мережі " + config.Company.name());
        else {
            try {
                JSONObject jObject = new JSONObject(res.Result);
                if (jObject.getInt("State") == 0) {
                    config.Role = eRole.fromOrdinal(jObject.getInt("Profile"));
                    return new Result();
                } else
                    return new Result(jObject.getInt("State"), jObject.getString("TextError"), "Неправильний логін або пароль");

            } catch (Exception e) {
                Utils.WriteLog("e",  TAG , "Login=>" + e.getMessage());
                return new Result(-1, e.getMessage());
            }

        }


    }

    //Завантаження довідників.
    public boolean LoadGuidData(boolean IsFull, ObservableInt pProgress) {
        try {
            Log.d(TAG, "Start");


            pProgress.set(5);
            HttpResult res = Http.HTTPRequest(config.IsLoginCO ? 1 : 0, "nomenclature", null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                Log.d(TAG, "LoadData=>" + res.Result.length());
                pProgress.set(40);
                if (IsFull) {
                    db.execSQL("DELETE FROM Wares");
                    db.execSQL("DELETE FROM ADDITION_UNIT");
                    db.execSQL("DELETE FROM BAR_CODE");
                    db.execSQL("DELETE FROM UNIT_DIMENSION");
                }
                Log.d(TAG, "DELETE");
                pProgress.set(45);
                InputData data = new Gson().fromJson(res.Result, InputData.class);

                Log.d(TAG, "Parse JSON");
                pProgress.set(60);
                mDbHelper.SaveWares(data.Nomenclature);
                Log.d(TAG, "Nomenclature");
                pProgress.set(70);
                mDbHelper.SaveAdditionUnit(data.Units);
                Log.d(TAG, "Units");
                pProgress.set(80);
                mDbHelper.SaveBarCode(data.Barcodes);
                Log.d(TAG, "Barcodes");
                pProgress.set(90);
                mDbHelper.SaveUnitDimension(data.Dimentions);
            } else
                Log.d(TAG, res.HttpState.name());

            res = Http.HTTPRequest(1, "reasons", null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                pProgress.set(95);
                List<Reason> Reasons = new Gson().fromJson(res.Result, new TypeToken<List<Reason>>() {
                }.getType());
                db.execSQL("DELETE FROM Reason;");
                mDbHelper.SaveReason(Reasons);
            }
            config.GetWorker().GetWarehouse();
            pProgress.set(100);
            Log.d(TAG, "End");
            return true;
        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "LoadGuidData=>" + e.getMessage());
            Toast toast = Toast.makeText(GlobalConfig.instance().context, "Помилка завантаження довідників=>" + e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
        return false;
    }


    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {
        if (pProgress != null)
            pProgress.set(5);
        HttpResult res;
        try {
            if (pTypeDoc == 5 || pTypeDoc == 6 || (pTypeDoc <= 0 && config.IsLoginCO)) {
                res = Http.HTTPRequest(1, "documents" + (pTypeDoc == 5 ? "\\" + pNumberDoc : "?StoreSetting=" + config.CodeWarehouse), null, "application/json;charset=utf-8", config.Login, config.Password);
            } else
                res = Http.HTTPRequest(0, "documents", null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                if (pProgress != null)
                    pProgress.set(40);
                InputDocs data = new Gson().fromJson(res.Result, InputDocs.class);
                if (pIsClear) {
                    // String sql = "DELETE FROM DOC; DELETE FROM DOC_WARES_sample; DELETE FROM DOC_WARES;";
                    db.execSQL("DELETE FROM DOC");
                    db.execSQL("DELETE FROM DOC_WARES_sample");
                    db.execSQL("DELETE FROM DOC_WARES");
                    // db.execSQL(sql.trim());
                } else
                    db.execSQL("update doc set state=-1 where type_doc not in (5,6)" + (pTypeDoc > 0 ? " and type_doc=" + pTypeDoc : ""));

                for (Doc v : data.Doc) {
                    //v.TypeDoc = ConvertTypeDoc(v.TypeDoc);
                    v.DateDoc = v.DateDoc.substring(0, 10);
                    mDbHelper.SaveDocs(v);
                }
                if (pProgress != null)
                    pProgress.set(60);
                SaveDocWaresSample(data.DocWaresSample);
                if (pProgress != null)
                    pProgress.set(100);
                return true;
            }

        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "LoadDocsData=>" + e.getMessage());
        }
        return false;
    }

    //Вивантаження документів з ТЗД (HTTP)
    public Result SyncDocsData(int pTypeDoc, String pNumberDoc, List<WaresItemModel> pWares, Date pDateOutInvoice, String pNumberOutInvoice, int pIsClose) {
        Gson gson = new Gson();
        SimpleDateFormat formatterDT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat formatterD = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());

        ArrayList<OutputDoc> OD = new ArrayList<>();
        OutputDoc el = new OutputDoc(pTypeDoc, pNumberDoc, formatterDT.format(date), formatterD.format(pDateOutInvoice), pNumberOutInvoice, pIsClose);

        for (WaresItemModel W : pWares) {
            OutputDocWares w = new OutputDocWares(W.CodeWares, W.InputQuantity, W.CodeReason);
            el.DocWares.add(w);
        }
        OD.add(el);
        String json = gson.toJson(OD);

        HttpResult res = Http.HTTPRequest((pTypeDoc == 5 || pTypeDoc == 6 ? 1 : 0), "documentin", json, "application/json;charset=utf-8", config.Login, config.Password);
        if (res.HttpState == eStateHTTP.HTTP_OK) {
            return new Result(res);
        }
        return new Result(-1, res.HttpState.toString());
    }

    boolean SaveDocWaresSample(DocWaresSample[] pDWS) {
        int i = 0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (DocWaresSample DWS : pDWS) {
                long result = -1;

                values.put("type_doc", DWS.TypeDoc);
                values.put("number_doc", DWS.NumberDoc);
                values.put("order_doc", DWS.OrderDoc);
                values.put("code_wares", DWS.CodeWares);
                values.put("quantity", DWS.Quantity);
                values.put("quantity_min", DWS.QuantityMin);
                values.put("quantity_max", DWS.QuantityMax);
                result = db.replace("DOC_WARES_sample", null, values);

                if (i >= 1000) {
                    i = 0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "SaveDOC_WARES_sample=>" + e.toString());
        } finally {
            db.endTransaction();
        }
        return true;
    }


    //Завантаження Списку складів (HTTP)
    public Warehouse[] LoadWarehouse() {

        HttpResult res;
        try {

            res = Http.HTTPRequest(1, "StoreSettings", null, "application/json;charset=utf-8", config.Login, config.Password);

            if (res.HttpState == eStateHTTP.HTTP_OK) {
                InputWarehouse[] data = new Gson().fromJson(res.Result, InputWarehouse[].class);
                Warehouse[] WH = new Warehouse[data.length];
                for (int i = 0; i < WH.length; i++) {
                    WH[i] = data[i].GetWarehouse();
                }
                return WH;
            }

        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "LoadWarehouse=>" + e.getMessage());
        }
        return null;
    }

    public Result SendLogPrice(List<LogPrice> pList) {
        StringBuilder sb = new StringBuilder();
        for (LogPrice s : pList) {
            if (s.IsGoodBarCode())
                sb.append("," + s.GetJsonSE());
        }
        if (sb.length() <= 2)
            return new Result(-1, "Недостатньо даних");
        String a = "[" + sb.substring(1) + "]";

        String data = a;

        HttpResult res = Http.HTTPRequest(0, "pricetag", data, "application/json;charset=utf-8", config.Login, config.Password);
        return new Result(res);
    }

    // Друк на стаціонарному термопринтері
    public String printHTTP(List<String> codeWares) {
        return null;
    }

    // Розбір штрихкоду.
    public ParseBarCode ParsedBarCode(String pBarCode,boolean pIsOnlyBarCode) {
        ParseBarCode res = new ParseBarCode();
        if(pBarCode==null)
            return res;
        pBarCode=pBarCode.trim();
        res.BarCode=pBarCode;
        res.IsOnlyBarCode=pIsOnlyBarCode;

        if (!pIsOnlyBarCode && pBarCode.length() <= 8 && !pBarCode.equals("")) {
            try{
                res.Code=Integer.parseInt(pBarCode);
                res.BarCode=null;
            }catch(Exception e)
            {
                Utils.WriteLog("e",TAG,"ParsedBarCode=> "+ pBarCode+" "+e.getMessage());
            }
        }

        if (config.Company == eCompany.Sim23 && pBarCode!=null) {
            if (pBarCode.substring(0, 2).equals("29") && pBarCode.length() == 13) {
                try {
                    res.Code = Integer.parseInt(pBarCode.substring(2, 8));
                    res.Price = Double.valueOf(pBarCode.substring(8, 13)) / 100d;
                    res.IsOnlyBarCode=false; // Для варіанту коли у виробника штрихкод починається з 29 так як і у цінника.
                    //res.BarCode=null;
                } catch (Exception e) {
                    Log.e("ParsedBarCode", e.getMessage());

                }
            }
        }
        return res;
    }
}

class OutputDocWares
{
    public String CodeWares;
    public Double Quantity;
    public int Reason;
    public OutputDocWares(int pCodeWares, Double pQuantity,int pReason)
    {
        CodeWares= Integer.toString(pCodeWares);
        Quantity=pQuantity;
        Reason=pReason;
    }
}
class OutputDoc
{
    public int TypeDoc;
    public String NumberDoc;
    public String DateDoc;
    public String DateOutInvoice; // YYYY-MM-DD
    public String NumberOutInvoice;
    public int  IsClose;
    List<OutputDocWares> DocWares;
    public OutputDoc(){};
    public OutputDoc(int pTypeDoc, String pNumberDoc,String pDateDoc)
    {
        TypeDoc=pTypeDoc;  NumberDoc= pNumberDoc; DateDoc =pDateDoc;
        DocWares = new ArrayList<>();
    }
    public OutputDoc(int pTypeDoc, String pNumberDoc,String pDateDoc,String pDateOutInvoice,String pNumberOutInvoice,int pIsClose)
    {
        this(pTypeDoc,pNumberDoc,pDateDoc);
        NumberOutInvoice=pNumberOutInvoice;
        DateOutInvoice=pDateOutInvoice;
        IsClose=pIsClose;
    }
}

 class InputDocs {
    Doc[]  Doc;
    DocWaresSample [] DocWaresSample;
}

 class InputWarehouse {
    public int Code;
    public String StoreCode; //Number
    public String Name; //Url
    public String Unit; //Name
    public String InternalIP;
    public String ExternalIP;
    Warehouse GetWarehouse()
    {
        return new Warehouse(Code,StoreCode,Unit,Name,InternalIP,ExternalIP);
    }
}

