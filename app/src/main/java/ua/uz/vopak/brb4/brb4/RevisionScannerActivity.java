package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;

import device.common.DecodeResult;
import device.common.DecodeStateCallback;
import device.common.ScanConst;
import device.sdk.ScanManager;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncRevisionScanHelper;
import ua.uz.vopak.brb4.brb4.helpers.EMDKWrapper;
import ua.uz.vopak.brb4.brb4.helpers.mScanerWrapper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.RevisionItemModel;

public class RevisionScannerActivity extends Activity {
    EditText barCode, currentCount, inpuCount, scannerCof, scannerCount, countInPosition;
    TextView scannerTitle, inPosition;
    EMDKWrapper emdkWrapper;
    static mScanerWrapper mScanerW;
    private final Handler mHandler = new Handler();
    public static RevisionScannerActivity aContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.revision_scanner_activity);

        aContext = this;

        String model = android.os.Build.MODEL;
        if( model.equals("TC20") && ( android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions")) ){
            emdkWrapper  = new EMDKWrapper(getApplicationContext());
        }

        if( model.equals("PM550") && android.os.Build.MANUFACTURER.contains("Point Mobile Co., Ltd.")){
            mScanerW  = new mScanerWrapper();
            mScanerW.mContext = this;
            mScanerW.mScanner = new ScanManager();
            mScanerW.mDecodeResult = new DecodeResult();
            mScanerW.mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_EVENT);
        }


    }

    public void RenderTable(RevisionItemModel model){

    }

    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mScanerW != null) {
                if (ScanConst.INTENT_USERMSG.equals(intent.getAction())) {
                    mScanerW.mScanner.aDecodeGetResult(mScanerW.mDecodeResult.recycle());
                }else if (ScanConst.INTENT_EVENT.equals(intent.getAction())) {
                    byte[] decodeBytesValue = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE);
                    String value = new String(decodeBytesValue);

                    new AsyncRevisionScanHelper(GlobalConfig.instance().GetWorker(), aContext).execute(value);

                }
            }

        }
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

    @Override
    protected void onPause() {
        if (mScanerW.mScanner != null) {
            mScanerW.mScanner.aUnregisterDecodeStateCallback(mStateCallback);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mScanerW != null) {
            mScanerW = null;
        }
        super.onDestroy();
    }
}
