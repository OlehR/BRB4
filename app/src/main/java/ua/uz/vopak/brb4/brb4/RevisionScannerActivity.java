package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import device.common.DecodeResult;
import device.common.DecodeStateCallback;
import device.common.ScanConst;
import device.sdk.ScanManager;
import ua.uz.vopak.brb4.brb4.enums.MessageType;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncRevisionScanHelper;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncSaveInventory;
import ua.uz.vopak.brb4.brb4.helpers.EMDKWrapper;
import ua.uz.vopak.brb4.brb4.helpers.Worker;
import ua.uz.vopak.brb4.brb4.helpers.mScanerWrapper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.InventoryModel;
import ua.uz.vopak.brb4.brb4.models.RevisionItemModel;

public class RevisionScannerActivity extends Activity {
    EditText barCode, currentCount, inputCount, scannerCof, scannerCount, countInPosition;
    TextView scannerTitle, inPosition, nameUnit;
    ScrollView scrollView;
    EMDKWrapper emdkWrapper;
    static mScanerWrapper mScanerW;
    private final Handler mHandler = new Handler();
    public static RevisionScannerActivity aContext;
    static String InventoryNumber;
    static Integer scanNN = 0;
    RelativeLayout loader;
    static String codeWares;
    List<InventoryModel> InventoryItems;
    RevisionItemModel InventoryItem;
    TableLayout RevisionTable;
    static Worker worker = GlobalConfig.instance().GetWorker();
    int dpValue = 3;
    float d;
    int padding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        d = this.getResources().getDisplayMetrics().density;
        padding = (int)(dpValue * d);
        setContentView(R.layout.revision_scanner_activity);

        aContext = this;

        Intent i = getIntent();
        InventoryNumber = i.getStringExtra("inv_number");
        InventoryItems = (List<InventoryModel>)i.getSerializableExtra("InventoryItems");
        if(InventoryItems.size() > 0)
        scanNN = Integer.parseInt(InventoryItems.get(InventoryItems.size() - 1).NN);
        codeWares = "";

        String model = android.os.Build.MODEL;
        if( model.equals("TC20") && ( android.os.Build.MANUFACTURER.contains("Zebra Technologies") || android.os.Build.MANUFACTURER.contains("Motorola Solutions")) ){
            emdkWrapper  = new EMDKWrapper(getApplicationContext());
        }

