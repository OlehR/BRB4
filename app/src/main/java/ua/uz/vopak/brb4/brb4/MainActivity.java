package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class  MainActivity extends Activity implements View.OnClickListener {
    Button[] menuItems = new Button[4];
    int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
}