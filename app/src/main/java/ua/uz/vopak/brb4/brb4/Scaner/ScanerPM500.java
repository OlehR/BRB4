package ua.uz.vopak.brb4.brb4.Scaner;
        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Handler;

        import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
        import device.common.DecodeResult;
        import device.common.DecodeStateCallback;
        import device.common.ScanConst;
        import device.sdk.ScanManager;
        import ua.uz.vopak.brb4.brb4.models.GlobalConfig;


public class ScanerPM500 extends Scaner {
    //Context varApplicationContext;
    //public ScanCallBack CallBack;
    private final Handler mHandler = new Handler();
    public static ScanManager mScanner;
    public static DecodeResult mDecodeResult;
    GlobalConfig config = GlobalConfig.instance();


    public ScanerPM500(Context parApplicationContext)
    {
        super(parApplicationContext);
        mScanner = new ScanManager();
        mDecodeResult = new DecodeResult();
        mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_EVENT);
    }


    public void onPause() {
        super.onPause();
        if (mScanner != null) {
            mScanner.aUnregisterDecodeStateCallback(mStateCallback);
        }
    }

    @Override
    public void Close()
    {
        config.Scaner=null;

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

    @Override
    public boolean Init(ScanCallBack cCallBack) {
        super.Init(cCallBack);
        if (mScanner != null) {
//            mScanner.aRegisterDecodeStateCallback(mStateCallback);
        //    mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);
        }
        return (mScanner != null);
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
