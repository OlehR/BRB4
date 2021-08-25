package ua.uz.vopak.brb4.brb4;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Date;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.databinding.MainLayoutBinding;
import ua.uz.vopak.brb4.brb4.models.MainModel;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class  MainActivity extends AppCompatActivity implements View.OnClickListener, ScanCallBack {
    MainLayoutBinding binding;
    private long backPressedTime;
    private Toast backToast;
    static Config config = Config.instance();
    //public RelativeLayout loader;
    private LinearLayout linearLayout;
    Button[] menuItems;
    int current = 0;
   // static boolean isFirstRun = true;

    private Scaner scaner;
    Context context;
    static MainModel MM = new MainModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        getSupportActionBar().setTitle("BRB "+ BuildConfig.VERSION_NAME+ " "+config.Login);

        //ініціалізація класа при старті.
        Utils.instance(context);
        //config.Init(context);

        setContentView(R.layout.main_layout);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = DataBindingUtil.setContentView(this, R.layout.main_layout);
        binding.setMM (MM);

        //loader = findViewById(R.id.RevisionLoader);
        linearLayout = findViewById(R.id.M_ButtonLayout);
        Intent in = getIntent();


        //---!!!!!TMP Not Load
        if(config.isAutorized ){
            setAlarm(60 * 30, 60 * 30);
        }
        ////////////////////////////////

        if(config.isAutorized) {
           int SizeArray= config.DocsSetting==null? 1: config.DocsSetting.length + 1;
            menuItems = new Button[SizeArray];
            menuItems[0] = findViewById(R.id.M_PriceCheker);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            //відступи між кнопками
            params.setMargins(5,0,5,10);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            if(config.DocsSetting!=null)
            for (int i = 0; i < config.DocsSetting.length; i++) {
                Button btn = new Button(this);
                btn.setText("F" + String.valueOf(i + 2) + "-" + config.DocsSetting[i].NameDoc);
                btn.setId(btn.generateViewId());//setId(some_random_id);
                btn.setPadding(50,0,50,0);
                btn.setTextSize(15);
                btn.setTextColor(Color.parseColor("#FFFFFF"));
                btn.setBackgroundResource(R.drawable.main_button);
                linearLayout.addView(btn, params);
                menuItems[i + 1] = btn;
            }

            for (int i = 0; i < menuItems.length; i++) {
                menuItems[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if (hasFocus) {
                            ((Button) v).setTextColor(ContextCompat.getColor(context, android.R.color.white));
                        } else {
                            ((Button) v).setTextColor(ContextCompat.getColor(context, android.R.color.white));
                        }
                    }

                });
                menuItems[i].setOnClickListener(this);
            }
            scaner = config.GetScaner();
            scaner.Init(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(config.isAutorized) {

            if (!config.getCodeWarehouse().equals("000000000")) {
                Date curDate = null;
                try {
                    curDate = config.FormatterDate.parse(config.FormatterDate.format(new Date()));
                } catch (Exception ex) {
                }

                if (config.LastFullUpdate == null || !config.LastFullUpdate.equals( curDate)) {
                    new AsyncHelper<Boolean>(new IAsyncHelper() {
                        @Override
                        public Boolean Invoke() {
                            return config.Worker.LoadData(-2, null, MM.Progress, false);
                        }
                    }
                    ).execute();
                }

            }
        }
        else
            RunAuth();
    }

    public void RunAuth()    {//boolean pUseAutologin
       runOnUiThread(new Runnable() {
            @Override
            public void run() {
                config.isAutorized=false;
                Intent i = new Intent(context, AuthActivity.class);
                i.putExtra("IsAutoLogin",false);
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
    }    ;

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
            case R.id.action_login:
                RunAuth();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
                case "131": //F1 -Прайсчекер
                    i = new Intent(this, PriceCheckerActivity.class);
                    startActivity(i);
                    break;
                default: //F2-F9
                    if(Integer.valueOf(keyCode)>=132 && Integer.valueOf(keyCode)<=137)
                        RunDoc(config.DocsSetting[Integer.valueOf(keyCode)-131].TypeDoc);


            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        int Id = v.getId();
        if (Id == R.id.M_PriceCheker) {
            intent = new Intent(this, PriceCheckerActivity.class);
            startActivity(intent);
            return;
        }

        for(int i = 1; i < menuItems.length; i++){
           if( menuItems[i].getId()==Id)
               RunDoc(config.DocsSetting[i-1].TypeDoc);

        }
         /* case R.id.ML_Test:
                i = new Intent(this, TestActivity.class);
                i.putExtra("document_type", 1);
                i.putExtra("document_number", "ПСЮ00003483");
                startActivity(i);
                break;*/
    }



    public void RunDoc(final int pTypeDoc) {


        new AsyncHelper<Boolean>(new IAsyncHelper() {
            @Override
            public Boolean Invoke() {
                    config.Worker.LoadData(pTypeDoc,null,MM.Progress,false);
                    return true;
            }
        },
                new IPostResult<Boolean>() {
                    @Override
                    public void Invoke(Boolean p) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(context, DocumentActivity.class);
                                intent.putExtra("document_type", pTypeDoc);
                                startActivity(intent);
                            }
                        });
                        return;
                    }}).execute();
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
                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.LoadData(0,null,MM.Progress,false);
                        return null;
                    }
                }).execute();
            }
        }
    }

//    @Override
//    public void onBackPressed() {
//        new AlertDialog.Builder(this)
//                .setTitle("Вийти з програми?")
//                .setMessage("Ви справді хочете вийти?")
//                .setNegativeButton(android.R.string.no, null)
//                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface arg0, int arg1) {
//                        //SomeActivity - имя класса Activity для которой переопределяем onBackPressed();
//                        config.isAutorized = false;
//                        finish();
//                        moveTaskToBack(true);
//                    }
//                }).create().show();
//    }

    //Кнопка НАЗАД

    @Override
    public void onBackPressed() {

        if (backPressedTime+2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            return;
        }else {
            backToast = Toast.makeText(getBaseContext(), "Натисніть ще раз, щоб вийти", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}