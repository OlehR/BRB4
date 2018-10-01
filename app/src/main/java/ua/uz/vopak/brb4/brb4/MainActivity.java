package ua.uz.vopak.brb4.brb4;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.journeyapps.barcodescanner.BarcodeView;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.StatusData;

public class MainActivity extends FragmentActivity implements View.OnClickListener, EMDKListener, StatusListener, DataListener{
    Button btnRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        btnRestart = findViewById(R.id.button);
        btnRestart.setOnClickListener(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onClick(View v) {
        BarcodeView barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.resume();
    }

    @Override
    public void onClosed() {
        // TODO Auto-generated method stub
    }
    @Override
    public void onOpened(EMDKManager emdkManager) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onData(ScanDataCollection scanDataCollection) {
        // TODO Auto-generated method stub
    }
    @Override
    public void onStatus(StatusData statusData) {
        // TODO Auto-generated method stub
    }

}
