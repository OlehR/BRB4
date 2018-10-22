package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncWareHauseHelper;
import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;
import ua.uz.vopak.brb4.brb4.helpers.EMDKWrapper;
import ua.uz.vopak.brb4.brb4.helpers.WareHauseHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class  MainActivity extends AppCompatActivity implements View.OnClickListener {
    GlobalConfig config = GlobalConfig.instance();
    public  static Boolean isCreatedScaner = false;
    public static EMDKWrapper emdkWrapper = null;
    Button[] menuItems = new Button[4];
    int current = 0;
    AuterizationsHelper auth;
    AsyncWareHauseHelper wares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String model = android.os.Build.MODEL;
        if( model.equals("TC20")  && ( android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions")) ){
            emdkWrapper  = new EMDKWrapper(getApplicationContext());
        }

        if(emdkWrapper != null){
            isCreatedScaner=emdkWrapper.getEMDKManager(savedInstanceState);
        }

        auth = new AuterizationsHelper();
        wares = new AsyncWareHauseHelper(new WareHauseHelper());
        wares.execute();

        if(!auth.isAutorized){
            Intent i = new Intent(this, AuthActivity.class);
            startActivity(i);
        }

        setContentView(R.layout.main_layout);

        menuItems[0] = findViewById(R.id.PriceCheker);
        menuItems[1] = findViewById(R.id.Revision);
        menuItems[2] = findViewById(R.id.Incom);
        menuItems[3] = findViewById(R.id.Settings);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        for(int i = 0; i < menuItems.length; i++){
            menuItems[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub
                    if (hasFocus) {
                        ((Button)v).setTextColor(getResources().getColor(android.R.color.white));
                    }else {
                        ((Button)v).setTextColor(getResources().getColor(android.R.color.black));
                    }
                }

            });

            menuItems[i].setOnClickListener(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!auth.isAutorized){
            Intent i = new Intent(this, AuthActivity.class);
            startActivity(i);
        }
    }

    /*@Override
    protected void onPause() {
        super.onPause();
        auth.isAutorized = false;
    }*/

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());

        if((keyCode.equals("19") || keyCode.equals("20")) && event.getAction() == KeyEvent.ACTION_UP){

            if(current > 0 && keyCode.equals("19") && menuItems[current-1].isEnabled()){
                current--;
            }

            if(current < 3 && keyCode.equals("20") && menuItems[current+1].isEnabled()){
                current++;
            }
        }

        if(keyCode.equals("285") && event.getAction() == KeyEvent.ACTION_UP){
            menuItems[current].callOnClick();
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.PriceCheker:
                Intent intent = new Intent(this, PriceCheckerActivity.class);
                startActivity(intent);
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
                        auth.isAutorized = false;
                        finish();
                        moveTaskToBack(true);
                    }
                }).create().show();
    }
}