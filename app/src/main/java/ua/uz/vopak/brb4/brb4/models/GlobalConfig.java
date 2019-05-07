package ua.uz.vopak.brb4.brb4.models;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerTC20;
import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.helpers.*;

public class GlobalConfig {
    private static GlobalConfig Instance = null;
    public static String CodeWarehouse = "0";
    public static String ApiUrl = "http://znp.vopak.local/api/api_v1_utf8.php";
    public static String Login = "c";
    public static String Password = "c";
    public static Worker Worker;
    public static String SN = Build.SERIAL;
    public static String NameDCT = Build.USER;
    public static SQLiteAdapter SQLiteAdapter;
    public static eTypeScaner TypeScaner = eTypeScaner.NotDefine;
    public static ua.uz.vopak.brb4.brb4.Scaner.Scaner Scaner;
    public static Context varApplicationContext;
    public static boolean isAutorized;
    public static Integer NumberPackege = 0;
    public static View BarcodeImageLayout;
    public static Integer connectionPrinterType;
    public static boolean yellowAutoPrint;
    public static String GetApiJson(int parCodeData, String parData) {
        return "{\"CodeData\":"+ Integer.toString(parCodeData) + ",\"SerialNumber\":\""+SN+"\",\"NameDCT\":\""+NameDCT+"\", \"Warehouse\":\""+getCodeWarehouse()+"\", \"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"" +
                (parData==null?"":","+parData )+"}";
    }

    public static String getCodeWarehouse() {
        String code = "000000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    protected GlobalConfig() {
//        String NameDCT = Build.ID;
    }

    public static void Init(Context parApplicationContext)
    {
        varApplicationContext=parApplicationContext;
        //Визначаємо тип Сканера
        TypeScaner=GetTypeScaner(varApplicationContext);
        //SQLite
        GetSQLiteAdapter(varApplicationContext);
        //Worker
        GetWorker();

        new AsyncHelper(new IAsyncHelper() {
            @Override
            public void Invoke() {
                String printerConnectionType = Worker.GetConfigPair("connectionPrinterType");

                if(printerConnectionType.equals("")){
                    connectionPrinterType = 0;
                }else{
                    connectionPrinterType = Integer.parseInt(printerConnectionType);
                }

                String parYellowAutoPrint = Worker.GetConfigPair("yellowAutoPrint");

                if(parYellowAutoPrint.equals("")){
                    yellowAutoPrint = false;
                }else{
                    yellowAutoPrint = Boolean.parseBoolean(parYellowAutoPrint);
                }
            }
        }).execute();
    }

    public static Scaner GetScaner() {

        if(Scaner!=null)
            return Scaner;

        switch (TypeScaner)
        {
            case PM550:
                Scaner = new ScanerPM500(varApplicationContext);
                break;
            case ZebraTC20:
                Scaner = new ScanerTC20(varApplicationContext);
                break;
            case Camera:
            default:
                Scaner = new Scaner(varApplicationContext);
                break;

        }

        return Scaner;
    }

    public static GlobalConfig instance() {
        if (Instance == null) {
            Instance = new GlobalConfig();
        }

        return Instance;
    }

    public static Worker GetWorker(ProgressBar varProgressBar) {
        Worker = GetWorker();
        if (Worker != null)
            Worker.SetProgressBar(varProgressBar);
        return Worker;
    }

    public static Worker GetWorker() {
        if (Worker == null) {
            Worker = new Worker();
        }
        return Worker;
    }

    public static SQLiteAdapter GetSQLiteAdapter(Context c) {
        if (SQLiteAdapter == null) {
            SQLiteAdapter = new SQLiteAdapter(c);
            SQLiteAdapter.createDatabase();
            SQLiteAdapter.open();
        }
        return SQLiteAdapter;
    }

    public static SQLiteAdapter GetSQLiteAdapter() {
        return SQLiteAdapter;
    }

    static eTypeScaner GetTypeScaner(Context parApplicationContext) {
        if(parApplicationContext==null)
            return eTypeScaner.None;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        if (model.equals("TC20") && (manufacturer.contains("Zebra Technologies") || manufacturer.contains("Motorola Solutions")))
            return eTypeScaner.ZebraTC20;
        if (model.equals("PM550") && manufacturer.contains("Point Mobile Co., Ltd."))
            return eTypeScaner.PM550;

        return eTypeScaner.Camera;

    }
}
