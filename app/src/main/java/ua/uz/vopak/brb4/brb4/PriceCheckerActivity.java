package ua.uz.vopak.brb4.brb4;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncWorker;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.LabelInfo;

public class PriceCheckerActivity extends FragmentActivity implements View.OnClickListener{
    private Worker worker;
    TextView codeView, textBarcodeView, perView, nameView, priceView, oldPriceView,oldPriceText,priceText,Printer,
            Network, CountData;
    Button ChangePrintType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.price_checker_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

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
        CountData = findViewById(R.id.CountData);
        ChangePrintType = findViewById(R.id.ChangePrintType );
        ChangePrintType.setOnClickListener(this);

        ProgressBar progresBar = findViewById(R.id.progressBar);
        worker = GlobalConfig.GetWorker(progresBar);
        worker.SetPriceCheckerActivity(this);
        worker.ReInitBT();
        ChangePrintType.setText(worker.LI.IsShort?"Коротка":"Стандартна");

        setScanResult(worker.LI);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(true);

        //In case we have been launched by the DataWedge intent plug-in
        Intent i = getIntent();
        handleDecodeData(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

        //We need to handle any incoming intents, so let override the onNewIntent method
    @Override
    public void onNewIntent(Intent i) {
        handleDecodeData(i);

    }

    @Override
    public void onClick(View v) {
      int Id= v.getId();
        switch (Id){
            case R.id.ChangePrintType:
                worker.LI.IsShort=!worker.LI.IsShort;
                ChangePrintType.setText(worker.LI.IsShort?"Коротка":"Стандартна");
                break;
        }

        if(!MainActivity.isCreatedScaner) {
            BarcodeView barcodeView = findViewById(R.id.barcode_scanner);
            barcodeView.resume();

        }
    }




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
        if(LI.InfoPrinter.equals("CanNotOpen"))
            Printer.setTextColor(getResources().getColor(R.color.messageError));
        else
            Printer.setTextColor(Color.parseColor("#856404"));
        Network.setText(LI.InfoHTTP);
        if(!LI.InfoHTTP.equals("HTTP_OK"))
            Network.setTextColor(getResources().getColor(R.color.messageError));
        else
        Network.setTextColor(Color.parseColor("#856404"));
        String PercentData = (LI.AllScan==0?"100":Integer.toString ( 100*(LI.AllScan - LI.BadScan) / LI.AllScan));
        CountData.setText(Integer.toString (LI.BadScan) +"/"+ Integer.toString (LI.AllScan)+" ("+ PercentData + "%)");
        CountData.setTextColor(Color.parseColor("#856404"));

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
        if (MainActivity.emdkWrapper != null) {
            MainActivity.emdkWrapper.release();
        }
    }

}
