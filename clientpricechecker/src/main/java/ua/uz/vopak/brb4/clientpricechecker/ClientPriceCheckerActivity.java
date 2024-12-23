package ua.uz.vopak.brb4.clientpricechecker;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;


import org.json.JSONObject;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

import java.io.File;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class ClientPriceCheckerActivity extends Activity {
    TextView Title, BarCodeView, Article, ActionView, PriceBill, PriceCoin, PriceBillOpt, PriceCoinOpt, OptTitle, Sum;
    RelativeLayout InfoLayout;
    LinearLayout LogoLayout, OptPriceBlock, VideoWatermark, HideInfoLayout;
    ImageView Background, Logo, Logo2;
    TextView CheckPriceHungary;
    VideoView PromoVideo;
    ClientPriceCheckerActivity context;
    LinearLayout ClientPriceChecker;
    EditText BarCode;
    private Timer infoLayoutTimer;
    public Timer videoTimer;
    PowerManager pm;
    PowerManager.WakeLock wl;
    private InfoLayoutTimerTask infoLayoutTimerTask;
    Button HideInfoBTN;
    ua.uz.vopak.brb4.clientpricechecker.Config config;
    Resources res;
    //ClientPriceCheckerActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        context = this;
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.client_price_checker_layout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ClientPriceChecker = findViewById(R.id.ClientPriceCheckerLayout);
        BarCode = findViewById(R.id.BarCode);
        HideInfoBTN = findViewById(R.id.HiddenInfoBtn);
        HideInfoLayout = findViewById(R.id.HideInfoLayout);
        config = ua.uz.vopak.brb4.clientpricechecker.Config.instance(this.getApplicationContext());
        //config.Company= eCompany.VopakPSU;
        BarCode.addTextChangedListener(new TextWatcher() {

            // the user's changes are saved here
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().indexOf("\n") > 0) {
                    String barCode = BarCode.getText().toString();
                    BarCode.setText("");
                    if (videoTimer != null) {
                        videoTimer.cancel();
                    }

                    if (PromoVideo.isPlaying()) {
                        PromoVideo.stopPlayback();
                        PromoVideo.setVisibility(View.INVISIBLE);
                        VideoWatermark.setVisibility(View.INVISIBLE);
                    }

                    FindBarCode(barCode);
                    // new AsyncPriceDataHelper(context).execute(barCode);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {
            }
        });



        res = getResources();
        InfoLayout = findViewById(R.id.InfoLayout);
        Title = findViewById(R.id.Title);
        BarCodeView = findViewById(R.id.BarCodeView);
        Article = findViewById(R.id.Article);
        ActionView = findViewById(R.id.Action);
        PriceBill = findViewById(R.id.PriceBill);
        PriceCoin = findViewById(R.id.PriceCoin);
        PriceBillOpt = findViewById(R.id.PriceBillOpt);
        PriceCoinOpt = findViewById(R.id.PriceCoinOpt);
        Background = findViewById(R.id.Background);
        LogoLayout = findViewById(R.id.LogoLayout);
        PromoVideo = findViewById(R.id.PromoVideo);
        VideoWatermark = findViewById(R.id.VideoWatermark);
        OptTitle = findViewById(R.id.OptTitle);
        OptPriceBlock = findViewById(R.id.OptPriceBlock);
        Logo = findViewById(R.id.Logo);
        Logo2 = findViewById(R.id.Logo2);
        CheckPriceHungary = findViewById((R.id.CheckPriceHungary));
        Sum = findViewById(R.id.Sum);
        Resources res = getResources();

        Drawable background = res.getDrawable(config.Company==eCompany.SparPSU ? R.drawable.background1spar : ( config.Company==eCompany.LuboPSU?R.drawable.background1lubo :  R.drawable.background1vopak));
        Background.setBackground(background);
        background = res.getDrawable(config.Company==eCompany.SparPSU ? R.drawable.logo1spar : ( config.Company==eCompany.LuboPSU?R.drawable.logo1lubo : R.drawable.logo1vopak));
        Logo.setBackground(background);
        background = res.getDrawable(config.Company==eCompany.SparPSU ? R.drawable.logo2spar : ( config.Company==eCompany.LuboPSU?R.drawable.logo2lubo : R.drawable.logo2vopak));
        Logo2.setBackground(background);
        if(!Config.IsHungary) {
            CheckPriceHungary.setVisibility(View.INVISIBLE);
            findViewById(R.id.CheckPrice_HU).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.WorkPhone)).setText("З питань працевлаштування звертайтесь за тел: 050 549 6971");
        }

        if (videoTimer != null) {
            videoTimer.cancel();
        }


        videoTimer = new Timer();
        videoTimer.schedule(new VideoPlaybackTimerTask(), 60000, 60000);

        BarCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, final boolean hasFocus) {
                BarCode.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //replace this line to scroll up or down
                        if (!hasFocus) {
                            BarCode.requestFocus();
                        }
                    }
                }, 100L);
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               // new AsyncFileCheker(context).execute();

                new AsyncHelper<Boolean>(
                        new IAsyncHelper<Boolean>() {
                            @Override
                            public Boolean Invoke() {
                                Boolean Res= config.cUtils.GetFileFTP("smb://10.1.0.15"+config.SmbPath,"vopak.local",config.SmbUser, config.SmbPassword,Environment.getExternalStorageDirectory()+"/Movies/promo.mp4");
                                return Res;
                            }
                        },
                        new IPostResult<Boolean>() {
                            @Override
                            public void Invoke(Boolean pIsNewFile) {
                                if(!pIsNewFile)
                                    return;
                                context.runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if(videoTimer != null){
                                            videoTimer.cancel();
                                        }

                                        if(PromoVideo.isPlaying()){
                                            PromoVideo.stopPlayback();
                                            PromoVideo.setVisibility(View.INVISIBLE);
                                            VideoWatermark.setVisibility(View.INVISIBLE);
                                        }

                                        final File newDestination = new File(Environment.getExternalStorageDirectory()+"/Movies/promo.mp4");
                                        final File tmpDestination = new File(Environment.getExternalStorageDirectory()+"/Movies/promo.mp4.tmp");

                                        newDestination.delete();
                                        tmpDestination.renameTo(newDestination);

                                        videoPlayback();
                                    }
                                });

                            }
                        }).execute();
            }
        }, 20000, 60000 * 60);

        pm = (PowerManager) getSystemService(context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "clientpriceckecker::client_priceckecker_sleep");

        HideInfoBTN.setOnTouchListener(new View.OnTouchListener() {
            Handler handler = new Handler();

            int numberOfTaps = 0;
            long lastTapTimeMs = 0;
            long touchDownMs = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touchDownMs = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacksAndMessages(null);

                        if ((System.currentTimeMillis() - touchDownMs) > ViewConfiguration.getTapTimeout()) {
                            //it was not a tap

                            numberOfTaps = 0;
                            lastTapTimeMs = 0;
                            break;
                        }

                        if (numberOfTaps > 0
                                && (System.currentTimeMillis() - lastTapTimeMs) < ViewConfiguration.getDoubleTapTimeout()) {
                            numberOfTaps += 1;
                        } else {
                            numberOfTaps = 1;
                        }

                        lastTapTimeMs = System.currentTimeMillis();
                        //FindBarCode("5449000130389");
                        if (numberOfTaps == 3) {
                            if (InfoLayout.getVisibility() == View.VISIBLE)
                                HideInfoLayout.setVisibility(View.VISIBLE);
                        }
                }

                return true;
            }
        });

    }

    public void FindBarCode(String pBarCode) {
        new AsyncHelper<LabelInfo>(new IAsyncHelper() {
            @Override
            public LabelInfo Invoke() {
                LabelInfo LI = new LabelInfo(null);
                String BarCode = pBarCode.replace("\n", "");

                LI = new PricecheckerHelper().getPriceCheckerData(LI, BarCode, false, config);
                if (LI.resHttp != null && !LI.resHttp.isEmpty()) {
                    try {
                        LI.Init(new JSONObject(LI.resHttp));
                    } catch (Exception e) {
                        e.getMessage();
                    }
                }
                return LI;
            }
        },
                (IPostResult<LabelInfo>) p -> {
                    setScanResult(p);
                }
        ).execute();
    }

    public void setScanResult(LabelInfo Li) {
        Title.setText(Li.Name);
        BarCodeView.setText(Li.BarCode.get());
        Article.setText(Li.Article);
        PriceBill.setText(((Integer) Li.PriceBill).toString());
        PriceCoin.setText(Li.strPriceCoin() + " " + Li.Unit);
        PriceBillOpt.setText(((Integer) Li.PriceBillOpt).toString());
        PriceCoinOpt.setText(Li.strPriceCoinOpt());
        OptTitle.setText("від " + (Math.round(Li.QuantityOpt) == (long) Li.QuantityOpt ? Long.toString((long) Li.QuantityOpt) : Double.toString(Li.QuantityOpt)) + " " + Li.Unit);

        OptPriceBlock.setVisibility(Li.PriceBillOpt > 0 || Li.PriceCoinOpt > 0 ? View.VISIBLE : View.INVISIBLE);

        ActionView.setVisibility(Li.Action() ? View.VISIBLE : View.INVISIBLE);
        Sum.setText(Double.toString(Li.Sum) + " грн");
        Sum.setVisibility(Li.Sum > 0 ? View.VISIBLE : View.INVISIBLE);


        Drawable background = res.getDrawable(config.Company==eCompany.SparPSU ? R.drawable.background1spar :( config.Company==eCompany.LuboPSU?R.drawable.background1lubo : R.drawable.background1vopak));
        Background.setBackground(background);

        LogoLayout.setVisibility(View.INVISIBLE);
        InfoLayout.setVisibility(View.VISIBLE);

        if (videoTimer != null) {
            videoTimer.cancel();
        }

        if (PromoVideo.isPlaying()) {
            PromoVideo.stopPlayback();
            PromoVideo.setVisibility(View.INVISIBLE);
            VideoWatermark.setVisibility(View.INVISIBLE);
        }

        if (infoLayoutTimer != null) {
            infoLayoutTimer.cancel();
        }

        infoLayoutTimer = new Timer();
        infoLayoutTimerTask = new InfoLayoutTimerTask();

        infoLayoutTimer.schedule(infoLayoutTimerTask, 30000);

        HideInfoLayout.removeAllViews();
        try {
            JSONObject fields = new JSONObject(Li.resHttp);
            Iterator keys = fields.keys();
            int dpValue = 18;
            float d = context.getResources().getDisplayMetrics().density;
            int textSize = (int) (dpValue * d);
            while (keys.hasNext()) {
                String key = (String) keys.next();

                TextView label = new TextView(this);
                label.setText(key + " : " + fields.get(key));
                label.setTextSize(textSize);
                HideInfoLayout.addView(label);
            }
        } catch (Exception ex) {

        }
    }

    public void hideInfo() {
        InfoLayout.setVisibility(View.INVISIBLE);
        HideInfoLayout.removeAllViews();
        HideInfoLayout.setVisibility(View.INVISIBLE);
        LogoLayout.setVisibility(View.VISIBLE);

        Drawable background = res.getDrawable(config.Company==eCompany.SparPSU ? R.drawable.background2spar :( config.Company==eCompany.LuboPSU?R.drawable.background2lubo : R.drawable.background2vopak));
        Background.setBackground(background);

        if (videoTimer != null) {
            videoTimer.cancel();
        }

        videoTimer = new Timer();
        videoTimer.schedule(new VideoPlaybackTimerTask(), 60000, 60000);
    }

    public void videoPlayback() {
        String FileName= Environment.getExternalStorageDirectory() + "/Movies/promo.mp4";
        File f = new File(FileName);
        if(!f.exists())
            return;
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory() + "/Movies/promo.mp4");
        PromoVideo.setVideoURI(uri);
        PromoVideo.setVisibility(View.VISIBLE);
        VideoWatermark.setVisibility(View.VISIBLE);
        PromoVideo.start();
        BarCode.requestFocus();

        if (videoTimer != null) {
            videoTimer.cancel();
        }

        PromoVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                PromoVideo.setVisibility(View.INVISIBLE);
                VideoWatermark.setVisibility(View.INVISIBLE);
                videoTimer = new Timer();
                videoTimer.schedule(new VideoPlaybackTimerTask(), 60000, 60000);
            }
        });

        PromoVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int i, int j) {
                PromoVideo.setVisibility(View.INVISIBLE);
                VideoWatermark.setVisibility(View.INVISIBLE);
                videoTimer = new Timer();
                videoTimer.schedule(new VideoPlaybackTimerTask(), 60000, 60000);

                return true;
            }
        });

    }

    class InfoLayoutTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    hideInfo();
                }
            });
        }
    }

    class VideoPlaybackTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videoPlayback();
                }
            });
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    ;

    class SleepOrWakeUp extends TimerTask {

        @Override
        public void run() {
            if (!wl.isHeld()) {
                if (videoTimer != null) {
                    videoTimer.cancel();
                }

                if (PromoVideo.isPlaying()) {
                    PromoVideo.stopPlayback();
                    PromoVideo.setVisibility(View.INVISIBLE);
                    VideoWatermark.setVisibility(View.INVISIBLE);
                }

                wl.acquire();
            } else {
                wl.release();
                videoPlayback();
            }
        }

    }

}