        if( model.equals("PM550") && android.os.Build.MANUFACTURER.contains("Point Mobile Co., Ltd.")){
            mScanerW  = new mScanerWrapper();
            mScanerW.mContext = this;
            mScanerW.mScanner = new ScanManager();
            mScanerW.mDecodeResult = new DecodeResult();
            mScanerW.mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_EVENT);
        }

        barCode = findViewById(R.id.RevisionBarCode);
        currentCount = findViewById(R.id.RevisionScannerCurrentCount);
        inputCount = findViewById(R.id.RevisionInputCount);
        scannerCof = findViewById(R.id.RevisionScannerCof);
        scannerCount = findViewById(R.id.RevisionScannerCount);
        countInPosition = findViewById(R.id.RevisionCountInPosition);
        scannerTitle = findViewById(R.id.RevisionScannerTitle);
        inPosition = findViewById(R.id.RevisionInPosition);
        nameUnit = findViewById(R.id.RevisionNameUnit);
        loader = findViewById(R.id.RevisionLoader);
        RevisionTable = findViewById(R.id.RevisionScanItemsTable);
        scrollView = findViewById(R.id.RevisionScrollView);

        RenderTable();

        inputCount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0){
                    float input = Float.parseFloat(inputCount.getText().toString());
                    String cofStr = scannerCof.getText().toString();
                    if(scannerCof.getText() == null || !scannerCof.getText().equals(""))
                        scannerCof.setText("1");
                    float cof = Float.parseFloat(scannerCof.getText().toString());
                    scannerCount.setText(String.format("%.3f",(input * cof)));
                }
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());

        if(keyCode.equals("66") && event.getAction() == KeyEvent.ACTION_UP){
            String input = inputCount.getText().toString();
            if(input.equals("") || Integer.parseInt(input) <= 0){
                RemoveItemFromTable();
            }
            else {
                loader.setVisibility(View.VISIBLE);
                scanNN++;
                new AsyncSaveInventory(worker, this).execute(scannerCount.getText().toString(), scanNN.toString(), codeWares, InventoryNumber);
            }
        }

        return super.dispatchKeyEvent(event);
    }

    public void RemoveItemFromTable(){
        barCode.setText("");
        currentCount.setText("0");
        scannerCof.setText("");
        scannerTitle.setText("Назва: ");
        nameUnit.setText(" X");
        inputCount.setText("");
        scannerCount.setText("");

        for (int i = 0; i < RevisionTable.getChildCount(); i++) {
            View v = RevisionTable.getChildAt(i);

            if (v instanceof TableRow) {
                TableRow row = (TableRow) v;
                if(row.getTag() != null && row.getTag().toString().equals("alert")) {
                    RevisionTable.removeView(row);
                }
            }
        }
    }

    public void AfterSave(final ArrayList args){
        final RevisionScannerActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isSave = (boolean) args.get(0);
                String message = (String) args.get(1);

                if(isSave) {
                    RenderTableItem(false);
                    RemoveItemFromTable();
                }

                loader.setVisibility(View.INVISIBLE);

                if (!isSave) {
                    Intent i = new Intent(context, MessageActivity.class);
                    i.putExtra("messageHeader", "Невдалося зберегти значення!");
                    i.putExtra("message", message);
                    i.putExtra("type", MessageType.ErrorMessage);
                    startActivityForResult(i, 1);
                }
            }
        });
    }

    public void RenderData(final RevisionItemModel model){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InventoryItem = model;
                codeWares = model.CodeWares;
                barCode.setText(model.BarCode);
                currentCount.setText("0");
                scannerCof.setText(model.Coefficient);
                scannerTitle.setText(model.NameWares);
                nameUnit.setText(model.NameUnit + " X");

                View similar = RevisionTable.findViewWithTag(codeWares);

                if (similar != null){
                    RenderTableItem(true);
                }
            }
        });
    }

    public void RenderTableItem(boolean isAlert){
            LinearLayout.LayoutParams params;

            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            TextView Position = new TextView(this);
            Position.setPadding(padding, padding, padding, padding);
            Position.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Position.setText(scanNN.toString());
            Position.setTextColor(Color.parseColor("#000000"));
            Position.setTag(codeWares);
            tr.addView(Position);

            params = (LinearLayout.LayoutParams)Position.getLayoutParams();
            params.weight = 1;
            Position.setLayoutParams(params);

            TextView Title = new TextView(this);
            Title.setPadding(padding, padding, padding, padding);
            Title.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Title.setText(scannerTitle.getText().toString().substring(0,25));
            Title.setTextColor(Color.parseColor("#000000"));
            tr.addView(Title);

            params = (LinearLayout.LayoutParams)Title.getLayoutParams();
            params.weight = 3;
            Title.setLayoutParams(params);

            TextView Quantity = new TextView(this);
            Quantity.setPadding(padding, padding, padding, padding);
            Quantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Quantity.setText(scannerCount.getText());
            Quantity.setTextColor(Color.parseColor("#000000"));
            tr.addView(Quantity);

            params = (LinearLayout.LayoutParams)Quantity.getLayoutParams();
            params.weight = 1;
            Quantity.setLayoutParams(params);

            TextView OldQuantity = new TextView(this);
            OldQuantity.setPadding(padding, padding, padding, padding);
            OldQuantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            OldQuantity.setText("0");
            OldQuantity.setTextColor(Color.parseColor("#000000"));
            tr.addView(OldQuantity);

            params = (LinearLayout.LayoutParams)OldQuantity.getLayoutParams();
            params.weight = 1;
            OldQuantity.setLayoutParams(params);

            RevisionTable.addView(tr);

            if(isAlert){
                ViewGroup rowGroup = tr;
                for (int i = 0; i < rowGroup.getChildCount(); i++) {
                    TextView v = (TextView) rowGroup.getChildAt(i);
                    v.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_alert));
                    v.setTextColor(getResources().getColor(R.color.messageAlert));
                }

                Integer newScanNN = scanNN + 1;
                Position.setText(newScanNN.toString());

                tr.setTag("alert");

                ViewGroup rows = RevisionTable;
                for (int i = 0; i < rows.getChildCount(); i++) {
                    TableRow trc = (TableRow) rows.getChildAt(i);
                    View v = trc.getChildAt(0);
                    String tag = (String) v.getTag();
                    if(tag != null && tag.equals(codeWares)){
                        for (int j = 0; j < trc.getChildCount(); j++) {
                            TextView vI = (TextView)trc.getChildAt(j);
                            vI.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_alert));
                            vI.setTextColor(getResources().getColor(R.color.messageAlert));
                        }
                    }
                }
            }

            scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                inputCount.requestFocus();
            }
        }, 100L);
    }

    public void RenderTable(){
        LinearLayout.LayoutParams params;

        for (InventoryModel item : InventoryItems) {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            TextView Position = new TextView(this);
            Position.setPadding(padding, padding, padding, padding);
            Position.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Position.setText(item.NN);
            Position.setTextColor(Color.parseColor("#000000"));
            Position.setTag(item.CodeWares);
            tr.addView(Position);

            params = (LinearLayout.LayoutParams)Position.getLayoutParams();
            params.weight = 1;
            Position.setLayoutParams(params);

            TextView Title = new TextView(this);
            Title.setPadding(padding, padding, padding, padding);
            Title.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Title.setText(item.NameWares.toString().substring(0,25));
            Title.setTextColor(Color.parseColor("#000000"));
            tr.addView(Title);

            params = (LinearLayout.LayoutParams)Title.getLayoutParams();
            params.weight = 3;
            Title.setLayoutParams(params);

            TextView Quantity = new TextView(this);
            Quantity.setPadding(padding, padding, padding, padding);
            Quantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Quantity.setText(item.Quantity);
            Quantity.setTextColor(Color.parseColor("#000000"));
            tr.addView(Quantity);

            params = (LinearLayout.LayoutParams)Quantity.getLayoutParams();
            params.weight = 1;
            Quantity.setLayoutParams(params);

            TextView OldQuantity = new TextView(this);
            OldQuantity.setPadding(padding, padding, padding, padding);
            OldQuantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            OldQuantity.setText(item.OldQuantity);
            OldQuantity.setTextColor(Color.parseColor("#000000"));
            tr.addView(OldQuantity);

            params = (LinearLayout.LayoutParams)OldQuantity.getLayoutParams();
            params.weight = 1;
            OldQuantity.setLayoutParams(params);

            RevisionTable.addView(tr);

            scrollView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //replace this line to scroll up or down
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    inputCount.requestFocus();
                }
            }, 100L);
        }
    }

    public static class ScanResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mScanerW != null) {
                if (ScanConst.INTENT_USERMSG.equals(intent.getAction())) {
                    mScanerW.mScanner.aDecodeGetResult(mScanerW.mDecodeResult.recycle());
                }else if (ScanConst.INTENT_EVENT.equals(intent.getAction())) {
                    byte[] decodeBytesValue = intent.getByteArrayExtra(ScanConst.EXTRA_EVENT_DECODE_VALUE);
                    if(decodeBytesValue != null) {
                        String value = new String(decodeBytesValue);

                        new AsyncRevisionScanHelper(worker, aContext).execute(value);
                    }

                }
            }

        }
    }

    private DecodeStateCallback mStateCallback = new DecodeStateCallback(mHandler) {
        public void onChangedState(int state) {
            switch (state) {
                case ScanConst.STATE_ON:
                case ScanConst.STATE_TURNING_ON:

                    break;
                case ScanConst.STATE_OFF:
                case ScanConst.STATE_TURNING_OFF:

                    break;
            }
        };
    };

    private void initScanner() {
        if (mScanerW != null) {
            mScanerW.mScanner.aRegisterDecodeStateCallback(mStateCallback);
            mScanerW.mScanner.aDecodeSetResultType(ScanConst.ResultType.DCD_RESULT_USERMSG);
        }
    }

    private Runnable mStartOnResume = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initScanner();
                }
            });
        }
    };

    @Override
    protected void onPause() {
        if (mScanerW != null) {
            mScanerW.mScanner.aUnregisterDecodeStateCallback(mStateCallback);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mScanerW != null) {
            mScanerW = null;
        }
        super.onDestroy();
    }
}
