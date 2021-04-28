package ua.uz.vopak.brb4.brb4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
    static boolean isNewVersion = true;
    static boolean isWriteDOWNLOADS =false;
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
                    isNewVersion=false;
                    AddText("Оновлення Відсутні");

                    isWriteDOWNLOADS=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canWrite();/*) {
                        AddText("Відсутні права на запис DOWNLOADS");
                        return false;
                    }*/
    /*                if(!isWriteDOWNLOADS)
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},0);
*/
                    if(config.IsAutoLogin&&config.Password.length()>0)
                    {
                        AddText("Автологін");
                        AuterizationsHelper aHelper=new AuterizationsHelper();
                        String res=aHelper.Login(activity,config.Login,config.Password,config.IsLoginCO,false);
                        if(res!=null) {
                            AddText("Автологін Успішно");
                            AddText(res);
                        }
                        else
                            AddText("Автологін Помилка");
                    }
                    return true;
                }},
                    new IPostResult<Boolean>() {
                        @Override
                        public void Invoke(Boolean p) {
                            isFirstRun = false;
                            onResume();
                            // if( !config.isAutorized)
                           //     RunAuth();
                        }
                    }
            ).execute();

    }
    @Override
    protected void onResume() {
        if (!isFinishing() && !isDestroyed()) {
            super.onResume();
            isWriteDOWNLOADS = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canWrite();
            boolean isAskPermissionCamera = config.IsUseCamera()  && (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);

            boolean isPhoneState = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
            if (!isFirstRun) {
                if (isNewVersion) {
                    AddText("Для продовження роботи необхідно оновити BRB4");
                } else {

                    if (isAskPermissionCamera || !isPhoneState || (!isWriteDOWNLOADS && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)) { //Непонятка з 10 андроїдом. Треба буде розібратись.
                        String[] Permissions = config.IsUseCamera() ?
                                                new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE} :
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
                        AddText("Для продовження роботи необхідно надати права на Зберігання та Статус телефона");
                        requestPermissions(Permissions, 0);
                    } else
                        RunForm(config.isAutorized ? MainActivity.class : AuthActivity.class);
                }
            }
        }
    }

    void AddText(final String pText)
    {
        Utils.WriteLog(pText+"\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String Text=TV.getText()+"\n"+pText;
                TV.setText(Text);
            }
        });
    }

    public void RunForm(final Class<?> par)    {//boolean pUseAutologin
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context,par); //(context, AuthActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
    }
}