package ua.uz.vopak.brb4.brb4.Scaner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
import device.common.DecodeResult;
import device.common.DecodeStateCallback;
import device.common.ScanConst;
import device.sdk.ScanManager;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

//getApplicationContext()
public class Scaner extends Activity {
    Context varApplicationContext;
    public ScanCallBack CallBack;
    //private final Handler mHandler = new Handler();


    public Scaner(Context parApplicationContext)
    {
        varApplicationContext=parApplicationContext;

            /*mScanResultReceiverPM = new ScanResultReceiverPM();
            IntentFilter IF=new IntentFilter("device.sdk.sample.scanner.permission.SCANNER_RESULT_RECEIVER");
            IF=new IntentFilter("device.scanner.EVENT");
            this.registerReceiver(mScanResultReceiverPM,IF);
            this.registerReceiver(mScanResultReceiverPM, new IntentFilter("device.scanner.EVENT"));*/

    }

    //Посилаємо повідомлення (Зараз через CallBack)
    public void Send(String parBarcode) {

        Intent MyIntent = new Intent("BRB4.BARCODE");
        MyIntent.putExtra("BARCODE", parBarcode);
        MyIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        try {
            varApplicationContext.sendBroadcast(MyIntent);
        } catch (Exception e)
        {
            String s = e.getMessage();
        }

    }

    public boolean Init(ScanCallBack cCallBack)
    {
        CallBack=cCallBack;
        return true;
    }

    public boolean Init(ScanCallBack cCallBack,Bundle savedInstanceState)
    {
        return Init(cCallBack);
    }

    public boolean StartScan()
    {
        return true;
    }
    public boolean StopScan()
    {
        return true;
    }

    public void Close()
    {

    }

    public void handleDecodeData(Intent i)
    {}





}
