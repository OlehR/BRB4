package ua.uz.vopak.brb4.clientpricechecker;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import java.util.Timer;
import java.util.TimerTask;

public class ClientPriceCheckerActivity extends Activity {
    TextView Title, BarCodeView, Article, ActionView, PriceBill, PriceCoin;
    RelativeLayout InfoLayout;
    LinearLayout LogoLayout;
    ImageView Background;
    ClientPriceCheckerActivity context;
    LinearLayout ClientPriceChecker;
    EditText BarCode;
    private Timer infoLayoutTimer;
    private InfoLayoutTimerTask infoLayoutTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.client_price_checker_layout);

        ClientPriceChecker = findViewById(R.id.ClientPriceCheckerLayout);
        BarCode = findViewById(R.id.BarCode);
        BarCode.addTextChangedListener(new TextWatcher() {

            // the user's changes are saved here
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.toString().indexOf("\n") > 0){
                        String barCode = BarCode.getText().toString();
                        BarCode.setText("");
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
        PriceCoin = findViewById(R.id.PriceBill);
        Background = findViewById(R.id.Background);
        LogoLayout = findViewById(R.id.LogoLayout);

    }

    public void setScanResult(LabelInfo Li){
        Title.setText(Li.Name);
        BarCodeView.setText(Li.BarCode);
        Article.setText(Li.Article);
        PriceBill.setText(((Integer)Li.PriceBill).toString());
        PriceCoin.setText(((Integer)Li.PriceCoin).toString());

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
}
