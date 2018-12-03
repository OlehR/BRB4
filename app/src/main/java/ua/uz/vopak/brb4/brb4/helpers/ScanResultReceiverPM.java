package ua.uz.vopak.brb4.brb4.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import device.common.ScanConst;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

import static ua.uz.vopak.brb4.brb4.models.GlobalConfig.GetScaner;

public  class ScanResultReceiverPM extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Scaner mScaner=GlobalConfig.GetScaner(null);
        if(mScaner!=null) {
            mScanerWrapper mScanerW = mScaner.mScanerW;

            if (mScanerW != null) {
                if (ScanConst.INTENT_USERMSG.equals(intent.getAction())) {
                    mScanerW.mScanner.aDecodeGetResult(mScanerW.mDecodeResult.recycle());
                } else if (ScanConst.INTENT_EVENT.equals(intent.getAction())) {
                    byte[] decodeBytesValue = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE);
                    if (decodeBytesValue != null) {
                        String value = new String(decodeBytesValue);
                        //mScaner.Send(value);
                        if(mScaner.CallBack!=null)
                          mScaner.CallBack.Run(value);

                    /* //Переробити через повідомлення
                    Intent MyIntent = new Intent("BRB4.BARCODE");
                    MyIntent.putExtra("BARCODE",value);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(MyIntent);*/

                    }

                }
            }
        }
    }
}
