package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import java.util.HashMap;
import java.util.Map;

import ua.uz.vopak.brb4.brb4.Connector.SE.Connector;
import ua.uz.vopak.brb4.brb4.databinding.SettingsLayoutBinding;
import ua.uz.vopak.brb4.brb4.models.SetingModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.HashMapHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.brb4.helpers.WareListHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class SettingsActivity extends Activity implements View.OnClickListener {
    //Button loadDocsData;
    TextView SN;
    Spinner connectionPrinterType;
    Spinner warList;
    //CheckBox yellowpriceAutoprint;
    public Map<String, String> printerConnectionMap = new HashMap<String, String>();
    public ArrayAdapter<String> printerConnectionAdapter;
    GlobalConfig config = GlobalConfig.instance();
    Context context;
    SettingsLayoutBinding binding;
    SetingModel SM = new SetingModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        context = this;

        binding = DataBindingUtil.setContentView(this, R.layout.settings_layout);
        binding.setSM (SM);

        //new AsyncWaresHelper(new WareListHelper(this)).execute();

        new AsyncHelper<WareListHelper>(
                new IAsyncHelper<WareListHelper>() {
                    @Override
                    public WareListHelper Invoke() {
                        return new WareListHelper((SettingsActivity) context).getWares();
                    }
                },
                new IPostResult<WareListHelper>() {
                    @Override
                    public void Invoke(final WareListHelper wH) {
                        warList = wH.activity.findViewById(R.id.wares);

                        warList.setAdapter(wH.adapter);
                        warList.setPrompt("Склад");
                        try {
                            warList.setSelection(wH.adapter.getPosition(HashMapHelper.getKeyFromValue(wH.map, config.CodeWarehouse).toString()));
                        }
                        catch (Exception e)
                        {};
                        warList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                config.CodeWarehouse = wH.map.get(warList.getSelectedItem().toString());

                                //new AsyncConfigPairAdd(config.GetWorker()).execute("Warehouse", config.CodeWarehouse);
                                new AsyncHelper<Void>(new IAsyncHelper() {
                                    @Override
                                    public Void Invoke() {
                                        config.Worker.AddConfigPair("Warehouse",config.CodeWarehouse);
                                        return null;
                                    }
                                }).execute();
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                            }
                        });
                    }
                }).execute();

        SN = findViewById(R.id.SN);
        SN.setText("SN: " + config.SN);
        SN = findViewById(R.id.Ver);
        SN.setText("Ver: " + BuildConfig.VERSION_NAME);
        //SN = findViewById(R.id.ApiUrl);
        //SN.setText( config.ApiUrl);

        //loadDocsData = findViewById(R.id.LoadDocumentsData);
       // loadDocsData.setOnClickListener(this);
        connectionPrinterType = findViewById(R.id.connectionPrinterType);
        //yellowpriceAutoprint = findViewById(R.id.yellowAutoPrint);

        String[] printerConnectionPath = new String[eTypeUsePrinter.values().length];
        for(eTypeUsePrinter el : eTypeUsePrinter.values()) {
            printerConnectionPath[el.getAction()]=el.GetText();
            printerConnectionMap.put(el.GetText(), el.GetStrCode());
        }


        printerConnectionAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, printerConnectionPath);
        printerConnectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionPrinterType.setAdapter(printerConnectionAdapter);
        connectionPrinterType.setSelection(printerConnectionAdapter.getPosition(config.TypeUsePrinter.GetText()));


        connectionPrinterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                config.TypeUsePrinter = eTypeUsePrinter.fromOrdinal(printerConnectionMap.get(connectionPrinterType.getSelectedItem().toString()));

                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.AddConfigPair("connectionPrinterType", config.TypeUsePrinter.GetStrCode());
                        return null;
                    }
                }).execute();

                //connectionPrinterType.setSelection(printerConnectionAdapter.getPosition(HashMapHelper.getKeyFromValue(printerConnectionMap, config.TypeUsePrinter.toString()).toString()));

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

/*
        yellowpriceAutoprint.setChecked(config.yellowAutoPrint);


        yellowpriceAutoprint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new AsyncHelper<Void>(new IAsyncHelper() {
                        @Override
                        public Void Invoke() {
                            config.yellowAutoPrint = true;
                            config.Worker.AddConfigPair("yellowAutoPrint", "true");
                            yellowpriceAutoprint.setChecked(config.yellowAutoPrint);
                            return null;
                        }
                    }).execute();
                } else {
                    new AsyncHelper<Void>(new IAsyncHelper() {
                        @Override
                        public Void Invoke() {
                            config.yellowAutoPrint = false;
                            config.Worker.AddConfigPair("yellowAutoPrint", "false");
                            yellowpriceAutoprint.setChecked(config.yellowAutoPrint);
                            return null;
                        }
                    }).execute();
                }

            }
        });*/
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
                    config.ApiUrl = SM.apiURL.get().trim().replace("\n","");
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
}
