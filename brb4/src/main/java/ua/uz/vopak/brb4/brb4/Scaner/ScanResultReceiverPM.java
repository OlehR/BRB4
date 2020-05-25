package ua.uz.vopak.brb4.brb4.Scaner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import device.common.ScanConst;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

import static ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500.mScanner;

public  class ScanResultReceiverPM extends BroadcastReceiver {

    GlobalConfig config = GlobalConfig.instance();

    @Override
    public void onReceive(Context context, Intent intent) {
        if(config.TypeScaner!=eTypeScaner.PM550)
            return;
        ScanerPM500 mScaner=(ScanerPM500)config.GetScaner();
        if(mScaner!=null) {
            //mScanerWrapper mScanerW = mScaner.mScanerW;

            if (mScaner != null) {
                if (ScanConst.INTENT_USERMSG.equals(intent.getAction())) {
                    mScanner.aDecodeGetResult(mScaner.mDecodeResult.recycle());
                } else if (ScanConst.INTENT_EVENT.equals(intent.getAction())) {
                    byte[] decodeBytesValue = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE);
                    if (decodeBytesValue != null) {
                        String value = new String(decodeBytesValue);

                        if(mScaner.CallBack!=null)
                          mScaner.CallBack.Run(value);
                    }

                }
            }
        }
    }
}
