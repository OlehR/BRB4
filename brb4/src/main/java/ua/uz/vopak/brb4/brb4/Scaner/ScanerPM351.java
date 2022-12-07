package ua.uz.vopak.brb4.brb4.Scaner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class ScanerPM351 extends Scaner {
    BroadcastReceiver mybroadcastReceiver;
    IntentFilter filter;
    boolean StatusScan=false;
    //!!!TMP!!!!
    final String  Action= "device.scanner.EVENT";
    public static final String EXTRA_EVENT_DECODE_VALUE = "EXTRA_EVENT_DECODE_VALUE";

    public ScanerPM351(Context parApplicationContext) {
        super(parApplicationContext);
        mybroadcastReceiver   = new BroadcastReceiver () {
           // final String LABEL_TYPE_TAG = "com.symbol.datawedge.label_type";
            //final String DATA_STRING_TAG = "com.symbol.datawedge.data_string";
            //  final String DECODE_DATA_TAG = "com.symbol.datawedge.decode_data";
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent!=null){

                    if (Action.equals(intent.getAction())) {
                        byte[] decodeBytesValue = intent.getByteArrayExtra(EXTRA_EVENT_DECODE_VALUE);
                        if (decodeBytesValue != null) {
                            String value = new String(decodeBytesValue);
                            CallBack.Run(value);
                            Log.d( "PM","onReceive =>"+value + " Type=>"+TypeBarCode);
                        }
                    }
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
        Log.d( "PM","Init");
        return super.Init(cCallBack,savedInstanceState);
    }

    @Override
    public boolean StartScan() {
        return true;
    }


    public boolean StartScan1()
    {
        Log.d( "PM","StartScan Status=>"+ (StatusScan?"On":"Off"));
        if (StatusScan)
            return false;
        if(varApplicationContext!=null)
            varApplicationContext.registerReceiver(mybroadcastReceiver,filter);
        //ManualScan();
        StatusScan=true;
        Log.d( "PM","StartScan End Status=>"+ (StatusScan?"On":"Off"));
        return true;
    }
    @Override
    public boolean StopScan() {
        return true;
    }

    public boolean StopScan1()
    {
        Log.d( "PM","StopScan Status=>"+ (StatusScan?"On":"Off"));
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


}
