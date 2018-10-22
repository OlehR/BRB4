package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncAuthHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class AuthActivity extends Activity  implements View.OnClickListener {
    GlobalConfig config = GlobalConfig.instance();
    Button loginBtn;
    EditText login, password;
    AuterizationsHelper aHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_layout);

        loginBtn = findViewById(R.id.LoginButton);
        loginBtn.setOnClickListener(this);
        login = findViewById(R.id.Login);
        password = findViewById(R.id.Password);
        aHelper = new AuterizationsHelper(this);
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
                String data = "{"+ CodeData +", "+ Login +", "+ PassWord +"}";

                new AsyncAuthHelper(aHelper).execute(data);

                if(aHelper.isAutorized){
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
                        //SomeActivity - имя класса Activity для которой переопределяем onBackPressed();
                        aHelper.isAutorized = false;
                        finish();
                        moveTaskToBack(true);
                    }
                }).create().show();
    }
}
