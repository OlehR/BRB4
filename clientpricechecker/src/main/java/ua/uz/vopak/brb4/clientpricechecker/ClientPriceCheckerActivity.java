package ua.uz.vopak.brb4.clientpricechecker;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;


import ua.uz.vopak.brb4.lib.models.LabelInfo;

import java.util.Timer;
import java.util.TimerTask;

public class ClientPriceCheckerActivity extends Activity {
    TextView Title, BarCodeView, Article, ActionView, PriceBill, PriceCoin, PriceBillOpt, PriceCoinOpt, OptTitle;
    RelativeLayout InfoLayout;
    LinearLayout LogoLayout, OptPriceBlock;
    ImageView Background;
    VideoView PromoVideo;
    ClientPriceCheckerActivity context;
    LinearLayout ClientPriceChecker;
    EditText BarCode;
    TextView VideoWatermark;
    private Timer infoLayoutTimer;
    public Timer videoTimer;
    PowerManager pm;
    PowerManager.WakeLock wl;
    private InfoLayoutTimerTask infoLayoutTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.client_price_checker_layout);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ClientPriceChecker = findViewById(R.id.ClientPriceCheckerLayout);
        BarCode = findViewById(R.id.BarCode);
        BarCode.addTextChangedListener(new TextWatcher() {

            // the user's changes are saved here
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.toString().indexOf("\n") > 0){
                        String barCode = BarCode.getText().toString();
                        BarCode.setText("");
                        if(videoTimer != null){
                            videoTimer.cancel();
                        }

                        if(PromoVideo.isPlaying()){
                            PromoVideo.stopPlayback();
                            PromoVideo.setVisibility(View.INVISIBLE);
                            VideoWatermark.setVisibility(View.INVISIBLE);
                        }
                        new AsyncPriceDataHelper(context).execute(barCode);
                    }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {
            }
        });

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

        if(videoTimer != null){
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
                        if(!hasFocus) {
                            BarCode.requestFocus();
                        }
                    }
                }, 100L);
            }
        });

        new Timer().schedule(new TimerTask(){
            @Override
            public void run() {
                new AsyncFileCheker(context).execute();
            }
        },90000, 60000 * 60);

        pm = (PowerManager) getSystemService(context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "clientpriceckecker::client_priceckecker_sleep");

    }

    public void setScanResult(LabelInfo Li){
        Title.setText(Li.Name);
        BarCodeView.setText(Li.BarCode);
        Article.setText(Li.Article);
        PriceBill.setText(((Integer)Li.PriceBill).toString());
        PriceCoin.setText(Li.strPriceCoin());
        PriceBillOpt.setText(((Integer)Li.PriceBillOpt).toString());
        PriceCoinOpt.setText(Li.strPriceCoinOpt());
        OptTitle.setText("від "+ (Math.round(Li.QuantityOpt)==(long) Li.QuantityOpt ? Long.toString((long)  Li.QuantityOpt) : Double.toString(Li.QuantityOpt)) + " " + Li.Unit);

        if(Li.PriceBillOpt > 0 || Li.PriceCoinOpt > 0){
            OptPriceBlock.setVisibility(View.VISIBLE);
        }else{
            OptPriceBlock.setVisibility(View.INVISIBLE);
        }

        if(Li.Action){
            ActionView.setVisibility(View.VISIBLE);
        }else {
            ActionView.setVisibility(View.INVISIBLE);
        }

        Resources res = getResources();
        Drawable background = res.getDrawable(R.drawable.background_3);

        Background.setBackground(background);
        LogoLayout.setVisibility(View.INVISIBLE);
        InfoLayout.setVisibility(View.VISIBLE);

        if(videoTimer != null){
            videoTimer.cancel();
        }

        if(PromoVideo.isPlaying()){
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

    }

    public void hideInfo(){
        InfoLayout.setVisibility(View.INVISIBLE);
        LogoLayout.setVisibility(View.VISIBLE);
        Resources res = getResources();
        Drawable background = res.getDrawable(R.drawable.background_4);
        Background.setBackground(background);

        if(videoTimer != null){
            videoTimer.cancel();
        }

        videoTimer = new Timer();
        videoTimer.schedule(new VideoPlaybackTimerTask(), 60000, 60000);
    }

    public void videoPlayback(){
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+"/Movies/promo.mp4");
        PromoVideo.setVideoURI(uri);
        PromoVideo.setVisibility(View.VISIBLE);
        VideoWatermark.setVisibility(View.VISIBLE);
        PromoVideo.start();
        BarCode.requestFocus();

        if(videoTimer != null){
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

    class SleepOrWakeUp extends  TimerTask{

        @Override
        public void run() {
            if(!wl.isHeld()) {
                if (videoTimer != null) {
                    videoTimer.cancel();
                }

                if (PromoVideo.isPlaying()) {
                    PromoVideo.stopPlayback();
                    PromoVideo.setVisibility(View.INVISIBLE);
                    VideoWatermark.setVisibility(View.INVISIBLE);
                }

                wl.acquire();
            }else{
                wl.release();
                videoPlayback();
            }
        }

    }

}
