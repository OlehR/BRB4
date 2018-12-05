package ua.uz.vopak.brb4.brb4.Scaner;

        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.os.Handler;


public class ScanerTC20 extends Scaner {

    private final Handler mHandler = new Handler();
    EMDKWrapper emdkWrapper;


    public ScanerTC20(Context parApplicationContext)
    {
       super(parApplicationContext);
       emdkWrapper  = new EMDKWrapper(varApplicationContext);
    }


    //Motorola
    //This function is responsible for getting the data from the intent
    private void handleDecodeData(Intent i)
    {
        if (i.getAction() != null && i.getAction().contentEquals("ua.uz.vopak.brb4.brb4.RECVR") ) {
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
                    CallBack.Run(data);

                }
            }
        }
    }



    @Override
    public void Close()
    {
        if (emdkWrapper != null) {
            emdkWrapper.release();

        }
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
