package ua.uz.vopak.brb4.brb4.models;

import android.content.Context;

import com.journeyapps.barcodescanner.camera.CameraSettings;

import ua.uz.vopak.brb4.brb4.Scaner.BitaHC61;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerPM351;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500;
//import ua.uz.vopak.brb4.brb4.Scaner.ScanerTC20;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerZebra;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.lib.enums.eRole;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class Config extends AbstractConfig {

    public static final int DB_VERSION = 2;

    public static boolean IsLoadStartData = false;
    public static boolean IsTest = false;
    public static boolean IsAutoLogin = false;
    public static boolean IsLoginCO = false;
    public static eRole  Role = eRole.NotDefined;
    public static int IdCamera=0;
    public DocSetting[] DocsSetting;
    public Warehouse[] Warehouses;

    public Worker Worker;
    public SQLiteAdapter SQLiteAdapter;

    public ua.uz.vopak.brb4.brb4.Scaner.Scaner Scaner;

    public boolean yellowAutoPrint;

    public Reason Reasons[];

    protected Config() {
        super();//
    }//

    public static Config instance() {
        if (Instance == null || !(Instance instanceof Config)){
            Instance = new Config();
        }
        return (Config) Instance;
    }

    public DocSetting GetDocSetting(int pDocumentType) {
        if (DocsSetting == null)
            return null;
        for (int ind = 0; ind < DocsSetting.length; ind++)
            if (DocsSetting[ind].TypeDoc == pDocumentType)
                return DocsSetting[ind];
        return null;
    }

    public Warehouse GetWarehouse(int pCodeWarehouse) {
        if (Warehouses == null)
            Warehouses=GetWorker().GetWarehouse();
        if (Warehouses == null) return null;

        for (int ind = 0; ind < Warehouses.length; ind++)
            if (Warehouses[ind].Code == pCodeWarehouse)
                return Warehouses[ind];
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
            case PM351:
                Scaner = new ScanerPM351(context);
            case BitaHC61:
                Scaner = new BitaHC61(context);
            case NotDefine:
                break;
            default:
            case Camera:
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
    public boolean IsUseCamera()  {return TypeScaner== eTypeScaner.Camera;}

    public CameraSettings GetCameraSettings() {
        CameraSettings settings = new CameraSettings();
        settings.setRequestedCameraId(IdCamera);
        settings.setFocusMode(CameraSettings.FocusMode.MACRO);

        return settings;
    }


}
