package ua.uz.vopak.brb4.brb4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
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
import ua.uz.vopak.brb4.brb4.helpers.*;
import ua.uz.vopak.brb4.brb4.models.AuthModel;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.Utils;

public class AuthActivity extends FragmentActivity implements ScanCallBack {
    static final String TAG="AuthActivity";
    private long backPressedTime;
    private Toast backToast;
    Config config = Config.instance();
    Context context;

    TextView nameStore;
    TextView TV;

    EditText login, password;
    public BarcodeView barcodeView;

    public AuterizationsHelper aHelper;
    final Activity activity = this;
    private Scaner scaner;

    AuthModel authModel= new AuthModel(this);
    public AuthLayoutBinding binding;

    static boolean isFirstRun = true;
    static boolean isNewVersion = true;
    static boolean isWriteDOWNLOADS =false;
    boolean IsAutoLogin=true;
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
        context=this.getApplicationContext();
        login = findViewById(R.id.Login);
        password = findViewById(R.id.Password);
        TV = findViewById(R.id.StartLog);

        Intent i = getIntent();
        IsAutoLogin =  i.getBooleanExtra("IsAutoLogin",true);

        //ініціалізація класа при старті.
        Utils.instance(context);
        config.Init(context);

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
        scaner=config.GetScaner();
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


        new AsyncHelper<Boolean>(new IAsyncHelper() {
            @Override
            public Boolean Invoke() {
                AddText("Завантаження початкових даних");
                config.GetWorker().LoadStartData();
                AddText("Видаленння старих логів");
                Utils.DellOldLog();

                AddText("Видаленння старих документів");
                config.GetWorker().DelOldData();

                AddText("Пошук Оновлення");
                if(config.cUtils.UpdateAPK("https://github.com/OlehR/BRB4/raw/master/apk/"+(config.IsTest?"test":"work")+"/","brb4.apk",null,BuildConfig.VERSION_CODE,BuildConfig.APPLICATION_ID))
                {
                    AddText("Оновлення Знайдено. Встановлюємо.");
                    return false;
                }
                isNewVersion=false;
                AddText("Оновлення Відсутні");
                aHelper=new AuterizationsHelper();
                isFirstRun = false;

                if(IsAutoLogin&& config.IsAutoLogin&&config.Password.length()>0)
                {
                    AddText("Автологін");
                    String res=aHelper.Login(activity,config.Login,config.Password,config.IsLoginCO,true);
                    if(res!=null) {
                        AddText("Автологін Успішно");
                        AddText(res);
                    }
                    else
                        AddText("Автологін Помилка");
                }
                return true;
            }},
                (IPostResult<Boolean>) p -> {
                    if(p) {
                        authModel.IsStarting=false;
                        authModel.Login=config.Login;
                        authModel.IsLoginCO=config.IsLoginCO;
                        binding.invalidateAll();
                    }
                    onResume();
                }
        ).execute();

    }

    @Override
    public void onResume() {

        if (!isFinishing() && !isDestroyed()) {
            super.onResume();
            //Zebra
            if (!authModel.IsStarting) {
                scaner.StartScan();

                if (config.IsUseCamera())
                    barcodeView.resume();
            } else {
                isWriteDOWNLOADS = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canWrite();
                boolean isAskPermissionCamera = config.IsUseCamera() && (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED);

                boolean isPhoneState = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED);
                if (!isFirstRun) {
                    if (isNewVersion) {
                        AddText("Для продовження роботи необхідно оновити BRB4");
                    } else {

                        if (isAskPermissionCamera || !isPhoneState || (!isWriteDOWNLOADS && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)) { //Непонятка з 10 андроїдом. Треба буде розібратись.
                            String[] Permissions = config.IsUseCamera() ?
                                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE} :
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
                            AddText("Для продовження роботи необхідно надати права на Зберігання та Статус телефона");
                            requestPermissions(Permissions, 0);
                        } else
                            if(config.isAutorized)
                            RunForm(MainActivity.class );
                    }
                }
            }
        }

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
            config.Worker.SetConfig(pBarCode);
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

    void AddText(final String pText)
    {
        Utils.WriteLog("i",TAG,pText+"\n");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                authModel.Log=pText+"\n"+authModel.Log;
                binding.invalidateAll();
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
