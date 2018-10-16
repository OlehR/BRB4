package ua.uz.vopak.brb4.brb4;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.app.Activity;

import ua.uz.vopak.brb4.brb4.fragments.ScanFragment;

//import static android.app.PendingIntent.getActivity;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    Button btnRestart;
    public  static Boolean isCreatedScaner = false;
    private Worker  worker;
    private EMDKWrapper emdkWrapper = null;
    TextView codeView, textBarcodeView, perView, nameView, priceView, oldPriceView,oldPriceText,priceText,Printer,
             Network, PrintCount, AllCount, Percent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String model = android.os.Build.MODEL;
        if( model.equals("TC20")  && ( android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions")) ){
                emdkWrapper  = new EMDKWrapper(getApplicationContext());
        }

        if(emdkWrapper != null){
            isCreatedScaner=emdkWrapper.getEMDKManager(savedInstanceState);
        }

        setContentView(R.layout.main_layout);

        //btnRestart = findViewById(R.id.button);
        //btnRestart.setOnClickListener(this);
        codeView = findViewById(R.id.code);
        perView  = findViewById(R.id.per);
        nameView  = findViewById(R.id.title);
        oldPriceView  = findViewById(R.id.old_price);
        priceView  = findViewById(R.id.price);
        textBarcodeView = findViewById(R.id.bar_code);
        oldPriceText = findViewById(R.id.old_price_text);
        priceText = findViewById(R.id.price_text);
        Printer = findViewById(R.id.Printer);
        Network = findViewById(R.id.Network);
        PrintCount = findViewById(R.id.PrintCount);
        AllCount = findViewById(R.id.AllCount);
        Percent = findViewById(R.id.AllCount);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //In case we have been launched by the DataWedge intent plug-in
        Intent i = getIntent();
        handleDecodeData(i);

        Context c=this.getApplicationContext();
        worker = new Worker(this);
        setScanResult(worker.LI);
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
                    ExecuteWorker(data);
                    //ScanFragment sf = (ScanFragment) getSupportFragmentManager().findFragmentById(R.id.scan_fragment);

                }
            }
        }
    }

    public void  setScanResult(LabelInfo LI){



        codeView.setText(Integer.toString(LI.Code));
        perView.setText(LI.Unit);
        nameView.setText(LI.Name);
        Printer.setText(LI.InfoPrinter);
        Printer.setTextColor(Color.parseColor("#856404"));
        Network.setText(LI.InfoHTTP);
        Network.setTextColor(Color.parseColor("#856404"));
        PrintCount.setText(LI.BadScan);
        PrintCount.setTextColor(Color.parseColor("#856404"));
        AllCount.setText(LI.AllScan);
        AllCount.setTextColor(Color.parseColor("#856404"));
        Percent.setText(((LI.AllScan - LI.BadScan) / LI.AllScan) * 100);
        Percent.setTextColor(Color.parseColor("#856404"));


        if(LI.OldPrice != LI.Price){
            oldPriceView.setTextColor(Color.parseColor("#ee4343"));
            priceView.setTextColor(Color.parseColor("#ee4343"));
            oldPriceText.setTextColor(Color.parseColor("#ee4343"));
            priceText.setTextColor(Color.parseColor("#ee4343"));
        }else {
            oldPriceView.setTextColor(Color.parseColor("#3bb46e"));
            priceView.setTextColor(Color.parseColor("#3bb46e"));
            oldPriceText.setTextColor(Color.parseColor("#3bb46e"));
            priceText.setTextColor(Color.parseColor("#3bb46e"));
        }

        oldPriceView.setText(String.format("%.2f",(double)LI.OldPrice/100));
        priceView.setText(String.format("%.2f",(double)LI.Price/100));
        textBarcodeView.setText(LI.BarCode);


        if(!MainActivity.isCreatedScaner) {
            //barcodeView.resume();
        }
    }


    public void SetProgres(int progres){

            ProgressBar progresBar = findViewById(R.id.progressBar);
            progresBar.setProgress(progres);

    }

    public void ExecuteWorker(String parBarCode){
        AsyncWorker aW =  new AsyncWorker(worker);
        aW.execute(parBarCode);
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

    }
