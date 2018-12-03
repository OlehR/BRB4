package ua.uz.vopak.brb4.brb4.helpers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.helpers.EMDKWrapper;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.brb4.helpers.mScanerWrapper;
import device.common.DecodeResult;
import device.common.DecodeStateCallback;
import device.common.ScanConst;
import device.sdk.ScanManager;

//getApplicationContext()
public class Scaner extends Activity {
    Context varApplicationContext;
    ScanCallBack CallBack;
    private final Handler mHandler = new Handler();
    eTypeScaner TypeScaner= eTypeScaner.None;

    EMDKWrapper emdkWrapper;
    public mScanerWrapper mScanerW;
    //private ScanResultReceiverPM mScanResultReceiverPM;

    public Scaner(Context parApplicationContext)
    {
        varApplicationContext=parApplicationContext;
        String model = android.os.Build.MODEL;
        if( model.equals("TC20") && ( android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions")) ){
            emdkWrapper  = new EMDKWrapper(varApplicationContext);
            TypeScaner= eTypeScaner.ZebraTC20;
        }

        if( model.equals("PM550") && android.os.Build.MANUFACTURER.contains("Point Mobile Co., Ltd.")){
            mScanerW  = new mScanerWrapper();
            //mScanerW.mContext = this;
            mScanerW.mScanner = new ScanManager();
            mScanerW.mDecodeResult = new DecodeResult();
            mScanerW.mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_EVENT);
            TypeScaner= eTypeScaner.PM550;

            /*mScanResultReceiverPM = new ScanResultReceiverPM();
            IntentFilter IF=new IntentFilter("device.sdk.sample.scanner.permission.SCANNER_RESULT_RECEIVER");
            IF=new IntentFilter("device.scanner.EVENT");
            this.registerReceiver(mScanResultReceiverPM,IF);
            this.registerReceiver(mScanResultReceiverPM, new IntentFilter("device.scanner.EVENT"));*/

        }
    }


    public void Send(String parBarcode) {
        //Переробити через повідомлення
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
    //Motorola
    //This function is responsible for getting the data from the intent
    private void handleDecodeData(Intent i)
    {
        if (i.getAction() != null && i.getAction().contentEquals("ua.uz.vopak.brb4.brb4.RECVR") ) {
            //Get the source of the data
            String source = i.getStringExtra("com.motorolasolutions.emdk.datawedge.source");

            //Check if the data has come from the Barcode scanner
            if(source.equalsIgnoreCase("scanner"))
            {
                //Get the data from the intent
                String data = i.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");

                //Check that we have received data
                if(data != null && data.length() > 0)
                {
                    CallBack.Run(data);
                    //ScanFragment sf = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.scan_fragment);

                }
            }
        }
    }

    public boolean StartScan()
    {
        return true;
    }

    public boolean init(ScanCallBack cCallBack)
    {
        CallBack=cCallBack;
        return true;
    }

    public boolean StopScan()
    {
        return true;
    }

    public void OnResume()
    {

    }
    @Override
    public void onPause() {
        super.onPause();
        if (mScanerW != null) {
            mScanerW.mScanner.aUnregisterDecodeStateCallback(mStateCallback);
        }

    }

    public void close()
    {

    }

    private DecodeStateCallback mStateCallback = new DecodeStateCallback(mHandler) {
        public void onChangedState(int state) {
            switch (state) {
                case ScanConst.STATE_ON:
                case ScanConst.STATE_TURNING_ON:

                    break;
                case ScanConst.STATE_OFF:
                case ScanConst.STATE_TURNING_OFF:

                    break;
            }
        };
    };


    private void initScanner() {
        if (mScanerW != null) {
            mScanerW.mScanner.aRegisterDecodeStateCallback(mStateCallback);
            mScanerW.mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);
        }
    }
/*
    private Runnable mStartOnResume = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initScanner();
                }
            });
        }
    };
*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
                //Release the EMDKmanager on Application exit.
        if (emdkWrapper != null) {
            emdkWrapper.release();
        }
    }

}
