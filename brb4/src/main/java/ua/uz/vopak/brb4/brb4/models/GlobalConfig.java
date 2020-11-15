package ua.uz.vopak.brb4.brb4.models;

import android.content.Context;
import ua.uz.vopak.brb4.brb4.BuildConfig;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500;
//import ua.uz.vopak.brb4.brb4.Scaner.ScanerTC20;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerZebra;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class GlobalConfig extends AbstractConfig {

    public static boolean IsLoadStartData = false;
    public static boolean IsTest = false;
    public static boolean IsAutoLogin = false;
    public DocSetting[] DocsSetting;

    public Worker Worker;
    public SQLiteAdapter SQLiteAdapter;

    public ua.uz.vopak.brb4.brb4.Scaner.Scaner Scaner;

    public boolean yellowAutoPrint;

    public Reason Reasons[];

    @Override
    public String GetApiJson(int parCodeData, String parData) {
        return "{\"CodeData\":" + parCodeData + ",\"SerialNumber\":\"" + SN + "\",\"NameDCT\":\"" + NameDCT + "\", \"Ver\":\"" + BuildConfig.VERSION_CODE + "\", \"CodeWarehouse\":\"" + this.getCodeWarehouse() + "\", \"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"" +
                (parData == null || parData =="" ? "" : "," + parData) + "}";
    }

    protected GlobalConfig() {
        super();//
    }//

    public static GlobalConfig instance() {
        if (Instance == null || !(Instance instanceof GlobalConfig)){
            Instance = new GlobalConfig();
        }
        return (GlobalConfig) Instance;
    }

    public DocSetting GetDocSetting(int pDocumentType)
    {
    for( int ind = 0; ind<DocsSetting.length;ind++)
            if(DocsSetting[ind].TypeDoc==pDocumentType)
                return  DocsSetting[ind];
            return null;
    }


    public void Init(Context parApplicationContext) {
       super.Init(parApplicationContext);
        //SQLite
        GetSQLiteAdapter(context);
        //Worker
        GetWorker();

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
    //            Utils cUtils=new Utils(context);
  /*              if (cUtils.GetAddressReachable(Url, Port, 1000))
                    ApiUrl = "http://" + Url + ":" + String.valueOf(Port) + PathApi;
                else
                    ApiUrl = "http://" + UrlLocal + ":" + String.valueOf(PortLocal) + PathApi;
*/
                String printerConnectionType = Worker.GetConfigPair("connectionPrinterType");

                if (printerConnectionType.equals("")) {
                    TypeUsePrinter = eTypeUsePrinter.NotDefined;
                } else {
                    TypeUsePrinter = eTypeUsePrinter.fromOrdinal(printerConnectionType);
                }

                String parYellowAutoPrint = Worker.GetConfigPair("yellowAutoPrint");

                if (parYellowAutoPrint.equals("")) {
                    yellowAutoPrint = false;
                } else {
                    yellowAutoPrint = Boolean.parseBoolean(parYellowAutoPrint);
                }

                return null;
            }
        }).execute();
    }

    public void InitScaner(ScanCallBack cCallBack) {
        GetScaner();
        if (Scaner != null)
            Scaner.Init(cCallBack);
    }

    public Scaner GetScaner() {
        if (Scaner != null)
            return Scaner;

        switch (TypeScaner) {
            case PM550:
                Scaner = new ScanerPM500(context);
                break;
            case Zebra:
                Scaner = new ScanerZebra(context);
                break;
            case Camera:
            default:
                Scaner = new Scaner(context);
                break;
        }
        return Scaner;
    }

    public Worker GetWorker() {
        if (Worker == null) {
            Worker = new Worker();
        }
        return Worker;
    }

    public SQLiteAdapter GetSQLiteAdapter(Context c) {
        if (SQLiteAdapter == null) {
            SQLiteAdapter = new SQLiteAdapter(c);
            SQLiteAdapter.createDatabase();
            SQLiteAdapter.open();
        }
        return SQLiteAdapter;
    }

    public SQLiteAdapter GetSQLiteAdapter() {
        return SQLiteAdapter;
    }



}
