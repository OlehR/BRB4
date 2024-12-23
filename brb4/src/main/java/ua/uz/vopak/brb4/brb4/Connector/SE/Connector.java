package ua.uz.vopak.brb4.brb4.Connector.SE;

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
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.enums.eTypeControlDoc;
import ua.uz.vopak.brb4.lib.enums.eTypeCreate;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.HttpResult;
import ua.uz.vopak.brb4.lib.models.ParseBarCode;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector.SE";
    public DocSetting[] GenSettingDocs(eCompany pCompany, eRole pProfile) {
        DocSetting[] Setting = null;
        boolean[] Right;
        switch (pProfile) {
            case Admin:
                Right = new boolean[]{true, true, true, true, true, true, true};
                Setting = new DocSetting[5];
                break;
            case User:
                Right = new boolean[]{true, true, true, false, false, true, false};
                Setting = new DocSetting[4];
                break;
            case Auditor:
                Right = new boolean[]{false, true, false, true, true, true, true};
                Setting = new DocSetting[5];
                break;
            case UserCO:
                Right = new boolean[]{true, true, true, false, true, true, true};
                Setting = new DocSetting[6];
                break;
            default:
                Right = new boolean[]{false, false, false, false, false, false, false};
                break;
        }
        int step = 0;
        if (Right[0])
            Setting[step++] = new DocSetting(2, "Мініревізія", eTypeControlDoc.Ask, false, false, false, false, false, 1, 1, 0, false, true, false, true, false, 0, eTypeCreate.Simple, false);
        if (Right[1])
            Setting[step++] = new DocSetting(5, "Перевірка Лотів з ЛЦ", eTypeControlDoc.Ask, true, true, true, true, true, 2, 2, 0, false, true, true, false, false, 1, eTypeCreate.None, false);
        if (Right[2])
            Setting[step++] = new DocSetting(1, "Прихід", eTypeControlDoc.Control, false, false, false, true, true, 1, 1, 3, true, true, true, false, false, 0, eTypeCreate.None, false);
        if (Right[3])
            Setting[step++] = new DocSetting(6, "Ревізія", eTypeControlDoc.Ask, true, false, true, false, false, 1, 1, 1, false, false, true, false, false, 1, eTypeCreate.None, true);
        if (Right[4])
            Setting[step++] = new DocSetting(7, "Ревізія ОЗ", eTypeControlDoc.Ask, true, false, false, false, false, 1, 6, 0, false, false, true, false, true, 2, eTypeCreate.None, false);
        if (Right[5])
            Setting[step++] = new DocSetting(8, "Переміщення ОЗ Вих", eTypeControlDoc.Ask, true, false, false, false, false, 1, 6, 0, false, true, true, false, true, 2, eTypeCreate.WithWarehouseTo, false);
        if (Right[6])
            Setting[step++] = new DocSetting(9, "Переміщення ОЗ Вх", eTypeControlDoc.Ask, true, false, false, false, false, 1, 6, 0, false, true, true, false, true, 2, eTypeCreate.None, false);
        return Setting;
    }
    public Result Login(final String pLogin, final String pPassWord, final boolean pIsLoginCO) {
        HttpResult res = Http.HTTPRequest(pIsLoginCO ? 1 : 0, "login", "{\"login\" : \"" + pLogin + "\"}", "application/json;charset=utf-8", pLogin, pPassWord);
        if (res.HttpState == eStateHTTP.HTTP_UNAUTHORIZED || res.HttpState == eStateHTTP.HTTP_Not_Define_Error) {
            Utils.WriteLog("e", TAG, "Login >>" + res.HttpState);
            return new Result(-1, res.HttpState.toString(), "Неправильний логін або пароль");
        } else if (res.HttpState != eStateHTTP.HTTP_OK)
            return new Result(res, "Ви не підключені до мережі " + config.Company.name());
        else {
            try {
                JSONObject jObject = new JSONObject(res.Result);
                //Якщо конектимся локально пробуємо до ЦБ
                if (jObject.getInt("State") == 0) {
                    Config.Role = eRole.fromOrdinal(jObject.getInt("Profile"));

                    if(!pIsLoginCO) {
                        res = Http.HTTPRequest(pIsLoginCO ? 1 : 0, "login", "{\"login\" : \"" + pLogin + "\"}", "application/json;charset=utf-8", pLogin, pPassWord);
                        if (res.HttpState == eStateHTTP.HTTP_OK) {
                            try {
                                
                                jObject = new JSONObject(res.Result);

                                if (jObject.getInt("State") == 0) {
                                    eRole vRole = eRole.fromOrdinal(jObject.getInt("Profile"));

                                    if (vRole == eRole.User) config.Role = eRole.UserCO;
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    return new Result();
                } else
                    return new Result(jObject.getInt("State"), jObject.getString("TextError"), "Неправильний логін або пароль");

            } catch (Exception e) {
                Utils.WriteLog("e", TAG, "Login=>", e);
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
                    db.execSQL("DELETE FROM GroupWares");
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
                Log.d(TAG, "GroupWares");
                pProgress.set(95);
                if (data.Parents != null && data.Parents.length > 0)
                    for (GroupWares el : data.Parents)
                        if (el.CodeGroup == 26 || el.CodeGroup == 47) el.IsAlcohol = true;
                mDbHelper.SaveGroupWares(data.Parents);
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
            Utils.WriteLog("e", TAG, "LoadGuidData=>", e);
//            Toast toast = Toast.makeText(Config.instance().context, "Помилка завантаження довідників=>" + e.getMessage(), Toast.LENGTH_LONG);
 //           toast.show();
        }
        return false;
    }


    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {
        DocSetting ds = config.GetDocSetting(pTypeDoc);
        String CodeWarehouse = String.valueOf(config.CodeWarehouse);

        int CodeApi = 0;
        if (ds != null)
            CodeApi = ds.CodeApi;
        else if (pTypeDoc <= 0 && config.IsLoginCO) CodeApi = 1;

        if (pTypeDoc >= 7 && pTypeDoc <= 9) {
            Warehouse Wh = config.GetWarehouse(config.CodeWarehouse);
            if (Wh != null)
                CodeWarehouse = Wh.Number;
        }

        String NameApi = "documents"+ (pTypeDoc>0&&pTypeDoc<5 ? "" :  "?StoreSetting=" + CodeWarehouse);
        if(pTypeDoc == 5)
            NameApi = (pNumberDoc==null || pNumberDoc.isEmpty())? "DocumentsCurrentLot?StoreSetting=" + CodeWarehouse : "documents\\" + pNumberDoc;

        if (pTypeDoc >= 8 && pTypeDoc <= 9) {
            NameApi = "docmoveoz?StoreSetting=" + CodeWarehouse+"&TypeMove=" + (pTypeDoc == 8 ? "0" : "1");
        }

        if (pTypeDoc == -1)
            LoadGuidData(true, pProgress);

        if (pProgress != null) pProgress.set(5);
        HttpResult res;
        try {
            //if ((pTypeDoc >= 5 && pTypeDoc <= 9) || (pTypeDoc <= 0 && config.IsLoginCO) ) {
                //if(pTypeDoc == 5  && (pNumberDoc==null || pNumberDoc.isEmpty())) return false;
                //NameApi + (pTypeDoc == 5 ? "\\" + pNumberDoc : "?StoreSetting=" + CodeWarehouse) + AddPar
                res = Http.HTTPRequest(CodeApi, NameApi, null, "application/json;charset=utf-8", config.Login, config.Password);
            //} else
             //   res = Http.HTTPRequest(CodeApi, "documents", null, "application/json;charset=utf-8", config.Login, config.Password);

            if (res.HttpState == eStateHTTP.HTTP_OK) {
                if (pProgress != null)
                    pProgress.set(40);
                InputDocs data = new Gson().fromJson(res.Result, InputDocs.class);
                data.set();
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
                    v.TypeDoc += (pTypeDoc == 9 ? 1 : 0);
                    mDbHelper.SaveDocs(v);
                }
                if (pProgress != null)
                    pProgress.set(60);
                SaveDocWaresSample(data.DocWaresSample, (pTypeDoc == 9 ? 1 : 0));
                if (pProgress != null)
                    pProgress.set(100);
                return true;
            }
        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "LoadDocsData=>", e);
        }
        return false;
    }

    //Вивантаження документів з ТЗД (HTTP)
    public Result SyncDocsData(int pTypeDoc, String pNumberDoc, List<WaresItemModel> pWares, Date pDateOutInvoice, String pNumberOutInvoice, int pIsClose) {
        Gson gson = new Gson();
        DocSetting ds = config.GetDocSetting(pTypeDoc);
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

        int CodeApi = 0;
        if (ds != null)
            CodeApi = ds.CodeApi;

        HttpResult res = Http.HTTPRequest(CodeApi, "documentin", json, "application/json;charset=utf-8", config.Login, config.Password);
        if (res.HttpState == eStateHTTP.HTTP_OK) {
            return new Result(res);
        }
        return new Result(-1, res.HttpState.toString());
    }

    //Завантаження Списку складів (HTTP)
    public Warehouse[] LoadWarehouse() {

        HttpResult res;
        try {

            res = Http.HTTPRequest(1, "StoreSettings", null, "application/json;charset=utf-8", config.Login, config.Password);

            if (res.HttpState == eStateHTTP.HTTP_OK) {
                InputWarehouse[] data = new Gson().fromJson(res.Result, InputWarehouse[].class);
                Warehouse[] WH_UTP = LoadWarehouseAdd();
                Warehouse[] WH = new Warehouse[data.length + (WH_UTP == null ? 0 : WH_UTP.length)];
                int i;
                for (i = 0; i < data.length; i++) WH[i] = data[i].GetWarehouse();
                if (WH_UTP != null)
                    for (i = 0; i < WH_UTP.length; i++) {
                        WH_UTP[i].Code += 999000000;
                        WH[i + data.length] = WH_UTP[i];
                    }
                return WH;
            }
        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "LoadWarehouse=>", e);
        }
        return null;
    }

    public Warehouse[] LoadWarehouseAdd(){
        return new Warehouse[0];
       /* HttpResult res;
        try {

            res = Http.HTTPRequest(2, "storeUTP", null, "application/json;charset=utf-8", config.Login, config.Password);

            if (res.HttpState == eStateHTTP.HTTP_OK) {
                InputWarehouse[] data = new Gson().fromJson(res.Result, InputWarehouse[].class);
                Warehouse[] WH = new Warehouse[data.length];
                for (int i = 0; i < WH.length; i++) {
                    WH[i] = data[i].GetWarehouse();
                }
                return WH;
            }

        } catch (Exception e) {
            Utils.WriteLog("e", TAG, "LoadWarehouse=>", e);
        }
        return null;*/
    }

    public Result SendLogPrice(List<LogPrice> pList) {
        StringBuilder sb = new StringBuilder();
        for (LogPrice s : pList) {
            if (s.IsGoodBarCode())
                sb.append(",").append(s.GetJsonSE());
        }
        if (sb.length() <= 2)
            return new Result(-1, "Недостатньо даних");

        String data = "[" + sb.substring(1) + "]";

        HttpResult res = Http.HTTPRequest(0, "pricetag", data, "application/json;charset=utf-8", config.Login, config.Password);
        return new Result(res);
    }

    // Друк на стаціонарному термопринтері
    public String printHTTP(List<String> codeWares) {
        return null;
    }

    // Розбір штрихкоду.
    public ParseBarCode ParsedBarCode(String pBarCode, boolean pIsOnlyBarCode) {
        ParseBarCode res = new ParseBarCode();
        if (pBarCode == null)
            return res;
        pBarCode = pBarCode.trim();
        res.BarCode = pBarCode;
        res.IsOnlyBarCode = pIsOnlyBarCode;

        if (!pIsOnlyBarCode && pBarCode.length() <= 8 && !pBarCode.equals("")) {
            try {
                res.Code = Integer.parseInt(pBarCode);
                res.BarCode = null;
            } catch (Exception e) {
                Utils.WriteLog("e", TAG, "ParsedBarCode=> " + pBarCode, e);
            }
        }

        if (pBarCode != null) {
            if (pBarCode.startsWith("29") && pBarCode.length() == 13) {
                try {
                    res.Code = Integer.parseInt(pBarCode.substring(2, 8));
                    res.Price = Double.parseDouble(pBarCode.substring(8, 13)) / 100d;
                    res.IsOnlyBarCode = false; // Для варіанту коли у виробника штрихкод починається з 29 так як і у цінника.
                    //res.BarCode=null;
                } catch (Exception e) {
                    Utils.WriteLog("e", TAG, "ParsedBarCode", e);

                }
            } else if (pBarCode.contains("$")) {
                res.Code = Integer.parseInt(pBarCode.substring(0, pBarCode.indexOf('$')));
                res.Quantity=1;
            }
        }
        return res;
    }

    public Result CreateNewDoc(int pTypeDoc, int pCodeWarehouseFrom, int pCodeWarehouseTo) {
        DocSetting ds = config.GetDocSetting(pTypeDoc);
        //String CodeWarehouse= String.valueOf(config.CodeWarehouse);
        int CodeApi = 0;
        if (ds != null) CodeApi = ds.CodeApi;
        String Data=null;
        String NameApi = null;
        if (pTypeDoc == 8) {
            NameApi = "newmovedoc?StoreSetting=" + pCodeWarehouseFrom + "&StoreSettingTO=" + pCodeWarehouseTo;
        }
        if (pTypeDoc == 2) {
            NameApi = "newmovedoc";
            Data="{\"TypeDoc\":"+pTypeDoc+"}";
        }

        HttpResult res;
        try {
            if (pTypeDoc <= 2|| (pTypeDoc >= 5 && pTypeDoc <= 9) || (pTypeDoc <= 0 && config.IsLoginCO)) {
                res = Http.HTTPRequest(CodeApi, NameApi, Data, "application/json;charset=utf-8", config.Login, config.Password);
                Result Res = new Gson().fromJson(res.Result, Result.class);
                return Res;
            }
        } catch (Exception e) {
            return new Result(-1, e.getMessage());
        }
        return new Result(-1, "Документ не створено");
    }

    public WaresItemModel GetWares(int pCodeWares, boolean pIsSimpleDoc) {
        String NameApi = "";
        if (pIsSimpleDoc)
            NameApi = "oz?CodeWares=" + pCodeWares;

        WaresItemModel Res=null;
        try {
            HttpResult res = Http.HTTPRequest(2, NameApi, null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                JSONObject jObject = new JSONObject(res.Result);
                Res= new WaresItemModel();
                Res.NameWares=jObject.getString("NAME");
                Res.CodeWares=pCodeWares;
            }
        } catch (
                Exception e) {
            //return new Result(-1,e.getMessage());
        }
        return Res; //new Result(-1,"Документ не створено");
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
    public int TypeMove=0;
    List<OutputDocWares> DocWares;
    public OutputDoc(){}
    public OutputDoc(int pTypeDoc, String pNumberDoc,String pDateDoc)
    {
        TypeDoc=pTypeDoc;  NumberDoc= pNumberDoc; DateDoc =pDateDoc;

        if(TypeDoc==9)
        {
            TypeMove=1;
            TypeDoc=8;
        }
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
    DocWaresSample2[] DocWaresSample;
     public void set()
     {
         for (DocWaresSample2 el:DocWaresSample) {el.set(); }
     }
}
class DocWaresSample2 extends DocWaresSample {
    //public int INUMBER;
   // public String NAME;

    public void set() {
        //CodeWares=INUMBER;
  //      if (NAME != null && NAME.length() > 0)
   //         Name = NAME;
    }
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

