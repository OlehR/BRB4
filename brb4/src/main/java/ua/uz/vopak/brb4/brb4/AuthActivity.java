package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;

public class AuthActivity extends Activity  implements View.OnClickListener, ScanCallBack {
    private long backPressedTime;
    private Toast backToast;
    GlobalConfig config = GlobalConfig.instance();
    Button loginBtn;
    TextView nameStore;
    EditText login, password;
    CheckBox IsLoginCO;
    AuterizationsHelper aHelper=new AuterizationsHelper();
    final Activity activity = this;
    private Scaner scaner=config.GetScaner();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);

        loginBtn = findViewById(R.id.LoginButton);
        loginBtn.setOnClickListener(this);
        login = findViewById(R.id.Login);
        password = findViewById(R.id.Password);
        IsLoginCO = findViewById(R.id.L_IsCentral);
        if(config.Company!= eCompany.SevenEleven)
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
        //IntentIntegrator.forSupportFragment(this).setBeepEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Zebra
        scaner.StopScan();
    }

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
