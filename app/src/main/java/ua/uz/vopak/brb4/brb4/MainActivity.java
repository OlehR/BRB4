package ua.uz.vopak.brb4.brb4;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import com.journeyapps.barcodescanner.BarcodeView;

public class MainActivity extends FragmentActivity implements View.OnClickListener{
    Button btnRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        btnRestart = findViewById(R.id.button);
        btnRestart.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        BarcodeView barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.resume();
    }

}
