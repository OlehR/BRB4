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

import android.view.KeyEvent;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.DecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import android.content.Intent;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.BL_PriceChecker;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.brb4.databinding.PriceCheckerLayoutNewBinding;
//import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class PriceCheckerActivity extends FragmentActivity implements ScanCallBack{
    //private ZXingScannerView mScannerView;
    Context context;
    private Scaner scaner;
    ua.uz.vopak.brb4.lib.helpers.UtilsUI UtilsUI = new UtilsUI(this);
    int SizeDeque=2;
    ArrayDeque<String> BarCodeQueue = new ArrayDeque<>();

    GlobalConfig config = GlobalConfig.instance();

    private LabelInfo LI= new LabelInfo(config);
    public BL_PriceChecker BL= new BL_PriceChecker(LI);

    HandlerPC HandlerPC=new HandlerPC(this);

    EditText textBarcodeView;
    EditText NumberOfReplenishment;
    public View BarcodeImageLayout;
    BarcodeView barcodeView;

    //LinearLayout PriceCheckerInfoLayout;

   // public RelativeLayout loader;
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

        textBarcodeView = findViewById(R.id.PCh_bar_code);
        NumberOfReplenishment = findViewById(R.id.PCh_NumberOfReplenishment);
        //mScannerView= new ZXingScannerView(this);

        //PriceCheckerInfoLayout = findViewById(R.id.PricecheckerInfoLayout);
        //ChangePrintBlockNumber = findViewById(R.id.ChangePrintBlockNumber);

        //loader = findViewById(R.id.PCh_Loader);

        BL.Init(LI,this);
        BL.ReInitBT();

        binding = DataBindingUtil.setContentView(this, R.layout.price_checker_layout_new);
        binding.setLI (LI);
        binding.setHandler(HandlerPC);

        BarcodeImageLayout = findViewById(R.id.PCh_BarcodeImageLayout);

        barcodeView = findViewById(R.id.PCh_barcode_scanner);
        barcodeView.setCameraSettings(config.GetCameraSettings());


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
        HandlerPC.OnClickFlashLite();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());
      /*  if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int KeyCode= Integer.valueOf(keyCode);
            if (KeyCode >= 7 && KeyCode < 16)
                if (!LI.isEdit) {
                    LI.isEdit = true;
                    LI.NumberOfReplenishment.set("");
                    binding.invalidateAll();
                }
        }*/
        if (event.getAction() == KeyEvent.ACTION_UP) {

            switch (keyCode)
            {
                case "66"://Enter
                    if(LI.InputFocus.get()==1) {
                        FindWares(LI.BarCode.get(), true);
                        LI.InputFocus.set(0);
                    }
                    else
                        if(LI.InputFocus.get()==2) {
                            LI.InputFocus.set(0);
                           final double inReplenishment= Double.valueOf(LI.NumberOfReplenishment.get());
                           if(inReplenishment>LI.Rest) {
                               LI.NumberOfReplenishment.set("");
                               UtilsUI.Dialog("Невірний ввід даних","На залишку=>"+ LI.Rest+" Ввели=>"+inReplenishment);
                           }


                            new AsyncHelper<Void>(
                                    new IAsyncHelper<Boolean>() {
                                        @Override
                                        public Boolean Invoke() {
                                            BL.SaveReplenishment(inReplenishment);
                                            return true;
                                        }
                                    },
                                    new IPostResult<Boolean>() {
                                        @Override
                                        public void Invoke(Boolean p) {
                                            LI.NumberOfReplenishment.set("");
                                            return;
                                        }
                                    }).execute();
                        }

                    break;
                case "131"://F1
                    break;
                case "132"://F2
                    LI.InputFocus.set(LI.InputFocus.get()==1?2:1);
                     break;
                case "133"://F3
                    LI.InputFocus.set(1);
                    break;
                case "134"://F4
                    if(config.Company== eCompany.Sim23)
                        LI.ChangeOnLineState();
                    break;
                case "135"://F5
                    LI.ChangeMultyLabel();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override //Приходить штрихкод.
    public void Run(String parBarCode) {
        // Не прийшов штрихкод
        if(parBarCode==null || parBarCode.isEmpty() )
            return;

        //Якщо в черзі вже є такий штрихкод ігноруємо.
         if(parBarCode.equals(BarCodeQueue.peekFirst()) || parBarCode.equals(BarCodeQueue.peekLast()))
             return;
         //Якщо черга задовга
         if(BarCodeQueue.size()>SizeDeque)
            return;

          BarCodeQueue.offer(parBarCode);
          if(BarCodeQueue.size()==1)
                 FindWares(parBarCode, false);
          else
             return ;

        }

    @Override
    public void onResume() {
        super.onResume();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null; // Usually back camera is at 0 position.
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, false);

                // TODO  Turn ON  Flash Light On
                mScannerView.setFlash(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        //Camera
        //barcodeView.setTorch(true);
        if(config.TypeScaner==eTypeScaner.Camera)
             barcodeView.resume();
        //Zebra
        scaner.StartScan();
       // ManualScan();
    }

    public void ManualScan()
    {
        final String ACTION = "com.symbol.datawedge.api.ACTION";
        final String  SOFT_SCAN_TRIGGER = "com.symbol.datawedge.api.SOFT_SCAN_TRIGGER";
        final String START_SCANNING = "START_SCANNING";
        Intent i = new Intent();
        i.setAction(ACTION);
        i.putExtra(SOFT_SCAN_TRIGGER, START_SCANNING);
        sendBroadcast(i);

    }

    @Override
    public void onPause() {
        super.onPause();
        //Camera
        if(config.TypeScaner==eTypeScaner.Camera)
            barcodeView.pause();
        //Zebra
        scaner.StopScan();

    }

    public void LoadSpinner() {
        new AsyncHelper<HashMap<String, String[]>>(
                new IAsyncHelper<HashMap<String, String[]>>() {
                    @Override
                    public HashMap<String, String[]> Invoke() {
                        HashMap<String, String[]> counts = BL.getPrintBlockItemsCount();
                        return counts;
                    }
                }
                ,
                new IPostResult<HashMap<String, String[]>>() {
                    @Override
                    public void Invoke(final HashMap<String, String[]> counts) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                LI.ListPackege.clear();
                                for (int i = 1; i <= config.NumberPackege; i++) {
                                    if (counts.get(String.valueOf(i)) != null) {
                                        LI.ListPackege.add(i + "-" + counts.get(String.valueOf(i))[0] + "/" + counts.get(String.valueOf(i))[1]);
                                    }
                                    else {
                                        //  LI.ListPackege.add(i + "-" +  "0/0" );
                                    }
                                    LI.ListPackegeIdx.set(LI.ListPackege.size()-1);
                                }


                            }
                        });
                    }}
                ).execute();
    }

    public void  setScanResult(LabelInfo LI) {

        LI.InputFocus.set(LI.IsViewReplenishment()?2:1);
        if(LI.Code==0&&LI.InputFocus.get()==2)
            LI.InputFocus.set(1);

        binding.invalidateAll();
        LI.SetListPackege();

        if (BarcodeImageLayout != null) {
            Drawable dw = BarcodeImageLayout.getBackground();
            if (BL.Printer.varPrinterError != ePrinterError.None || LI.HttpState != eStateHTTP.HTTP_OK) {
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
        //очищаємо чергу штрихкодів від
        BarCodeQueue.poll();
        if(BarCodeQueue.size()>0)
            FindWares(BarCodeQueue.peek(),false);

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
