package ua.uz.vopak.brb4.brb4.Connector.SE;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.Tag;
import android.util.Log;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.SQLiteAdapter;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.Result;

public class Connector {

    protected static final String TAG = "BRB4/Connector";
    GlobalConfig config = GlobalConfig.instance();
    SQLiteAdapter mDbHelper  = config.GetSQLiteAdapter();
    SQLiteDatabase db=mDbHelper.GetDB();
    GetDataHTTP Http = new GetDataHTTP();
    //Завантаження довідників.
    public void LoadGuidData(boolean IsFull, ObservableInt pProgress)    {
        Log.d(TAG, "Start");
        pProgress.set(5);
        String res = Http.HTTPRequest(config.ApiUrl+"nomenclature", null, "application/json;charset=utf-8", config.Login, config.Password);
        if (Http.HttpState == eStateHTTP.HTTP_OK) {
            Log.d(TAG, "LoadData=>"+res.length());
            pProgress.set(40);
            if(IsFull)
            {
                String sql="DELETE FROM Wares; DELETE FROM ADDITION_UNIT; DELETE FROM  BAR_CODE;DELETE FROM UNIT_DIMENSION;";
                db.execSQL(sql.trim());
            }
            Log.d(TAG, "DELETE");
            pProgress.set(45);
            InputData data = new Gson().fromJson(res, InputData.class);

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
        }
        else
            Log.d(TAG,  Http.HttpState.name());

        res = Http.HTTPRequest(config.ApiURLadd+"reasons", null, "application/json;charset=utf-8", config.Login, config.Password);
        if (Http.HttpState == eStateHTTP.HTTP_OK) {
            pProgress.set(95);
            List<Reason> Reasons = new Gson().fromJson(res, new TypeToken<List<Reason>>(){}.getType());
            SaveReason(Reasons);
        }
        pProgress.set(100);
        Log.d(TAG, "End");
    }

    boolean SaveReason(List<Reason> pReasons)
    {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            for (Reason R : pReasons) {
                values.put("CODE_REASON", R.code);
                values.put("NAME_REASON", R.reason);
                db.replace("Reason", null, values);
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.e(TAG,"SaveReason=>"+ ex.toString());
        }
        finally {
            db.endTransaction();
        }
        return true;

    }

    boolean SaveWares(Nomenclature[] pW)    {
        int i=0;
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
                if(i>=1000) {
                i=0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.e(TAG,"SaveWares=>"+ ex.toString());
        }
        finally {
            db.endTransaction();
        }
        return true;
    }

    boolean SaveAdditionUnit(Units[] pUnits)    {
        int i=0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (Units Units : pUnits) {
                values.put("CODE_WARES", Units.CODE_WARES);
                values.put("CODE_UNIT", Units.CODE_UNIT);
                values.put("COEFFICIENT", Units.COEF_WARES);
                db.replace("ADDITION_UNIT", null, values);
                if(i>=1000) {
                    i=0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.e(TAG,"SaveAdditionUnit=>"+ ex.toString());
        }
        finally {
            db.endTransaction();
        }
        return true;
    }

    boolean SaveBarCode(Barcode[] pBarCode)    {
        int i=0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (Barcode BarCode : pBarCode) {
                values.put("CODE_WARES", BarCode.CODE_WARES);
                values.put("CODE_UNIT", BarCode.CODE_UNIT);
                values.put("BAR_CODE", BarCode.BAR_CODE);
                db.replace("BAR_CODE", null, values);
                if(i>=1000) {
                    i=0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.e(TAG,"SaveBarCode=>"+ ex.toString());
        }
        finally {
            db.endTransaction();
        }
        return true;
    }

    boolean SaveUnitDimension(UnitDimension[] pUD)  {
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
        }
        catch (Exception ex)
        {
            Log.e(TAG,"SaveUnitDimension=>"+ ex.toString());
        }
        finally {
            db.endTransaction();
        }
        return true;
    }

    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public Boolean LoadDocsData(int pTypeDoc,String  pNumberDoc,ObservableInt pProgress,boolean pIsClear) {



        if(pProgress!=null)
            pProgress.set(5);
        String res;
        try {
            if (pTypeDoc == 5) {
                res = Http.HTTPRequest(config.ApiURLadd + "documents\\" + pNumberDoc, null, "application/json;charset=utf-8", config.Login, config.Password);
            } else
                res = Http.HTTPRequest(config.ApiUrl + "documents", null, "application/json;charset=utf-8", config.Login, config.Password);
            if (Http.HttpState == eStateHTTP.HTTP_OK) {
                if (pProgress != null)
                    pProgress.set(40);
                InputDocs data = new Gson().fromJson(res, InputDocs.class);
                if (pIsClear)
                    db.execSQL("Delete from DOC;Delete from DOC_WARES_sample;Delete from DOC_WARES".trim());

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

        }
        catch (Exception ex) {
            Log.e(TAG, "LoadDocsData=>"+ex.getMessage());
        }
        return false;
    }

    //Вивантаження документів з ТЗД (HTTP)
    public Result SyncDocsData(int pTypeDoc, String pNumberDoc, List<WaresItemModel> pWares)
    {
        Gson gson = new Gson();
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());

        ArrayList<OutputDoc> OD = new ArrayList<>();
        OutputDoc el = new  OutputDoc(pTypeDoc,pNumberDoc,formatter.format(date));

        for (WaresItemModel W : pWares ) {
            OutputDocWares w = new OutputDocWares(W.CodeWares, W.InputQuantity,W.CodeReason);
            el.DocWares.add(w);
        }
        OD.add(el);
        String json = gson.toJson(OD);

        String res = Http.HTTPRequest( (pTypeDoc==5? config.ApiURLadd :config.ApiUrl)+"documentin", json, "application/json;charset=utf-8", config.Login, config.Password);
        if (Http.HttpState == eStateHTTP.HTTP_OK) {
            return new Result();
        }
        return new Result(-1,Http.HttpState.toString());
    }

    boolean SaveDocWaresSample(DocWaresSample []  pDWS)    {
        int i=0;
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


                if(i>=1000) {
                    i=0;
                    db.setTransactionSuccessful();
                    db.endTransaction();
                    db.beginTransaction();
                }
            }
            db.setTransactionSuccessful();
        }
        catch (Exception ex)
        {
            Log.e(TAG,"SaveDOC_WARES_sample=>"+ ex.toString());
        }
        finally {
            db.endTransaction();
        }
        return true;
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
    List<OutputDocWares> DocWares;
    public OutputDoc(){};
    public OutputDoc(int pTypeDoc, String pNumberDoc,String pDateDoc)
    {
        TypeDoc=pTypeDoc;  NumberDoc= pNumberDoc; DateDoc =pDateDoc;
        DocWares = new ArrayList<>();
    }


}

 class InputDoc {
    Doc  Doc;
    DocWaresSample [] DocWaresSample;
}