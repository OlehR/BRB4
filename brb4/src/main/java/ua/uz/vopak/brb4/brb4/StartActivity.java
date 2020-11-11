package ua.uz.vopak.brb4.brb4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import ua.uz.vopak.brb4.brb4.helpers.AuterizationsHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class StartActivity extends AppCompatActivity {
    TextView TV;
    static GlobalConfig config = GlobalConfig.instance();
    Context context;
    final Activity activity = this;
    static boolean isFirstRun = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        TV = findViewById(R.id.StartLog);
        context = this.getApplicationContext();
        //ініціалізація класа при старті.
        Utils.instance(context);
        config.Init(context);

       // TV.setText("SN: " + config.SN);

        new AsyncHelper<Boolean>(new IAsyncHelper() {
                @Override
                public Boolean Invoke() {
                    AddText("Завантаження початкових даних");
                    config.Worker.LoadStartData();
                    AddText("Пошук Оновлення");
                    if(config.cUtils.UpdateAPK("https://github.com/OlehR/BRB4/raw/master/apk/"+(config.IsTest?"test":"work")+"/","brb4.apk",null,BuildConfig.VERSION_CODE,BuildConfig.APPLICATION_ID))
                    {
                        AddText("Оновлення Знайдено. Встановлюємо.");
                        return false;
                    }
                    AddText("Оновлення Відсутні");
                    if(config.IsAutoLogin&&config.Password.length()>0)
                    {
                        AddText("Автологін");
                        AuterizationsHelper aHelper=new AuterizationsHelper();
                        if(aHelper.Login(activity,config.Login,config.Password))
                            AddText("Автологін Успішно");
                        else
                            AddText("Автологін Помилка");
                    }

                    return true;
                }},
                    new IPostResult<Boolean>() {
                        @Override
                        public void Invoke(Boolean p) {
                            isFirstRun = false;
                            if( !config.isAutorized)
                                RunAuth();
                        }
                    }
            ).execute();


    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!isFirstRun && !config.isAutorized){
            RunAuth();
        }
    }

    void AddText(final String pText)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String Text=TV.getText()+"\n"+pText;
                TV.setText(Text);
            }
        });
    }

    public void RunAuth()    {//boolean pUseAutologin
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
}