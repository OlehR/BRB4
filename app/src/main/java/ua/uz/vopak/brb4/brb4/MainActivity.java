package ua.uz.vopak.brb4.brb4;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import ua.uz.vopak.brb4.brb4.fragments.ScanFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    Button btnRestart;
    public  static Boolean isCreatedScaner = false;

    private EMDKWrapper emdkWrapper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions") ){
                emdkWrapper  = new EMDKWrapper();
        }

        if(emdkWrapper != null){
            emdkWrapper.getEMDKManager(savedInstanceState);
        }

        setContentView(R.layout.main_layout);

        btnRestart = findViewById(R.id.button);
        btnRestart.setOnClickListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //In case we have been launched by the DataWedge intent plug-in
        Intent i = getIntent();
        handleDecodeData(i);
    }

    //We need to handle any incoming intents, so let override the onNewIntent method
    @Override
    public void onNewIntent(Intent i) {
        handleDecodeData(i);

    }

    @Override
    public void onClick(View v) {
        if(!isCreatedScaner) {
            BarcodeView barcodeView = findViewById(R.id.barcode_scanner);
            barcodeView.resume();
        }
    }


    //This function is responsible for getting the data from the intent
    private void handleDecodeData(Intent i)
    {
        //Check the intent action is for us
        if (i.getAction().contentEquals("ua.uz.vopak.brb4.brb4.RECVR") ) {
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
                    ScanFragment sf = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.scan_fragment);
                    AsyncWorker aW =  new AsyncWorker(sf.worker);
                    aW.execute(data);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //Release the EMDKmanager on Application exit.
        if (emdkWrapper  != null) {
            emdkWrapper.release();
        }
    }

    public class EMDKWrapper implements EMDKListener {
        private String profileName = "DataCaptureProfile";

        //Declare a variable to store ProfileManager object
        private ProfileManager mProfileManager = null;

        //Declare a variable to store EMDKManager object
        EMDKManager emdkManager = null;


        void getEMDKManager(Bundle savedInstanceState) {
            EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

            //Check the return status of getEMDKManager
            if(results.statusCode == EMDKResults.STATUS_CODE.FAILURE)
            {
                isCreatedScaner = false;

            }else {
                isCreatedScaner = true;
            }
        }


        void release() {
            //Release the EMDKmanager on Application exit.
            if (emdkManager != null) {
                emdkManager.release();
                emdkManager = null;
            }
        }

        @Override
        public void onOpened(EMDKManager emdkManager) {
            this.emdkManager = emdkManager;

            //Get the ProfileManager object to process the profiles
            mProfileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

            if(mProfileManager != null)
            {
                try{

                    String[] modifyData = new String[1];
                    //Call processPrfoile with profile name and SET flag to create the profile. The modifyData can be null.

                    EMDKResults results = mProfileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, modifyData);
                    if(results.statusCode == EMDKResults.STATUS_CODE.FAILURE)
                    {
                        //Failed to set profile
                    }
                }catch (Exception ex){
                    // Handle any exception
                }


            }
        }


        @Override
        public void onClosed() {
            /* EMDKManager is closed abruptly. Call EmdkManager.release() to free the resources used by the current EMDK instance. */
            if (emdkManager != null) {
                emdkManager.release();
                emdkManager = null;
            }
        }
    }

}
