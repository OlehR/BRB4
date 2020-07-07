package ua.uz.vopak.brb4.brb4.Scaner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//import device.common.ScanConst;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;

//import static ua.uz.vopak.brb4.brb4.Scaner.ScanerPM500.mScanner;

public class ScanResultReceiverTC extends BroadcastReceiver {

    GlobalConfig config = GlobalConfig.instance();

    //We need to handle any incoming intents, so let override the onNewIntent method
    @Override
    public void onReceive(Context context, Intent intent) {
        if(config.TypeScaner!= eTypeScaner.Zebra)
        {
            Scaner mScaner=config.GetScaner();
            String v=intent.getStringExtra("com.symbol.datawedge.data_string");
            mScaner.CallBack.Run(v);
        }


    }

}



