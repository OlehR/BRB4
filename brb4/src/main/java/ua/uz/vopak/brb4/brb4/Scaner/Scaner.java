package ua.uz.vopak.brb4.brb4.Scaner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

//getApplicationContext()
public class Scaner extends Activity {
    Context varApplicationContext;
    public ScanCallBack CallBack;
    public String TypeBarCode;



    public Scaner(Context parApplicationContext)
    {
        varApplicationContext=parApplicationContext;

            /*mScanResultReceiverPM = new ScanResultReceiverPM();
            IntentFilter IF=new IntentFilter("device.sdk.sample.scanner.permission.SCANNER_RESULT_RECEIVER");
            IF=new IntentFilter("device.scanner.EVENT");
            this.registerReceiver(mScanResultReceiverPM,IF);
            this.registerReceiver(mScanResultReceiverPM, new IntentFilter("device.scanner.EVENT"));*/

    }
/*
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
*/
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

    public void Close()   {    }

    public void handleDecodeData(Intent i)
    {}

    public void ManualScan(){}


}
