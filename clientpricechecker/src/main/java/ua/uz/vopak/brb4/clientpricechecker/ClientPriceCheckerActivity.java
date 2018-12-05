package ua.uz.vopak.brb4.clientpricechecker;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ClientPriceCheckerActivity extends Activity {
    LinearLayout ClientPriceChecker;
    EditText BarCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {
            }
        });

    }
}
