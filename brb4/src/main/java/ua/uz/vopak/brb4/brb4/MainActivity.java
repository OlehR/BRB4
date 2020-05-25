package ua.uz.vopak.brb4.brb4;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class  MainActivity extends AppCompatActivity implements View.OnClickListener, ScanCallBack {
    static GlobalConfig config = GlobalConfig.instance();
    public RelativeLayout loader;
    Button[] menuItems = new Button[8];
    int current = 0;
    //AuterizationsHelper auth;
    static boolean isFirstRun = true;
    boolean isReload = false;
    private Scaner scaner;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        //auth = new AuterizationsHelper();
/*
        if(!config.isAutorized){

        }*/

        //ініціалізація класа при старті.
        Utils.instance(context);
        config.Init(context);


        setContentView(R.layout.main_layout);
        loader = findViewById(R.id.RevisionLoader);

        Intent in = getIntent();
        isReload = Boolean.parseBoolean(in.getStringExtra("isReload"));

        //---!!!!!TMP Not Load
        if((isFirstRun || isReload) ){
            ShowLoader();
            //new AsyncLoadDocsData(config.GetWorker(), this).execute(isReload?"-1":"0");
            new AsyncHelper<Boolean>(new IAsyncHelper() {
                @Override
                public Boolean Invoke() {
                    config.Worker.LoadStartData();
                    if(!config.isAutorized) return false;
                    if(!config.getCodeWarehouse().equals("000000000")) {
                        isFirstRun = false;
                        return config.Worker.LoadDocsData(isReload ? "-1" : "0",null);

                    }
                    return false;
                }},
                        new IPostResult<Boolean>() {
                    @Override
                    public void Invoke(Boolean p) {
                        if(config.isAutorized)
                         HideLoader();
                        else
                        {
                            Intent i = new Intent(context, AuthActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }

                    }
                }
            ).execute();
            if(!isReload)
            setAlarm(60 * 30, 60 * 30);
        }
        ////////////////////////////////

        menuItems[0] = findViewById(R.id.PriceCheker);
        menuItems[1] = findViewById(R.id.Revision);
        menuItems[2] = findViewById(R.id.Incom);
        menuItems[3] = findViewById(R.id.Moving);
        menuItems[4] = findViewById(R.id.WriteOff);
        menuItems[5] = findViewById(R.id.Returning);
        menuItems[6] = findViewById(R.id.Expenditure);
        menuItems[7] = findViewById(R.id.ML_Test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        for(int i = 0; i < menuItems.length; i++){
            menuItems[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // TODO Auto-generated method stub
                    if (hasFocus) {
                        ((Button)v).setTextColor(ContextCompat.getColor(context,android.R.color.white));
                    }else {
                        ((Button)v).setTextColor(ContextCompat.getColor(context,android.R.color.black));
                    }
                }

            });

            menuItems[i].setOnClickListener(this);
        }
        scaner=config.GetScaner();
        scaner.Init(this);
    }


    public void RunAuth()    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, AuthActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();

            }
        });

    }

    @Override
    public void Run(String parBarCode)
    {
        String varNumber=null;
        String varDocType=null;
        //Якщо внутрішне переміщення
        if(parBarCode.length()==13&& parBarCode.substring(0,2).equals("28")) {
            varDocType="9";
            varNumber=parBarCode.substring(2,8);
        }
        if(varNumber!=null&&varDocType!=null) {
            Intent i;
            i = new Intent(this, DocumentItemsActivity.class);
            i.putExtra("number", varNumber);
            i.putExtra("document_type", varDocType);
            startActivity(i);
        }
    }
    ;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!config.isAutorized){
            Intent i = new Intent(this, AuthActivity.class);
            startActivity(i);
        }
        else {
   /*         new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.SendLogPrice();
                    return null;
                }
            }).execute();*/
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Intent i;
        String keyCode = String.valueOf(event.getKeyCode());
        if(event.getAction() == KeyEvent.ACTION_UP) {

            switch (keyCode)
            {
                case "19":
                    current--;
                    break;
                case "20":
                    current++;
                    break;
                case "285":
                    menuItems[current].callOnClick();
                    break;
                case "131":
                    i = new Intent(this, PriceCheckerActivity.class);
                    startActivity(i);
                    break;
                default:
                    if(Integer.valueOf(keyCode)>=132 && Integer.valueOf(keyCode)<=137)
                    {
                        i = new Intent(this, DocumentActivity.class);
                        i.putExtra("document_type", Integer.valueOf(keyCode)-131);
                        startActivity(i);
                    }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        Intent i;
        int Id=v.getId();
        switch (v.getId()) {
            case R.id.PriceCheker:
                i = new Intent(this, PriceCheckerActivity.class);
                startActivity(i);
                break;
            case R.id.Revision:
                i = new Intent(this, DocumentActivity.class);
                i.putExtra("document_type", 1);
                startActivity(i);
                break;
            case R.id.Incom:
                i = new Intent(this, DocumentActivity.class);
                i.putExtra("document_type", 2);
                startActivity(i);
                break;
            case R.id.Moving:
                i = new Intent(this, DocumentActivity.class);
                i.putExtra("document_type", 3);
                startActivity(i);
                break;
            case R.id.WriteOff:
                i = new Intent(this, DocumentActivity.class);
                i.putExtra("document_type", 4);
                startActivity(i);
                break;
            case R.id.Returning:
                i = new Intent(this, DocumentActivity.class);
                i.putExtra("document_type", 5);
                startActivity(i);
                break;
            case R.id.Expenditure:
                i = new Intent(this, DocumentActivity.class);
                i.putExtra("document_type", 6);
                startActivity(i);
                break;
            case R.id.ML_Test:
                i = new Intent(this, TestActivity.class);
                i.putExtra("document_type", 1);
                i.putExtra("document_number", "ПСЮ00003483");
                startActivity(i);
                break;

        }
    }

    public final void setAlarm(int start, int interval) {
        // create the pending intent
        Intent intent = new Intent(this, AlarmReceiver.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0);
        // get the alarm manager, and scedule an alarm that calls the receiver
        ((AlarmManager) getSystemService(ALARM_SERVICE)).setRepeating(
                AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + start
                        * 1000,  interval
                        * 1000,pendingIntent);
    }

    public static class AlarmReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(!config.getCodeWarehouse().equals("000000000")) {
                //new AsyncLoadDocsData(config.GetWorker(), null).execute("0");
                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.LoadDocsData("0",null);
                        return null;
                    }
                }).execute();
            }

        }
    }

    public void HideLoader(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loader.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void ShowLoader(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loader.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Вийти з програми?")
                .setMessage("Ви справді хочете вийти?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //SomeActivity - имя класса Activity для которой переопределяем onBackPressed();
                        config.isAutorized = false;
                        finish();
                        moveTaskToBack(true);
                    }
                }).create().show();
    }
}