package ua.uz.vopak.brb4.brb4;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;

import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ua.uz.vopak.brb4.brb4.enums.ePrinterError;
import ua.uz.vopak.brb4.brb4.enums.eTypeScaner;
import ua.uz.vopak.brb4.brb4.fragments.ScanFragment;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.IPostResult;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.models.LabelInfo;


public class PriceCheckerActivity extends FragmentActivity implements View.OnClickListener,ScanCallBack{
    private Scaner scaner;
    TextView codeView, perView, nameView, priceView, oldPriceView,oldPriceText,priceText,Printer,
            Network, CountData, NewPriceOpt, OldPriceOpt, Rest;
    EditText textBarcodeView;
    Button ChangePrintType,AddPrintBlock,ChangePrintColorType;
    LinearLayout optRow, PriceCheckerInfoLayout,priceCheckerLinearLayout;
    GlobalConfig config = GlobalConfig.instance();
    Spinner ChangePrintBlockNumber;
    Integer PrintType = 0,currentPrintBlock=1; //Колір чека 0-звичайнийб 1-жовтий

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
        priceCheckerLinearLayout = findViewById(R.id.priceCheckerLinearLayout);
        ChangePrintColorType = findViewById(R.id.ChangePrintColorType);
        ChangePrintBlockNumber = findViewById(R.id.ChangePrintBlockNumber);

        AddPrintBlock = findViewById(R.id.AddPrintBlock);
        AddPrintBlock.setOnClickListener(this);
        ChangePrintColorType.setOnClickListener(this);
        AddPrintBlock.setText(config.NumberPackege.toString());

        ProgressBar progresBar = findViewById(R.id.progressBar);
        config.SetProgressBar(progresBar);
        config.Worker.SetPriceCheckerActivity(this);
        config.Worker.ReInitBT();
        ChangePrintType.setText(config.Worker.LI.IsShort?"Коротка":"Стандартна");

        setScanResult(config.Worker.LI);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(true);

        scaner=config.GetScaner();
        scaner.Init(this,savedInstanceState);

        setSpinner();

