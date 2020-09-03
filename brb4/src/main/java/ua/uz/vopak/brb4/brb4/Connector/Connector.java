package ua.uz.vopak.brb4.brb4.Connector;

import android.database.sqlite.SQLiteDatabase;

import androidx.databinding.ObservableInt;

import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.SQLiteAdapter;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.Result;

public abstract class Connector {
    public abstract Warehouse[] LoadWarehouse();
    private static Connector Instance = null;

    protected static GlobalConfig config = GlobalConfig.instance();
    protected SQLiteAdapter mDbHelper = config.GetSQLiteAdapter();
    protected SQLiteDatabase db = mDbHelper.GetDB();
    protected GetDataHTTP Http = new GetDataHTTP();

    public static ua.uz.vopak.brb4.brb4.Connector.Connector instance() {
       // if (Instance == null) {
            Instance = (config.Company== eCompany.SevenEleven ? new ua.uz.vopak.brb4.brb4.Connector.SE.Connector(): new ua.uz.vopak.brb4.brb4.Connector.PSU.Connector());
      //  }
        return Instance;
    }

    //Завантаження довідників.
    public abstract void LoadGuidData(boolean IsFull, ObservableInt pProgress);

    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public abstract Boolean LoadDocsData(int pTypeDoc, String pNumberDoc, ObservableInt pProgress, boolean pIsClear);
    //Вивантаження документів з ТЗД (HTTP)
    public abstract Result SyncDocsData(int pTypeDoc, String pNumberDoc, List<WaresItemModel> pWares, Date pDateOutInvoice, String pNumberOutInvoice, int pIsClose) ;


}
