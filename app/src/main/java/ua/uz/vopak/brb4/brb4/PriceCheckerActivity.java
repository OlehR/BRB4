package ua.uz.vopak.brb4.brb4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;

import android.view.KeyEvent;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.BL_PriceChecker;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.brb4.databinding.PriceCheckerLayoutNewBinding;

public class PriceCheckerActivity extends FragmentActivity implements ScanCallBack{
    Context context;
    private Scaner scaner;

    GlobalConfig config = GlobalConfig.instance();

    private LabelInfo LI= new LabelInfo(config);
    public BL_PriceChecker BL= new BL_PriceChecker(LI);

    HandlerPC HandlerPC=new HandlerPC(this);

    EditText textBarcodeView;
    public View BarcodeImageLayout;
    BarcodeView barcodeView;

    LinearLayout PriceCheckerInfoLayout;

    public RelativeLayout loader;
    PriceCheckerLayoutNewBinding binding;
    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;


// Калбек штрихкода з камери.
private BarcodeCallback callback = new BarcodeCallback() {
    @Override
    public void barcodeResult(BarcodeResult result) {
        if (result.getText() != null) {
            barcodeView.pause();
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 1000);
            toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,250);
            Run(result.getText());//config.Scaner.CallBack.
        }
    }
    @Override
    public void possibleResultPoints(List<ResultPoint> resultPoints) {
    }
};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        LoadSpinner();
        setContentView(R.layout.price_checker_layout_new);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textBarcodeView = findViewById(R.id.bar_code);

        PriceCheckerInfoLayout = findViewById(R.id.PricecheckerInfoLayout);


        //ChangePrintBlockNumber = findViewById(R.id.ChangePrintBlockNumber);

        loader = findViewById(R.id.RevisionLoader);

        BL.Init(LI,this);
        BL.ReInitBT();

        binding = DataBindingUtil.setContentView(this, R.layout.price_checker_layout_new);
        binding.setLI (LI);
        binding.setHandler(HandlerPC);

        BarcodeImageLayout = findViewById(R.id.BarcodeImageLayout_);
        barcodeView = findViewById(R.id.barcode_scanner_);

        if(config.TypeScaner==eTypeScaner.Camera) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_ACCESS_CAMERA);
            } else {
                //barcodeView.setVisibility(View.VISIBLE);
                barcodeView.decodeContinuous(callback);
                barcodeView.resume();
            }
        }else{
            //barcodeView.setVisibility(View.INVISIBLE);
        }

        setScanResult(LI);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(true);

        //Для отримання штрихкодів
        scaner=config.GetScaner();
        scaner.Init(this,savedInstanceState);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode)
            {
                case "66"://Enter
                    if(LI.InputFocus.get()==1)
                        FindWares(textBarcodeView.getText().toString(), false);
                    else
                        if(LI.InputFocus.get()==2)
                            new AsyncHelper<Void>(
                                    new IAsyncHelper<Void>() {
                                        @Override
                                        public Void Invoke() {
                                            BL.SaveReplenishment(Double.valueOf(LI.NumberOfReplenishment.get()) );
                                            return null;
                                        }
                                    }).execute();


                    break;
                case "131":
                case "132"://F2
                    LI.InputFocus.set(LI.InputFocus.get()==1?2:1);
                     break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override //Приходить штрихкод.
        public void Run(String parBarCode) {
            FindWares(parBarCode,false);
        }


    /*@Override
    protected void onResume() {
        super.onResume();
    }*/

    @Override
    public void onResume() {
        super.onResume();
        if(config.TypeScaner==eTypeScaner.Camera)
             barcodeView.resume();
        //IntentIntegrator.forSupportFragment(this).setBeepEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(config.TypeScaner==eTypeScaner.Camera)
            barcodeView.pause();

    }

    public void LoadSpinner() {
        new AsyncHelper<Void>(
                new IAsyncHelper<Void>() {
                    @Override
                    public Void Invoke() {

                        LI.ListPackege.clear();
                        HashMap<String, String[]> counts = BL.getPrintBlockItemsCount();
                        for (int i = 1; i <= config.NumberPackege; i++) {
                            if (counts.get(String.valueOf(i)) != null) {
                                LI.ListPackege.add(i + "-" + counts.get(String.valueOf(i))[0] + "/" + counts.get(String.valueOf(i))[1]);
                            }
                            else {
                              //  LI.ListPackege.add(i + "-" +  "0/0" );
                            }
                            LI.ListPackegeIdx.set(LI.ListPackege.size()-1);
                        }
                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                ArrayAdapter adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, fPath);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                ChangePrintBlockNumber.setAdapter(adapter);
                            }
                        });*/
                        return null;
                    }
                }).execute();
    }


    //We need to handle any incoming intents, so let override the onNewIntent method
    //Необхідно для Zebta  TC20 Оскільки повідомлення приходять саме так. !!!TMP Можливо перероблю через повідомлення
    @Override
    public void onNewIntent(Intent i) {
        config.GetScaner().handleDecodeData(i);
    }

    public void  setScanResult(LabelInfo LI) {
        binding.invalidateAll();
        LI.SetListPackege();

        if (BarcodeImageLayout != null) {
            Drawable dw = BarcodeImageLayout.getBackground();
            if (BL.Printer.varPrinterError != ePrinterError.None || config.Worker.Http.HttpState != eStateHTTP.HTTP_OK) {
                dw.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY);
            } else {
                if (LI.Action())
                   dw.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                else
                dw.clearColorFilter();
            }
        }

         if (config.TypeScaner == eTypeScaner.Camera) {
            if(barcodeView!=null)
                barcodeView.resume();
        }
    }

    public void FindWares(final String parBarCode, final boolean isHandInput){
        LI.Clear();
        new AsyncHelper<LabelInfo>(
                new IAsyncHelper<LabelInfo>() {
                    @Override
                    public LabelInfo Invoke() {
                        LabelInfo LI = BL.Start(parBarCode, isHandInput);
                        //setSpinner();
                        return LI;
                    }
                },
                new IPostResult<LabelInfo>() {
                    @Override
                    public void Invoke(LabelInfo parLI) {
                        setScanResult(parLI);
                    }
                }).execute();
    }

    @Override
    protected void onDestroy() {
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
               BL.SendLogPrice();
                return null;
            }
        }).execute();
        // TODO Auto-generated method stub
        super.onDestroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(config.TypeScaner==eTypeScaner.Camera) {
            if (requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    barcodeView.setVisibility(View.VISIBLE);
                    barcodeView.decodeContinuous(callback);
                }
            }
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }
    }

}
