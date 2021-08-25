package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import androidx.databinding.DataBindingUtil;
import ua.uz.vopak.brb4.brb4.databinding.SettingsLayoutBinding;
import ua.uz.vopak.brb4.brb4.models.SetingModel;
import ua.uz.vopak.brb4.brb4.models.Warehouse;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.brb4.models.Config;

public class SettingsActivity extends Activity implements View.OnClickListener {

    Config config = Config.instance();
    Context context;
    SettingsLayoutBinding binding;
    SetingModel SM = new SetingModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.settings_layout);
        binding.setSM(SM);

        BildWarehouse();
        TextView SN;
        SN = findViewById(R.id.SN);
        SN.setText("SN: " + config.SN);
        SN = findViewById(R.id.Ver);
        SN.setText("Ver: " + BuildConfig.VERSION_NAME);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case "66"://Enter
                    config.ApiUrl = SM.apiURL.get().trim().replace("\n", "");
                    new AsyncHelper<Void>(new IAsyncHelper() {
                        @Override
                        public Void Invoke() {
                            config.Worker.AddConfigPair("ApiUrl", config.ApiUrl);
                            return null;
                        }

                    }).execute();
                    break;

            }
        }
        return super.dispatchKeyEvent(event);
    }

    void BildWarehouse() {
        SM.Progress.set(5);

        new AsyncHelper<>(
                new IAsyncHelper<Warehouse[]>() {
                    @Override
                    public Warehouse[] Invoke() {
                        //ua.uz.vopak.brb4.brb4.Connector.Connector con = ua.uz.vopak.brb4.brb4.Connector.Connector.instance();
                        return config.Worker.GetWarehouse();//con.LoadWarehouse();
                    }
                },
                new IPostResult<Warehouse[]>() {
                    @Override
                    public void Invoke(final Warehouse[] wH) {

                        SM.Warehouse=wH;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SM.Progress.set(75);
                                SM.ListWarehouseIdx.set(0);
                                SM.ListWarehouse.clear();
                                for (int i = 0; i < wH.length; i++) {
                                    SM.ListWarehouse.add(wH[i].Name);
                                    if (wH[i].Code == config.CodeWarehouse)
                                        SM.ListWarehouseIdx.set(i);
                                }
                                SM.Progress.set(100);

                            }});
                    }
                }).execute();
    }
}
