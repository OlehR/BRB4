package ua.uz.vopak.brb4.brb4.models;

import android.content.Context;
import ua.uz.vopak.brb4.brb4.BuildConfig;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerTC20;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerZebra;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class GlobalConfig extends AbstractConfig {
    private static GlobalConfig Instance = null;
    public static boolean IsLoadStartData = false;

    public DocSetting[] DocsSetting;

    //public String ApiUrl ="http://195.16.78.134:7654/api/api_v1_utf8.php";//"http://znp.vopak.local/api/api_v1_utf8.php";
    //private String UrlLocal = "znp.vopak.local";
    //private int PortLocal = 80;
    //private String Url = "195.16.78.134";
    //private int Port = 7654;
    //private String PathApi = "/api/api_v1_utf8.php";

    public Worker Worker;
    public SQLiteAdapter SQLiteAdapter;

    public ua.uz.vopak.brb4.brb4.Scaner.Scaner Scaner;

    public boolean yellowAutoPrint;

    public Reason Reasons[];

    @Override
    public String GetApiJson(int parCodeData, String parData) {
        return "{\"CodeData\":" + parCodeData + ",\"SerialNumber\":\"" + SN + "\",\"NameDCT\":\"" + NameDCT + "\", \"Ver\":\"" + BuildConfig.VERSION_CODE + "\", \"CodeWarehouse\":\"" + this.getCodeWarehouse() + "\", \"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"" +
                (parData == null ? "" : "," + parData) + "}";
    }

    protected GlobalConfig() {
        super();//
    }//

    public static GlobalConfig instance() {
        if (Instance == null) {
            Instance = new GlobalConfig();
        }
        return Instance;
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
                Utils cUtils=new Utils(context);
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


   /* public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber;
    }*/
}
