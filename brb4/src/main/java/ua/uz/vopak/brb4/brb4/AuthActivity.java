package ua.uz.vopak.brb4.brb4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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

import androidx.fragment.app.FragmentActivity;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class AuthActivity extends FragmentActivity implements View.OnClickListener, ScanCallBack {
    private long backPressedTime;
    private Toast backToast;
    GlobalConfig config = GlobalConfig.instance();
    Worker worker = config.GetWorker();
    Button loginBtn;
    TextView nameStore;
    EditText login, password;
    BarcodeView barcodeView;
    CheckBox IsLoginCO;
    AuterizationsHelper aHelper=new AuterizationsHelper();
    final Activity activity = this;
    private Scaner scaner=config.GetScaner();
    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;

    // Калбек штрихкода з камери.
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                //barcodeView.pause();
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
        setContentView(R.layout.auth_layout);

        loginBtn = findViewById(R.id.LoginButton);
        loginBtn.setOnClickListener(this);
        login = findViewById(R.id.Login);
        password = findViewById(R.id.Password);
        IsLoginCO = findViewById(R.id.L_IsCentral);
        if(config.Company!= eCompany.Sim23)
            IsLoginCO.setVisibility(View.GONE);
        nameStore = findViewById(R.id.NameStore);
        nameStore.setText(config.Company.GetName());
        /*if (config.Company.getAction() == 1)
            nameStore.setText("Вопак");
        else if (config.Company.getAction() == 2)
            nameStore.setText("Spar");
        else if (config.Company.getAction() == 3)
            nameStore.setText("SevenEleven");
        else nameStore.setText("Зверніться до адміністратора");*/
        if(config.IsDebug)
            password.setText(config.Password);
        login.setText(config.Login);
        IsLoginCO.setChecked(config.IsLoginCO);

        barcodeView=findViewById(R.id.A_scanner);
        barcodeView.setCameraSettings(config.GetCameraSettings());

        if(config.IsUseCamera()) {
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
        }

        scaner.Init(this,savedInstanceState);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginBtn.callOnClick();
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
            loginBtn.callOnClick();
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.LoginButton:
                final String Login = login.getText().toString();
                final String PassWord = password.getText().toString();
                final boolean LoginCO =IsLoginCO.isChecked();
                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        aHelper.Login(activity,Login,PassWord,LoginCO,true);
                        return null;
                    }
                }).execute();
                break;
        }

    }

    @Override
    public void Run(final String pBarCode) {

        if(pBarCode==null )
            return;
        if(pBarCode.length()>=6 && pBarCode.substring(0,6).equals("Conf=>") )
            SetConfig(pBarCode);


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

    private void SetConfig(String pBarCode)
    {
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
                        worker.AddConfigPair("Company", Integer.toString(config.Company.getAction()));
                        break;
                    case "Warehouse":
                        config.CodeWarehouse= Integer.valueOf(El[1]);
                        worker.AddConfigPair("Warehouse", Integer.toString(config.CodeWarehouse));
                        break;
                    case "Url":
                        config.ApiUrl= El[1];
                        worker.AddConfigPair("ApiUrl", config.ApiUrl);

                        break;
                    case "URLadd":
                        config.ApiURLadd = El[1];
                        worker.AddConfigPair("ApiUrladd", config.ApiURLadd);
                        break;
                    case "AutoLogin":
                        config.IsAutoLogin = El[1].equals("1");
                        worker.AddConfigPair("IsAutoLogin",config.IsAutoLogin?"true":"false");
                        break;
                    case "Printer":
                    config.TypeUsePrinter = eTypeUsePrinter.fromOrdinal(Integer.valueOf(El[1]));
                    config.Worker.AddConfigPair("connectionPrinterType", config.TypeUsePrinter.GetStrCode());

                }
            }
        }
        finishAndRemoveTask();
    };



    private void SetFocusPassWord()
    {
        password.requestFocus();
    }

//    @Override
//    public void onBackPressed() {
//        new AlertDialog.Builder(this)
//                .setTitle("Вийти з програми?")
//                .setMessage("Ви справді хочете вийти?")
//                .setNegativeButton(android.R.string.no, null)
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        config.isAutorized = false;
//                        finish();
//                        moveTaskToBack(true);
//                    }
//                }).create().show();
//    }
    @Override
    public void onBackPressed() {

        if (backPressedTime+2000 > System.currentTimeMillis()){
            backToast.cancel();
            config.isAutorized = false;
            super.onBackPressed();
            return;
        }else {
            backToast = Toast.makeText(getBaseContext(), "Натисніть ще раз, щоб вийти", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
