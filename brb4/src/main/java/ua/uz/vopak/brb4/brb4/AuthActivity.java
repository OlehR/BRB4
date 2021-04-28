package ua.uz.vopak.brb4.brb4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.databinding.AuthLayoutBinding;
import ua.uz.vopak.brb4.brb4.databinding.DocumentScannerActivityBinding;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.models.AuthModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class AuthActivity extends FragmentActivity implements ScanCallBack {
    private long backPressedTime;
    private Toast backToast;
    GlobalConfig config = GlobalConfig.instance();

    TextView nameStore;
    EditText login, password;
    public BarcodeView barcodeView;

    AuterizationsHelper aHelper=new AuterizationsHelper();
    final Activity activity = this;
    private Scaner scaner=config.GetScaner();

    AuthModel authModel= new AuthModel(this);
    public AuthLayoutBinding binding;
    // Калбек штрихкода з камери.
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.pause();
                Run(result.getText());
            }
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);

        login = findViewById(R.id.Login);
        password = findViewById(R.id.Password);

        nameStore = findViewById(R.id.L_NameStore);
        nameStore.setText(config.Company.GetName());

        binding = DataBindingUtil.setContentView(this, R.layout.auth_layout);
        barcodeView=findViewById(R.id.A_scanner);
        barcodeView.setCameraSettings(config.GetCameraSettings());

        binding.setAM (authModel);
        binding.invalidateAll();

        if(config.IsUseCamera()) {
                barcodeView.decodeContinuous(callback);
                barcodeView.resume();
        }

        scaner.Init(this,savedInstanceState);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    authModel.onClickLogin();
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Zebra
        scaner.StartScan();

        if(config.IsUseCamera())
            barcodeView.resume();

        //IntentIntegrator.forSupportFragment(this).setBeepEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Zebra
        scaner.StopScan();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());

        if(keyCode.equals("66") && event.getAction() == KeyEvent.ACTION_UP){
            authModel.onClickLogin();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void Run(final String pBarCode) {
        if(pBarCode==null )
            return;
        if(pBarCode.length()>=6 && pBarCode.substring(0,6).equals("Conf=>") ) {
            SetConfig(pBarCode);
            new AlertDialog.Builder(this)
                    .setTitle("Налаштування Прийнято")
                    .setMessage("Для завершення програми натисніть ОК?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Exit();
                        }
                    }).create().show();
            return;
        }

        final String LP[] = pBarCode.split(",");
        if (LP.length > 1) {
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    aHelper.Login(activity, LP[0], LP[1],config.IsLoginCO,true);
                    return null;
                }
            }).execute();
        } else {
            login.setText(LP[0]);
            SetFocusPassWord();
        }
    }

    private void SetConfig(String pBarCode)    {
        pBarCode=pBarCode.substring(6);
        String[]  par=pBarCode.split(" ");
        for ( String el:par) {
            String[]  El=el.split("=");
            if(El.length==2)
            {
                switch(El[0])
                {
                    case "Company":
                        config.Company= eCompany.fromOrdinal(Integer.valueOf(El[1]));
                        config.Worker.AddConfigPair("Company", Integer.toString(config.Company.getAction()));
                        break;
                    case "Warehouse":
                        config.CodeWarehouse= Integer.valueOf(El[1]);
                        config.Worker.AddConfigPair("Warehouse", Integer.toString(config.CodeWarehouse));
                        break;
                    case "Url":
                        config.ApiUrl= El[1];
                        config.Worker.AddConfigPair("ApiUrl", config.ApiUrl);

                        break;
                    case "URLadd":
                        config.ApiURLadd = El[1];
                        config.Worker.AddConfigPair("ApiUrladd", config.ApiURLadd);
                        break;
                    case "AutoLogin":
                        config.IsAutoLogin = El[1].equals("1");
                        config.Worker.AddConfigPair("IsAutoLogin",config.IsAutoLogin?"true":"false");
                        break;
                    case "Printer":
                    config.TypeUsePrinter = eTypeUsePrinter.fromOrdinal(Integer.valueOf(El[1]));
                    config.Worker.AddConfigPair("connectionPrinterType", config.TypeUsePrinter.GetStrCode());
                }
            }
        }
    };

    private void SetFocusPassWord()
    {
        password.requestFocus();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime+2000 > System.currentTimeMillis()){
            backToast.cancel();
            Exit();
        }else {
            backToast = Toast.makeText(getBaseContext(), "Натисніть ще раз, щоб вийти", Toast.LENGTH_SHORT);
            backToast.show();
            backPressedTime = System.currentTimeMillis();
        }
    }

    void Exit()  {
        config.isAutorized = false;
        super.onBackPressed();
 //       finish();
        moveTaskToBack(true);
        finishAndRemoveTask();
    }
}
