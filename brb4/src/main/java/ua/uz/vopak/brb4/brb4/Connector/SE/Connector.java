package ua.uz.vopak.brb4.brb4.Connector.SE;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;

import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.SQLiteAdapter;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;

public class Connector {
    protected static final String TAG = "BRB4/Connector";
    GlobalConfig config = GlobalConfig.instance();
    SQLiteAdapter mDbHelper  = config.GetSQLiteAdapter();
    SQLiteDatabase db=mDbHelper.GetDB();
    GetDataHTTP Http = new GetDataHTTP();
    public void LoadData(boolean IsFull, ObservableInt pProgress)    {
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
        Log.d(TAG, "End");
        pProgress.set(100);
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
    public Boolean LoadDocsData(int pTypeDoc, ObservableInt pProgress) {
        pProgress.set(5);
        String res = Http.HTTPRequest(config.ApiUrl+"documents", null, "application/json;charset=utf-8", config.Login, config.Password);
        if (Http.HttpState == eStateHTTP.HTTP_OK) {
            pProgress.set(40);
            InputDocs data = new Gson().fromJson(res, InputDocs.class);
            for (Doc v : data.Doc) {
                mDbHelper.SaveDocs(v);
            }
            pProgress.set(60);
            for (DocWaresSample v : data.DocWaresSample) {
                mDbHelper.SaveDocWaresSample(v);
            }
            pProgress.set(100);
            return true;
        }
        return false;
    }

    //Вивантаження документів з ТЗД (HTTP)
    public String SyncDocsData(int parTypeDoc, String NumberDoc, List<WaresItemModel> Wares)
    {
        return null;
    }


}
