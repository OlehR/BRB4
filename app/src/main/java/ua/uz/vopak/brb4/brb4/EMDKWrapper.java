package ua.uz.vopak.brb4.brb4;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;

public class EMDKWrapper implements EMDKListener {
    private String profileName = "DataCaptureProfile";

    //Declare a variable to store ProfileManager object
    private ProfileManager mProfileManager = null;
    private Context varContext=null;
    //Declare a variable to store EMDKManager object
    EMDKManager emdkManager = null;

    public EMDKWrapper(Context parContext)
    {
        varContext=parContext;
    }

    boolean getEMDKManager(Bundle savedInstanceState) {
        EMDKResults results = EMDKManager.getEMDKManager(varContext, this);

        //Check the return status of getEMDKManager
        return !(results.statusCode == EMDKResults.STATUS_CODE.FAILURE);

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