        if(config.NumberPackege>0){
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    String val = config.Worker.GetConfigPair("currentPrintBlock");
                    if(!val.equals(""))
                    currentPrintBlock = Integer.parseInt(val);
                    ChangePrintBlockNumber.setSelection(currentPrintBlock-1);
                    return null;
                }
            }).execute();
        }

        ChangePrintBlockNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentPrintBlock = Integer.parseInt(ChangePrintBlockNumber.getSelectedItem().toString());
                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.AddConfigPair("currentPrintBlock",currentPrintBlock.toString());
                        return null;
                    }
                }).execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());

        if(keyCode.equals("66") && event.getAction() == KeyEvent.ACTION_UP){
            new AsyncHelper<LabelInfo>(
                    new IAsyncHelper<LabelInfo>() {
                        @Override
                        public LabelInfo Invoke() {
                            return config.Worker.Start(textBarcodeView.getText().toString());
                        }
                    },
                    new IPostResult<LabelInfo>() {
                        @Override
                        public void Invoke(LabelInfo parLI) {
                            config.Worker.priceCheckerActivity.setScanResult(parLI);
                        }
                    }).execute();
        }

        return super.dispatchKeyEvent(event);
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
                config.Worker.LI.IsShort=!config.Worker.LI.IsShort;
                ChangePrintType.setText(config.Worker.LI.IsShort?"Коротка":"Стандартна");
                break;
            case R.id.AddPrintBlock:
                config.NumberPackege++;
                DateFormat df = new SimpleDateFormat("yyyyMMdd");
                Date today = Calendar.getInstance().getTime();
                final String todayAsString = df.format(today);
                //new AsyncConfigPairAdd(worker).execute("NumberPackege",todayAsString+GlobalConfig.NumberPackege.toString());
                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.AddConfigPair("NumberPackege",todayAsString+config.NumberPackege.toString());
                        return null;
                    }
                }).execute();
                AddPrintBlock.setText(config.NumberPackege.toString());
                break;
            case R.id.ChangePrintColorType:
                ChangePrintColorType();
                break;
            case R.id.PrintBlock:
                config.Worker.printPackage(PrintType,currentPrintBlock);
                break;
        }

        if(config.TypeScaner==eTypeScaner.Camera) {
            BarcodeView barcodeView = findViewById(R.id.barcode_scanner);
            barcodeView.resume();

        }
    }

    private void setSpinner(){
        String[] path = new String[config.NumberPackege];
        Integer j=0;
        for(int i=0;i<config.NumberPackege;i++){
            j++;
            path[i] = j.toString();
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,path);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ChangePrintBlockNumber.setAdapter(adapter);
    }

    private void ChangePrintColorType(){
        if(ChangePrintColorType.getTag() == null){
            ChangePrintColorType.setTag("ChangePrintColorType");
            ChangePrintColorType.setText("Жовтий");
            PrintType = 0;
            setBgColor(priceCheckerLinearLayout,"#ffff00");
        }else {
            ChangePrintColorType.setTag(null);
            ChangePrintColorType.setText("Звичайний");
            PrintType = 1;
            setBgColor(priceCheckerLinearLayout,"#ffffff");
        }
    }

    private void setBgColor(ViewGroup vG, String color){
        for(int i=0;i<vG.getChildCount();i++){
            View v = vG.getChildAt(i);
            v.setBackgroundColor(Color.parseColor(color));
            if(v instanceof ViewGroup && !(v.getId() != R.id.scan_fragment) && ((ViewGroup)v).getChildCount() > 0 && !(v instanceof Button)){
                setBgColor((ViewGroup)v,color);
            }
        }
        return;
    }

    //We need to handle any incoming intents, so let override the onNewIntent method
    //Необхідно для Zebta  TC20 Оскільки повідомлення приходять саме так. !!!TMP Можливо перероблю через повідомлення
    @Override
    public void onNewIntent(Intent i) {
        config.GetScaner().handleDecodeData(i);

    }

    public void  setScanResult(LabelInfo LI){

        codeView.setText(Integer.toString(LI.Code));
        perView.setText(LI.Unit);
        nameView.setText(LI.Name);
        Printer.setText(LI.InfoPrinter);

        if( config.Worker.Printer.varPrinterError != ePrinterError.None) {
            Printer.setTextColor(getResources().getColor(R.color.messageError));
        }
        else {
            Printer.setTextColor(Color.parseColor("#856404"));
        }

        Network.setText(LI.InfoHTTP);
        if( config.Worker.Http.HttpState != eStateHTTP.HTTP_OK ) {
            Network.setTextColor(getResources().getColor(R.color.messageError));
        }
        else {
            Network.setTextColor(Color.parseColor("#856404"));
        }

        boolean isProblem = false;
        if( config.Worker.Printer.varPrinterError != ePrinterError.None || config.Worker.Http.HttpState != eStateHTTP.HTTP_OK){
            if(config.BarcodeImageLayout != null){
                Drawable dw = config.BarcodeImageLayout.getBackground();
                dw.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
                isProblem = true;
            }
        }else{
            if(config.BarcodeImageLayout != null){
                Drawable dw = config.BarcodeImageLayout.getBackground();
                dw.clearColorFilter();
            }
        }

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

        if(LI.OldPriceOpt != LI.PriceOpt){

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

        if(LI.ActionType == 2){
            if(config.BarcodeImageLayout != null && !isProblem){
                Drawable dw = config.BarcodeImageLayout.getBackground();
                dw.clearColorFilter();
                dw.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
            }
        }else{
            if(config.BarcodeImageLayout != null && !isProblem){
                Drawable dw = config.BarcodeImageLayout.getBackground();
                dw.clearColorFilter();
            }
        }

        oldPriceView.setText(String.format("%.2f",(double)LI.OldPrice/100));
        priceView.setText(String.format("%.2f",(double)LI.Price/100));
        textBarcodeView.setText(LI.BarCode);

        Rest.setText(String.format("%.2f",LI.Rest));


        if(config.TypeScaner==eTypeScaner.Camera) {
            BarcodeView barcodeView = (BarcodeView) findViewById(R.id.barcode_scanner);
            barcodeView.resume();
        }
    }


    public void SetProgres(int progres){
        ProgressBar progresBar = findViewById(R.id.progressBar);
        progresBar.setProgress(progres);
    }

    public void ExecuteWorker(final String parBarCode){
        //AsyncWorker aW =  new AsyncWorker(worker);
        //aW.execute(parBarCode);

        new AsyncHelper<LabelInfo>(
                new IAsyncHelper<LabelInfo>() {
                    @Override
                    public LabelInfo Invoke() {
                        return config.Worker.Start(parBarCode);
                    }
                },
                new IPostResult<LabelInfo>() {
                    @Override
                    public void Invoke(LabelInfo parLI) {
                        config.Worker.priceCheckerActivity.setScanResult(parLI);
                    }
                }).execute();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }



}
