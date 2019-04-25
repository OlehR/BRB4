package ua.uz.vopak.brb4.brb4;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncWorker;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.models.LabelInfo;


public class PriceCheckerActivity extends FragmentActivity implements View.OnClickListener,ScanCallBack{
    private Worker worker;
    private Scaner scaner;
    TextView codeView, textBarcodeView, perView, nameView, priceView, oldPriceView,oldPriceText,priceText,Printer,
            Network, CountData, NewPriceOpt, OldPriceOpt, Rest;
    Button ChangePrintType,AddPrintBlock;
    LinearLayout optRow, PriceCheckerInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.price_checker_layout_new);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
        NewPriceOpt = findViewById(R.id.price_opt );
        OldPriceOpt = findViewById(R.id.old_price_opt );
        optRow = findViewById(R.id.tableRowOpt);
        ChangePrintType.setOnClickListener(this);
        Rest = findViewById(R.id.rest);
        PriceCheckerInfoLayout = findViewById(R.id.PricecheckerInfoLayout);

        AddPrintBlock = findViewById(R.id.AddPrintBlock);
        AddPrintBlock.setOnClickListener(this);
        AddPrintBlock.setText(GlobalConfig.NumberPackege.toString());

        ProgressBar progresBar = findViewById(R.id.progressBar);
        worker = GlobalConfig.GetWorker(progresBar);
        worker.SetPriceCheckerActivity(this);
        worker.ReInitBT();
        ChangePrintType.setText(worker.LI.IsShort?"Коротка":"Стандартна");

        setScanResult(worker.LI);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(true);

        scaner=GlobalConfig.GetScaner();
        scaner.Init(this,savedInstanceState);

    }

    @Override
        public void Run(String parBarCode) {
            ExecuteWorker(parBarCode);
        }


    @Override
    protected void onResume() {
        super.onResume();
    }



    @Override
    public void onClick(View v) {
      int Id= v.getId();
        switch (Id){
            case R.id.ChangePrintType:
                worker.LI.IsShort=!worker.LI.IsShort;
                ChangePrintType.setText(worker.LI.IsShort?"Коротка":"Стандартна");
                break;
            case R.id.AddPrintBlock:
                GlobalConfig.NumberPackege++;
                AddPrintBlock.setText(GlobalConfig.NumberPackege.toString());
        }

        if(GlobalConfig.TypeScaner==eTypeScaner.Camera) {
            BarcodeView barcodeView = findViewById(R.id.barcode_scanner);
            barcodeView.resume();

        }
    }

    //We need to handle any incoming intents, so let override the onNewIntent method
    //Необхідно для Zebta  TC20 Оскільки повідомлення приходять саме так. !!!TMP Можливо перероблю через повідомлення
    @Override
    public void onNewIntent(Intent i) {
        GlobalConfig.GetScaner().handleDecodeData(i);

    }

    public void  setScanResult(LabelInfo LI){

        codeView.setText(Integer.toString(LI.Code));
        perView.setText(LI.Unit);
        nameView.setText(LI.Name);
        Printer.setText(LI.InfoPrinter);

        if(LI.InfoPrinter.equals("CanNotOpen")) {
            Printer.setTextColor(getResources().getColor(R.color.messageError));
            //Drawable dw = BarcodeImage.getBackground();
            //dw.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
        }
        else {
            Printer.setTextColor(Color.parseColor("#856404"));
        }

        Network.setText(LI.InfoHTTP);
        if(!LI.InfoHTTP.equals("HTTP_OK"))
            Network.setTextColor(getResources().getColor(R.color.messageError));
        else
        Network.setTextColor(Color.parseColor("#856404"));
        String PercentData = (LI.AllScan==0?"100":Integer.toString ( 100*(LI.AllScan - LI.BadScan) / LI.AllScan));
        CountData.setText(Integer.toString (LI.BadScan) +"/"+ Integer.toString (LI.AllScan)+" ("+ PercentData + "%)");
        CountData.setTextColor(Color.parseColor("#856404"));

        if(LI.OldPrice != LI.Price){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
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

        if(LI.OldPriceOpt != LI.PriceOpt){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
            OldPriceOpt.setTextColor(Color.parseColor("#ee4343"));
            NewPriceOpt.setTextColor(Color.parseColor("#ee4343"));
            OldPriceOpt.setTextColor(Color.parseColor("#ee4343"));
            NewPriceOpt.setTextColor(Color.parseColor("#ee4343"));
        }else {
            OldPriceOpt.setTextColor(Color.parseColor("#3bb46e"));
            NewPriceOpt.setTextColor(Color.parseColor("#3bb46e"));
            OldPriceOpt.setTextColor(Color.parseColor("#3bb46e"));
            NewPriceOpt.setTextColor(Color.parseColor("#3bb46e"));
        }

        if(LI.OldPriceOpt != 0 || LI.PriceOpt != 0){
            OldPriceOpt.setText(String.format("%.2f",(double)LI.OldPriceOpt/100));
            NewPriceOpt.setText(String.format("%.2f",(double)LI.PriceOpt/100));
            optRow.setVisibility(View.VISIBLE);
        }else{
            optRow.setVisibility(View.INVISIBLE);
        }

        if(LI.Action){
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(1500);
            }
        }

        oldPriceView.setText(String.format("%.2f",(double)LI.OldPrice/100));
        priceView.setText(String.format("%.2f",(double)LI.Price/100));
        textBarcodeView.setText(LI.BarCode);

        Rest.setText(String.format("%.2f",LI.Rest));


        if(GlobalConfig.TypeScaner==eTypeScaner.Camera) {
            BarcodeView barcodeView = (BarcodeView) findViewById(R.id.barcode_scanner);
            barcodeView.resume();
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
    }

}
