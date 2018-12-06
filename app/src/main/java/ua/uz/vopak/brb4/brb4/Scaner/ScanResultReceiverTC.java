package ua.uz.vopak.brb4.brb4.Scaner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class ScanResultReceiverTC extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        //handleDecodeData(i);
    }

    //We need to handle any incoming intents, so let override the onNewIntent method
    @Override
    public void onNewIntent(Intent i) {
        //handleDecodeData(i);

    }


}
