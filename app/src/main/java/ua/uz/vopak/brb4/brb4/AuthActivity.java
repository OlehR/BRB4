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
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class AuthActivity extends Activity  implements View.OnClickListener {
    GlobalConfig config = GlobalConfig.instance();
    Button loginBtn;
    EditText login, password;
    AuterizationsHelper aHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Ініціалізація BD
        Context c =this.getApplicationContext();
        config.Init(c);

        setContentView(R.layout.auth_layout);

        loginBtn = findViewById(R.id.LoginButton);
        loginBtn.setOnClickListener(this);
        login = findViewById(R.id.Login);
        password = findViewById(R.id.Password);
        aHelper = new AuterizationsHelper(this);
        if(config.IsDebug)
            password.setText(config.Password);
        //new AsyncLastLogin(aHelper).execute();

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                aHelper.GetLastLogin();
                return null;
            }
        }).execute();

        //new AsyncGetWarehouseConfig().execute();

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {

                config.CodeWarehouse = config.Worker.GetConfigPair("Warehouse");
                if(config.GetWorker().LI!=null)
                  config.GetWorker().LI.SetTypeShop(config.isSPAR());
                return null;

            }
        }).execute();

        //new AsyncGetNumberPackege().execute();

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                DateFormat df = new SimpleDateFormat("yyyyMMdd");
                Date today = Calendar.getInstance().getTime();
                String todayAsString = df.format(today);

                String var  = config.Worker.GetConfigPair("NumberPackege");
                String varNumberPackege="1";
                if(var.length()>8 && var.substring(0,8).equals(todayAsString))
                {
                    varNumberPackege = var.substring(8);
                }
                else
                    config.Worker.AddConfigPair("NumberPackege",todayAsString+ varNumberPackege);

                config.NumberPackege=Integer.valueOf(varNumberPackege);
                return null;
            }
        }).execute();

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
                config.Login = login.getText().toString();
                config.Password = password.getText().toString();
                String CodeData = "\"CodeData\": \"1\"";
                String Login = "\"Login\": \"" + config.Login + "\"";
                String PassWord = "\"PassWord\": \"" + config.Password + "\"";
                final String data = "{"+ CodeData +", "+ Login +", "+ PassWord +"}";

                //new AsyncAuthHelper(aHelper).execute(data);

                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        aHelper.Start(data);
                        return null;
                    }
                }).execute();

                if(config.isAutorized){
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }
                break;
        }

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Выйти из приложения?")
                .setMessage("Вы действительно хотите выйти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        config.isAutorized = false;
                        finish();
                        moveTaskToBack(true);
                    }
                }).create().show();
    }

    public void setLogin(final String LastLogin) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EditText edit = (EditText) findViewById(R.id.Login);
                        EditText editpass = (EditText) findViewById(R.id.Password);
                        edit.setText(LastLogin);
                        //editpass.requestFocus();
                        editpass.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                        editpass.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                    }
                }, 100);
            }
        });
    }
}
