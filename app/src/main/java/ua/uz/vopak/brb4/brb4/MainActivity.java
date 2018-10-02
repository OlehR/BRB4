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
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.StatusData;

import ua.uz.vopak.brb4.brb4.fragments.ScanFragment;

public class MainActivity extends FragmentActivity implements View.OnClickListener, EMDKListener, StatusListener, DataListener{
    Button btnRestart;
    public  static Boolean isCreatedScaner = false;
    //Assign the profile name used in EMDKConfig.xml
    private String profileName = "DataCaptureProfile";

    //Declare a variable to store ProfileManager object
    private ProfileManager mProfileManager = null;

    //Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        //Check the return status of getEMDKManager
        if(results.statusCode == EMDKResults.STATUS_CODE.FAILURE)
        {
            isCreatedScaner = false;

        }else {
            isCreatedScaner = true;
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


    @Override
    public void onClosed() {
        // TODO Auto-generated method stub
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
    public void onData(ScanDataCollection scanDataCollection) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onStatus(StatusData statusData) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //Clean up the objects created by EMDK manager
        emdkManager.release();
    }

}
