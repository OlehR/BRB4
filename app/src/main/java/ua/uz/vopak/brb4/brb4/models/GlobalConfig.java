package ua.uz.vopak.brb4.brb4.models;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500;
import ua.uz.vopak.brb4.brb4.Scaner.ScanerTC20;
import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;

public class GlobalConfig extends AbstractConfig {
    public Boolean IsDebug=false;
    private static GlobalConfig Instance = null;
    public String CodeWarehouse = "0";
    //public String ApiUrl ="http://195.16.78.134:7654/api/api_v1_utf8.php";//"http://znp.vopak.local/api/api_v1_utf8.php";
    private String UrlLocal="znp.vopak.local";
    private int PortLocal=80;
    private String Url="195.16.78.134";
    private int Port=7654;
    private String PathApi="/api/api_v1_utf8.php";
    public String VerBRB="4.01.03";


    public String Login = "nov";
    public String Password = "123";
    public Worker Worker;
    public String SN = GetSN();//Build.SERIAL;
    public String NameDCT = Build.USER;
    public SQLiteAdapter SQLiteAdapter;
    public eTypeScaner TypeScaner = eTypeScaner.NotDefine;
    public ua.uz.vopak.brb4.brb4.Scaner.Scaner Scaner;
    public Context varApplicationContext;
    public boolean isAutorized;
    public Integer NumberPackege = 0;
    public View BarcodeImageLayout;
    public String[] printerConnectionPath = new String[]{"Без Принтера", "Тільки при вході", "Авто підключення","Стаціонарний з обрізжчиком"};
    public Integer connectionPrinterType;
    public boolean yellowAutoPrint;
    public Integer printType = 0;//Колір чека 0-звичайнийб 1-жовтий
    @Override
    public String GetApiJson(int parCodeData, String parData) {
        return "{\"CodeData\":"+ Integer.toString(parCodeData) + ",\"SerialNumber\":\""+SN+"\",\"NameDCT\":\""+NameDCT+"\", \"Warehouse\":\""+this.getCodeWarehouse()+"\", \"CodeWarehouse\":\""+this.getCodeWarehouse()+"\", \"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"" +
                (parData==null?"":","+parData )+"}";
    }

    @Override
    public String getCodeWarehouse() {
        String code = "000000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }
    public boolean isSPAR() {
        //String code = "000000000" + CodeWarehouse;
        if(CodeWarehouse==null|| CodeWarehouse=="")
            return false;
        return Integer.parseInt(CodeWarehouse)>50;
    }
    protected GlobalConfig() {
        super("http://znp.vopak.local/api/api_v1_utf8.php");//"http://195.16.78.134:7654/api/api_v1_utf8.php"
    }//

    public static GlobalConfig instance() {
        if (Instance == null) {
            Instance = new GlobalConfig();
        }

        return Instance;
    }

    public void Init(Context parApplicationContext)
    {
        varApplicationContext=parApplicationContext;
        //Визначаємо тип Сканера
        TypeScaner=GetTypeScaner(varApplicationContext);
        //SQLite


       // if( TypeScaner==eTypeScaner.Camera)

        GetSQLiteAdapter(varApplicationContext);
        //Worker
        GetWorker();

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {

                if( true || GetAddressReachable(UrlLocal))//,PortLocal,1000))
                    ApiUrl="http://"+UrlLocal+":"+String.valueOf(PortLocal)+PathApi;
                else
                    ApiUrl="http://"+Url+":"+String.valueOf(Port)+PathApi;

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

                return null;
            }
        }).execute();
    }
 public void InitScaner(ScanCallBack cCallBack)
 {
     GetScaner();
     if(Scaner!=null)
        Scaner.Init(cCallBack);

 }
    public Scaner GetScaner() {

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

    public void SetProgressBar(ProgressBar varProgressBar) {
        if (Worker != null)
            Worker.SetProgressBar(varProgressBar);
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

    eTypeScaner GetTypeScaner(Context parApplicationContext) {
        if(parApplicationContext==null)
            return eTypeScaner.None;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        if (model.equals("TC20") && (manufacturer.contains("Zebra Technologies") || manufacturer.contains("Motorola Solutions")))
            return eTypeScaner.ZebraTC20;
        if (model.equals("PM550") && (manufacturer.contains("POINTMOBILE")|| manufacturer.contains("Point Mobile Co., Ltd.")))
            return eTypeScaner.PM550;

        return eTypeScaner.Camera;

    }

    public String GetSN()
    {
        try {
            if (android.os.Build.VERSION.SDK_INT > 25)
                return Build.getSerial();
            if (android.os.Build.VERSION.SDK_INT == 25)
                return Build.SERIAL;
        }
        catch (Exception e )
        {
            String ee=e.getMessage();
        }
        return "";

    }

    public boolean GetAddressReachable(String parHost)
    {
        try {
            return InetAddress.getByName(parHost).isReachable(1000);
        }
        catch ( Exception ex)
        {
            String Res=ex.getMessage();

        return false;}
    }
    public boolean GetAddressReachable(String address, int port, int timeout) {
        try {

            try (Socket crunchifySocket = new Socket()) {
                // Connects this socket to the server with a specified timeout value.
                crunchifySocket.connect(new InetSocketAddress(address, port), timeout);
            }
            // Return true if connection successful
            return true;
        } catch (IOException exception) {
            exception.printStackTrace();

            // Return false if connection fails
            return false;
        }
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
