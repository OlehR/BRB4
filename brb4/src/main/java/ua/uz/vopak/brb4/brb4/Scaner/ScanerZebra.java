package ua.uz.vopak.brb4.brb4.Scaner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;


public class ScanerZebra extends Scaner {
    BroadcastReceiver mybroadcastReceiver;
    IntentFilter filter;
     boolean StatusScan=false;
    //!!!TMP!!!!
    final String  Action= "ua.uz.vopak.brb4";

    final String ACTION = "com.symbol.datawedge.api.ACTION";
    final String SWITCH = "com.symbol.datawedge.api.SWITCH_TO_PROFILE";
    final String CREATE_PROFILE = "com.symbol.datawedge.api.CREATE_PROFILE";
    final String PROFILE_NAME = "PROFILE_NAME";
    final String PROFILE_STATUS = "PROFILE_ENABLED";
    final String CONFIG_MODE = "CONFIG_MODE";
    final String CONFIG_MODE_UPDATE = "UPDATE";
    final String CONFIG_MODE_CREATE = "CREATE_IF_NOT_EXIST";
    final String SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG";

    final String  SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";
    final String START_SCANNING = "START_SCANNING";

    private final String DW_PKG_NAME = "com.symbol.datawedge";
    private final String DW_INTENT_SUPPORT_VERSION = "6.3";

    //Broadcast Receiver for recieving the intents back from DataWedge

    public ScanerZebra(Context parApplicationContext) {
        super(parApplicationContext);
        mybroadcastReceiver   = new BroadcastReceiver () {
            final String LABEL_TYPE_TAG = "com.symbol.datawedge.label_type";
            final String DATA_STRING_TAG = "com.symbol.datawedge.data_string";
            //  final String DECODE_DATA_TAG = "com.symbol.datawedge.decode_data";
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null){
                    TypeBarCode = intent.getStringExtra(LABEL_TYPE_TAG);
                    String decodeData = intent.getStringExtra(DATA_STRING_TAG);
                    CallBack.Run(decodeData);
                    Log.d( "Zebra","onReceive =>"+decodeData + " Type=>"+TypeBarCode);
                }
            }
        };

        filter = new IntentFilter();
        filter.addAction(Action);
        filter.addCategory("android.intent.category.DEFAULT");
        StartScan1();

    }

    @Override
    public boolean Init(ScanCallBack cCallBack,Bundle savedInstanceState)
    {
        Log.d( "Zebra","Init");
        return super.Init(cCallBack,savedInstanceState);
    }

    @Override
    public boolean StartScan() {
        return true;
    }


    public boolean StartScan1()
    {
        Log.d( "Zebra","StartScan Status=>"+ (StatusScan?"On":"Off"));
        if (StatusScan)
            return false;
        if(varApplicationContext!=null)
            varApplicationContext.registerReceiver(mybroadcastReceiver,filter);
        //ManualScan();
        StatusScan=true;
        Log.d( "Zebra","StartScan End Status=>"+ (StatusScan?"On":"Off"));
        return true;
    }
    @Override
    public boolean StopScan() {
    return true;
    }

    public boolean StopScan1()
    {
        Log.d( "Zebra","StopScan Status=>"+ (StatusScan?"On":"Off"));
        if (!StatusScan)
            return false;
        if(varApplicationContext!=null)
            varApplicationContext.unregisterReceiver(mybroadcastReceiver);
       // unregisterReceiver(mybroadcastReceiver);

        StatusScan=false;
        Log.d( "Zebra","StopScan End Status=>"+ (StatusScan?"On":"Off"));
        return true;
    }

    public void finalize() {
        StopScan1();
    }

    @Override
    public void Close()
    {

    }

    public void createDataWedgeProfile()
    {
        //Create profile if doesn't exit and update the required settings
        {
            Bundle configBundle = new Bundle();
            Bundle bConfig = new Bundle();
            Bundle bParams = new Bundle();
            Bundle bundleApp1 = new Bundle();

            bParams.putString("scanner_selection", "auto");
            bParams.putString("intent_output_enabled", "true");
            bParams.putString("intent_action", Action);
            bParams.putString("intent_category", "android.intent.category.DEFAULT");
            bParams.putString("intent_delivery", "2");

            configBundle.putString(PROFILE_NAME, "brb4");
            configBundle.putString(PROFILE_STATUS, "true");
            configBundle.putString(CONFIG_MODE, CONFIG_MODE_CREATE);

            bundleApp1.putString("PACKAGE_NAME", Action);
            bundleApp1.putStringArray("ACTIVITY_LIST", new String[]{Action});


            configBundle.putParcelableArray("APP_LIST", new Bundle[]{bundleApp1});

            bConfig.putString("PLUGIN_NAME", "INTENT");
            bConfig.putString("RESET_CONFIG", "false");

            bConfig.putBundle("PARAM_LIST", bParams);
            configBundle.putBundle("PLUGIN_CONFIG", bConfig);

            Intent i = new Intent();
            i.setAction(ACTION);
            i.putExtra(SET_CONFIG, configBundle);
            this.sendBroadcast(i);
        }

        //TO recieve the scanned via intent, the keystroke must disabled.
        {
            Bundle configBundle = new Bundle();
            Bundle bConfig = new Bundle();
            Bundle bParams = new Bundle();

            bParams.putString("keystroke_output_enabled", "false");

            configBundle.putString(PROFILE_NAME, "brb4");
            configBundle.putString(PROFILE_STATUS, "true");
            configBundle.putString(CONFIG_MODE, CONFIG_MODE_UPDATE);

            bConfig.putString("PLUGIN_NAME", "KEYSTROKE");
            bConfig.putString("RESET_CONFIG", "false");

            bConfig.putBundle("PARAM_LIST", bParams);
            configBundle.putBundle("PLUGIN_CONFIG", bConfig);

            Intent i = new Intent();
            i.setAction(ACTION);
            i.putExtra(SET_CONFIG, configBundle);
            this.sendBroadcast(i);
        }
    }

    @Override
    public void ManualScan()
    {
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(SOFT_SCAN_TRIGGER, START_SCANNING);
        sendBroadcast(i);
    }

}
