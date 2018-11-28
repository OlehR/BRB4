package ua.uz.vopak.brb4.brb4.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
public class Scaner {
    Context varApplicationContext;
    ScanCallBack CallBack;
    private final Handler mHandler = new Handler();
    eTypeScaner TypeScaner= eTypeScaner.None;

    EMDKWrapper emdkWrapper;
    mScanerWrapper mScanerW;

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

    //PM
    public  class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mScanerW != null) {
                if (ScanConst.INTENT_USERMSG.equals(intent.getAction())) {
                    mScanerW.mScanner.aDecodeGetResult(mScanerW.mDecodeResult.recycle());
                }else if (ScanConst.INTENT_EVENT.equals(intent.getAction())) {
                    byte[] decodeBytesValue = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE);
                    if(decodeBytesValue != null) {
                        String value = new String(decodeBytesValue);
                        CallBack.Run(value);
                    }

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

    public void onPause() {
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
}
