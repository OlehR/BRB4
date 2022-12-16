package ua.uz.vopak.brb4.brb4.Connector;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import androidx.databinding.ObservableInt;

import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.LogPrice;
import ua.uz.vopak.brb4.brb4.helpers.SQLiteAdapter;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.brb4.models.DocWaresSample;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.ParseBarCode;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.Result;

public abstract class Connector {
    protected static final String TAG = "BRB4/Connector";
    public abstract Warehouse[] LoadWarehouse();
    private static Connector Instance = null;

    protected static Config config = Config.instance();
    protected SQLiteAdapter mDbHelper = config.GetSQLiteAdapter();
    protected SQLiteDatabase db = mDbHelper.GetDB();
    protected GetDataHTTP Http = GetDataHTTP.instance(); //new GetDataHTTP(new String[]{config.ApiUrl, config.ApiURLadd});

    public static ua.uz.vopak.brb4.brb4.Connector.Connector instance() {
       // if (Instance == null) {
            Instance = (config.Company== eCompany.Sim23 ? new ua.uz.vopak.brb4.brb4.Connector.SE.Connector(): new ua.uz.vopak.brb4.brb4.Connector.PSU.Connector());
      //  }
        return Instance;
    }

    //Логін
    public abstract Result Login(final String pLogin, final String pPassWord,final boolean pIsLoginCO);
    //Завантаження довідників.
    public abstract boolean LoadGuidData(boolean IsFull, ObservableInt pProgress);

    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public abstract Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear);
    //Вивантаження документів з ТЗД (HTTP)
    public abstract Result SyncDocsData(int pTypeDoc, String pNumberDoc, List<WaresItemModel> pWares, Date pDateOutInvoice, String pNumberOutInvoice, int pIsClose) ;

    //Збереження ПРосканованих товарів в 1С
    public abstract Result  SendLogPrice(List<LogPrice> pList) ;

    // Друк на стаціонарному термопринтері
    public abstract String printHTTP(List<String> codeWares);

    // Розбір штрихкоду.
    public abstract ParseBarCode ParsedBarCode(String pBarCode,boolean pIsOnlyBarCode);

    public boolean SaveDocWaresSample(DocWaresSample[] pDWS, int AddTypeDoc) {
        int i = 0;
        db.beginTransaction();
        try {
            i++;
            ContentValues values = new ContentValues();
            for (DocWaresSample DWS : pDWS) {
                long result = -1;

                values.put("type_doc", DWS.TypeDoc+AddTypeDoc);
                values.put("number_doc", DWS.NumberDoc);
                values.put("order_doc", DWS.OrderDoc);
                values.put("code_wares", DWS.CodeWares);
                values.put("quantity", DWS.Quantity);
                values.put("quantity_min", DWS.QuantityMin);
                values.put("quantity_max", DWS.QuantityMax);
                values.put("Name", DWS.Name);
                values.put("BarCode",DWS.BarCode);
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

    public abstract Result CreateNewDoc(int pTypeDoc,int pCodeWarehouseFrom,int pCodeWarehouseTo);

    public abstract WaresItemModel GetWares(int pCodeWares,boolean IsSimpleDoc);
}
