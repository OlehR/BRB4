package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.HashMapHelper;
import ua.uz.vopak.brb4.brb4.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.IPostResult;
import ua.uz.vopak.brb4.brb4.helpers.WareListHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class SettingsActivity extends Activity implements View.OnClickListener {
    Button loadDocsData;
    TextView SN;
    Spinner connectionPrinterType;
    Spinner warList;
    CheckBox yellowpriceAutoprint;
    public Map<String, String> printerConnectionMap = new HashMap<String, String>();
    public ArrayAdapter<String> printerConnectionAdapter;
    GlobalConfig config = GlobalConfig.instance();
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        context = this;

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
        SN.setText("SN: " + GlobalConfig.instance().SN);
        loadDocsData = findViewById(R.id.LoadDocumentsData);
        loadDocsData.setOnClickListener(this);
        connectionPrinterType = findViewById(R.id.connectionPrinterType);
        yellowpriceAutoprint = findViewById(R.id.yellowAutoPrint);

        String[] printerConnectionPath = new String[]{"Без Принтера", "Тільки при вході", "Авто підключення"};
        printerConnectionMap.put("Без Принтера", "-1");
        printerConnectionMap.put("Тільки при вході", "0");
        printerConnectionMap.put("Авто підключення", "1");

        printerConnectionAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, printerConnectionPath);
        printerConnectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        connectionPrinterType.setAdapter(printerConnectionAdapter);


        connectionPrinterType.setSelection(printerConnectionAdapter.getPosition(HashMapHelper.getKeyFromValue(printerConnectionMap, config.connectionPrinterType.toString()).toString()));


        connectionPrinterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                config.connectionPrinterType = Integer.parseInt(printerConnectionMap.get(connectionPrinterType.getSelectedItem().toString()));

                new AsyncHelper<Void>(new IAsyncHelper() {
                    @Override
                    public Void Invoke() {
                        config.Worker.AddConfigPair("connectionPrinterType", config.connectionPrinterType.toString());
                        return null;
                    }
                }).execute();

                connectionPrinterType.setSelection(printerConnectionAdapter.getPosition(HashMapHelper.getKeyFromValue(printerConnectionMap, config.connectionPrinterType.toString()).toString()));

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


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
        });
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra("isReload","true");
        startActivity(i);
    }
}
