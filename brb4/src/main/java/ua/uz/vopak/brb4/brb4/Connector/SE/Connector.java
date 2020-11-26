package ua.uz.vopak.brb4.brb4.Connector.SE;

import android.content.ContentValues;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.LogPrice;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.models.HttpResult;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector extends  ua.uz.vopak.brb4.brb4.Connector.Connector {

    protected static final String TAG = "BRB4/Connector";

    //Завантаження довідників.
    public boolean LoadGuidData(boolean IsFull, ObservableInt pProgress) {
        try {
            Log.d(TAG, "Start");
            pProgress.set(5);
            HttpResult res = Http.HTTPRequest(0, "nomenclature", null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                Log.d(TAG, "LoadData=>" + res.Result.length());
                pProgress.set(40);
                if (IsFull) {
                    String sql = "DELETE FROM Wares; DELETE FROM ADDITION_UNIT; DELETE FROM BAR_CODE;DELETE FROM UNIT_DIMENSION;";
                    db.execSQL(sql.trim());
                }
                Log.d(TAG, "DELETE");
                pProgress.set(45);
                InputData data = new Gson().fromJson(res.Result, InputData.class);

                Log.d(TAG, "Parse JSON");
                pProgress.set(60);
                SaveWares(data.Nomenclature);
                Log.d(TAG, "Nomenclature");
                pProgress.set(70);
                SaveAdditionUnit(data.Units);
                Log.d(TAG, "Units");
                pProgress.set(80);
                SaveBarCode(data.Barcodes);
                Log.d(TAG, "Barcodes");
                pProgress.set(90);
                SaveUnitDimension(data.Dimentions);
            } else
                Log.d(TAG, res.HttpState.name());

            res = Http.HTTPRequest(1, "reasons", null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                pProgress.set(95);
                List<Reason> Reasons = new Gson().fromJson(res.Result, new TypeToken<List<Reason>>() {
                }.getType());
                db.execSQL("DELETE FROM Reason;");
                SaveReason(Reasons);
            }
            pProgress.set(100);
            Log.d(TAG, "End");
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast toast = Toast.makeText(GlobalConfig.instance().context, "Помилка завантаження довідників=>" + e.getMessage(), Toast.LENGTH_LONG);
            toast.show();
        }
        return false;
    }

    boolean SaveReason(List<Reason> pReasons) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Reason R : pReasons) {
                values.put("CODE_REASON", R.code);
                values.put("NAME_REASON", R.reason);
                db.replace("Reason", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "SaveReason=>" + ex.toString());
        } finally {
            db.endTransaction();
        }
        return true;

    }

    boolean SaveWares(Nomenclature[] pW) {
        int i = 0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (Nomenclature wares : pW) {
                values.put("CODE_WARES", wares.CODE_WARES);
                values.put("NAME_WARES", wares.NAME_WARES);
                values.put("ARTICL", wares.ARTICL);
                values.put("CODE_UNIT", wares.CODE_UNIT);
                values.put("VAT", wares.VAT);
                values.put("DESCRIPTION", wares.DESCRIPTION);
                values.put("CODE_GROUP", wares.CODE_GROUP);
                values.put("VAT_OPERATION", wares.VAT_OPERATION);
                db.replace("Wares", null, values);
                if (i >= 1000) {
                    i = 0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "SaveWares=>" + ex.toString());
        } finally {
            db.endTransaction();
        }
        return true;
    }

    boolean SaveAdditionUnit(Units[] pUnits) {
        int i = 0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (Units Units : pUnits) {
                values.put("CODE_WARES", Units.CODE_WARES);
                values.put("CODE_UNIT", Units.CODE_UNIT);
                values.put("COEFFICIENT", Units.COEF_WARES);
                db.replace("ADDITION_UNIT", null, values);
                if (i >= 1000) {
                    i = 0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "SaveAdditionUnit=>" + ex.toString());
        } finally {
            db.endTransaction();
        }
        return true;
    }

    boolean SaveBarCode(Barcode[] pBarCode) {
        int i = 0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (Barcode BarCode : pBarCode) {
                values.put("CODE_WARES", BarCode.CODE_WARES);
                values.put("CODE_UNIT", BarCode.CODE_UNIT);
                values.put("BAR_CODE", BarCode.BAR_CODE);
                db.replace("BAR_CODE", null, values);
                if (i >= 1000) {
                    i = 0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "SaveBarCode=>" + ex.toString());
        } finally {
            db.endTransaction();
        }
        return true;
    }

    boolean SaveUnitDimension(UnitDimension[] pUD) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (UnitDimension UD : pUD) {

                values.put("CODE_UNIT", UD.CODE_UNIT);
                values.put("NAME_UNIT", UD.NAME_UNIT);
                values.put("ABR_UNIT", UD.ABR_UNIT);
                values.put("DESCRIPTION", UD.DESCRIPTION_TEXT);
                db.replace("UNIT_DIMENSION", null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            Log.e(TAG, "SaveUnitDimension=>" + ex.toString());
        } finally {
            db.endTransaction();
        }
        return true;
    }

    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear) {
        if (pProgress != null)
            pProgress.set(5);
        HttpResult res;
        try {
            if (pTypeDoc == 5|| pTypeDoc == 6 ) {
                res = Http.HTTPRequest(1, "documents"+(pTypeDoc == 5?"\\" + pNumberDoc: "?StoreSetting="+config.CodeWarehouse ), null, "application/json;charset=utf-8", config.Login, config.Password);
            } else
                res = Http.HTTPRequest(0, "documents" , null, "application/json;charset=utf-8", config.Login, config.Password);
            if (res.HttpState == eStateHTTP.HTTP_OK) {
                if (pProgress != null)
                    pProgress.set(40);
                InputDocs data = new Gson().fromJson(res.Result, InputDocs.class);
                if (pIsClear)
                    db.execSQL("Delete from DOC;Delete from DOC_WARES_sample;Delete from DOC_WARES;".trim());
                else
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

        } catch (Exception ex) {
            Log.e(TAG, "LoadDocsData=>" + ex.getMessage());
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

        HttpResult res = Http.HTTPRequest((pTypeDoc == 5 || pTypeDoc == 6  ? 1 : 0) , "documentin", json, "application/json;charset=utf-8", config.Login, config.Password);
        if (res.HttpState == eStateHTTP.HTTP_OK) {
            return new Result();
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
        } catch (Exception ex) {
            Log.e(TAG, "SaveDOC_WARES_sample=>" + ex.toString());
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
                Warehouse [] WH = new Warehouse[data.length];
                for (int i = 0; i <WH.length ; i++) {
                    WH[i]=data[i].GetWarehouse();
                }
                return WH;
            }

        } catch (Exception ex) {
            Log.e(TAG, "LoadWarehouse=>" + ex.getMessage());
        }
        return null;
    }

    public  Result  SendLogPrice(List<LogPrice> pList){
        StringBuilder sb = new StringBuilder();
        for (LogPrice s : pList) {
            if(s.IsGoodBarCode())
                sb.append("," + s.GetJsonSE());
        }
        if (sb.length() <= 2)
            return new Result(-1,"Недостатньо даних");
        String a = "[" + sb.substring(1) + "]";

        String data = a;

        HttpResult res = Http.HTTPRequest(0, "pricetag", data, "application/json;charset=utf-8", config.Login, config.Password);
        return new Result(res);
    }

    // Друк на стаціонарному термопринтері
    public void printHTTP(List<String> codeWares) {};

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

