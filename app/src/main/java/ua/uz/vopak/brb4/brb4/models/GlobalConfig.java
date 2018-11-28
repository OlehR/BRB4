package ua.uz.vopak.brb4.brb4.models;

import android.content.Context;
import android.widget.ProgressBar;

import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncGetWarehouseConfig;

public class GlobalConfig {
    private static GlobalConfig Instance = null;
    public static String CodeWarehouse = "0";
    public static String ApiUrl = "http://znp.vopak.local/api/api_v1_utf8.php";
    public static String Login = "c";
    public static String Password = "c";
    public static Worker Worker;
    public static SQLiteAdapter SQLiteAdapter;

    public static String GetLoginJson()
    {
      return "\"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"";
    }




    public String getCodeWarehouse(){
        String code = "00000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    protected GlobalConfig(){



    }

    public static GlobalConfig instance(){
        if(Instance == null){
            Instance = new GlobalConfig();
        }
        return Instance;
    }

    public static Worker GetWorker(ProgressBar varProgressBar)
    {
        Worker=GetWorker();
        if(Worker!=null)
           Worker.SetProgressBar(varProgressBar);
        return Worker;
    }

    public static Worker GetWorker(){
        if(Worker == null){
            Worker = new Worker();        }
        return Worker;
    }

    public static SQLiteAdapter GetSQLiteAdapter(Context c){
        if(SQLiteAdapter == null){
            SQLiteAdapter = new SQLiteAdapter(c);
            SQLiteAdapter.createDatabase();
            SQLiteAdapter.open();
        }
        return SQLiteAdapter;
    }

    public static SQLiteAdapter GetSQLiteAdapter(){
        return SQLiteAdapter;
    }

}
