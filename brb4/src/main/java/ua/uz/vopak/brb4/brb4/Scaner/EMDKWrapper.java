package ua.uz.vopak.brb4.brb4.Scaner;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class EMDKWrapper implements EMDKListener, Scanner.DataListener, Scanner.StatusListener, BarcodeManager.ScannerConnectionListener {
    private String profileName = "DataCaptureProfile";

    //Declare a variable to store ProfileManager object
    private ProfileManager mProfileManager = null;
    private Context varContext = null;
    //Declare a variable to store EMDKManager object
    EMDKManager emdkManager = null;
    BarcodeManager barcodeManager = null;
    Scanner scanner = null;

    String statusString = "";

    boolean doSoftScanOnce = false;

    //Local variable to indicate that the use requested the configuration change.
    boolean setDecodersRequested = false;
    public EMDKWrapper(Context parContext) {
        varContext = parContext;
    }


    public boolean getEMDKManager(Bundle savedInstanceState) {
        EMDKResults results = EMDKManager.getEMDKManager(varContext, this);

        //Check the return status of getEMDKManager
        return !(results.statusCode == EMDKResults.STATUS_CODE.FAILURE);

    }



    public void release() {
        initScanner();
        //Release the EMDKmanager on Application exit.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;
          /*

        //Get the ProfileManager object to process the profiles
        mProfileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

        if (mProfileManager != null) {
            try {

                String[] modifyData = new String[1];
                //Call processPrfoile with profile name and SET flag to create the profile. The modifyData can be null.

                EMDKResults results = mProfileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, modifyData);
                if (results.statusCode == EMDKResults.STATUS_CODE.FAILURE) {
                    //Failed to set profile
                }
            } catch (Exception ex) {
                // Handle any exception
            }


        }*/

        initScanner();
    }


    @Override
    public void onClosed() {
        deInitScanner();
        /* EMDKManager is closed abruptly. Call EmdkManager.release() to free the resources used by the current EMDK instance. */
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onData(ScanDataCollection scanDataCollection) {

        if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
            ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
            for (ScanDataCollection.ScanData data : scanData) {
                String dataString = data.getData();
                String labelType = data.getLabelType().toString();

            }
        }
    }

    /**
     * Initialize the barcode manager to scan the barcode
     */
    public void initScanner() {

        if(emdkManager != null) {

            // Acquire the barcode manager object
            barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);

            // Add connection listener to get the external scanner connection and disconnection notification.
            if (barcodeManager != null) {
                barcodeManager.addConnectionListener(this);
            }

            //Get the scanner object to use the default scanner
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
            if (scanner != null) {

                scanner.addStatusListener(this);
                scanner.addDataListener(this);
                try {
                    scanner.enable();
                    scanner.read();
                } catch (ScannerException e) {
                    //displayStatus("" + e.getMessage());
                    Log.d(TAG, "initScanner: " + e.getMessage());
                }
            }else{
                //displayStatus("Failed to initialize the scanner device.");
                Log.d(TAG, "initScanner: " + "Failed to initialize the scanner device.");
            }
        }
    }

    /**
     *  Release the barcode manager resources
     */
    private void deInitScanner() {

        if (barcodeManager != null) {
            emdkManager.release(EMDKManager.FEATURE_TYPE.BARCODE);
        }
    }

    @Override
    public void onStatus(StatusData statusData) {

        StatusData.ScannerStates state = statusData.getState();
        Log.d(TAG, "onStatus: " + statusData.getFriendlyName()+" is enabled and idle...");
        switch(state) {
            case IDLE:
                statusString = statusData.getFriendlyName()+" is enabled and idle...";
                if(!scanner.isReadPending()) {

                    // Set decoder configuration if the user requested
                    if (setDecodersRequested) {
                        setDecoders();
                        setDecodersRequested = false;
                    }

                    if( doSoftScanOnce ) {
                        scanner.triggerType = Scanner.TriggerType.SOFT_ONCE;
                        doSoftScanOnce = false;
                    }

                    try {
                        scanner.read();
                    } catch (ScannerException e) {
                        e.printStackTrace();
                    }
                }else {
                    statusString = "Previous read is still pending..";
                }
                break;
            case WAITING:
                statusString = "Scanner is waiting for trigger press...";
                break;
            case SCANNING:
                statusString = "Scanning...";
                break;
            case DISABLED:
                statusString = statusData.getFriendlyName()+" is disabled.";

                break;
            case ERROR:
                statusString = "An error has occurred.";
                break;
            default:
                break;
        }

       // displayStatus(statusString);
    }
/*
    private void addSoftScanButtonListener() {

        Button btnSoftScan = (Button)findViewById(R.id.buttonSoftScan);

        btnSoftScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (scanner.isEnabled()) {
                    if (scanner.isReadPending()) {
                        doSoftScanOnce = true;
                        try {
                            scanner.cancelRead();
                        } catch (ScannerException e) {
                            e.printStackTrace();
                            displayStatus("" + e.getMessage());
                        }

                    } else {
                        scanner.triggerType = Scanner.TriggerType.SOFT_ONCE;
                        try {
                            scanner.read();
                        } catch (ScannerException e) {
                            e.printStackTrace();
                            displayStatus("" + e.getMessage());
                        }
                    }
                } else {
                    displayStatus("Scanner is not enabled");
                }
            }
        });
    }
*/
    private void setDecoders() {

        try {

            ScannerConfig config = scanner.getConfig();

            //Set ean8
            config.decoderParams.ean8.enabled = false;

            //Set UPCA
            config.decoderParams.upca.enabled = false;

            //set UPCE
            config.decoderParams.upce0.enabled = true;
            config.decoderParams.upce1.enabled = true;

            //Set Code128
            config.decoderParams.code128.enabled = true;

            scanner.setConfig(config);

        } catch (ScannerException e) {
            //displayStatus("" + e.getMessage());
            Log.d(TAG, "BarcodeSample1.setDecoders: " + e.getMessage());
        }
    }
    @Override
    public void onConnectionChange(ScannerInfo scannerInfo, BarcodeManager.ConnectionState connectionState) {
        switch(connectionState) {
            case CONNECTED:
                initScanner();
                break;
            case DISCONNECTED:
                deInitScanner();
                break;
        }
    }

}

