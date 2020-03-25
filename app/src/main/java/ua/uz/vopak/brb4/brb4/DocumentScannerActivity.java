package ua.uz.vopak.brb4.brb4;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ua.uz.vopak.brb4.brb4.databinding.DocumentScannerActivityBinding;
import ua.uz.vopak.brb4.brb4.enums.MessageType;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;

public class DocumentScannerActivity extends Activity   implements ScanCallBack {
    EditText barCode, currentCount, inputCount, scannerCof, scannerCount, countInPosition;
    TextView scannerTitle, inPosition, nameUnit;
    ScrollView scrollView;
    RelativeLayout loader;
    TableLayout WaresTableLayout;

    Activity context;
    GlobalConfig config = GlobalConfig.instance();
    DocumentScannerActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.document_scanner_activity);
    //public static DocumentScannerActivity aContext;

    int documentType;
    static String DocNumber;
    static Integer scanNN = 0;
    static String codeWares;
    List<DocWaresModel> ListWares;
    WaresItemModel WaresItem = new WaresItemModel();

    int dpValue = 3, padding;
    float d, totalExistingCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        d = this.getResources().getDisplayMetrics().density;
        padding = (int) (dpValue * d);
        setContentView(R.layout.document_scanner_activity);

        Intent i = getIntent();
        DocNumber = i.getStringExtra("inv_number");
        documentType = i.getIntExtra("document_type", 0);
        ListWares = (List<DocWaresModel>) i.getSerializableExtra("InventoryItems");
        if (ListWares.size() > 0)
            scanNN = Integer.parseInt(ListWares.get(ListWares.size() - 1).OrderDoc);
        codeWares = "";

        config.InitScaner(this);

        WaresItem.ClearData();
        binding.setWaresItem(WaresItem);
        //binding.setEmployee(employee);

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
        WaresTableLayout = findViewById(R.id.RevisionScanItemsTable);
        scrollView = findViewById(R.id.RevisionScrollView);

        RenderTable();

        inputCount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    WaresItem.InputQuantity= Float.parseFloat(inputCount.getText().toString());

                    //float input = Float.parseFloat(inputCount.getText().toString());
                    //scannerCount.setText(String.format(WaresItem.CodeUnit == 7 ? "%.3f" : "%.0f", (input *WaresItem.Coefficient )));
                }
            }
        });

    }

    @Override
    public void Run(final String parBarCode) {
        //new AsyncRevisionScanHelper(worker, aContext).execute(parBarCode);

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetWaresFromBarcode(parBarCode, context);
                return null;
            }
        }).execute();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            String keyCode = String.valueOf(event.getKeyCode());
            switch (keyCode){
                case "66":
                    barCode.setFocusable(true);
                    Object tag = barCode.getTag();
                    barCode.setFocusable(false);
                    if (tag != null && tag.toString().equals("onBarCode")) {
                        findWareByArticleOrCode();
                    } else {
                        saveDocumentItem(false);
                    }
                    break;
                case "131":
                    setNullToExistingPosition();
                    break;
                case "132":
                    focusOnView("up");
                case "133":
                    focusOnView("down");
                    break;
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

        for (int i = 0; i < WaresTableLayout.getChildCount(); i++) {
            View v = WaresTableLayout.getChildAt(i);

            if (v instanceof TableRow) {
                TableRow row = (TableRow) v;
                Object tag = row.getTag();
                if(tag != null && tag.toString().equals("alert")) {
                    WaresTableLayout.removeView(row);
                }
            }
        }
    }

    public void AfterSave(final ArrayList args){
        final DocumentScannerActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isSave = (boolean) args.get(0);
                String message = (String) args.get(1);

                barCode.setFocusable(true);
                barCode.setTag("onBarCode");
                barCode.setFocusable(false);

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

    public void RenderData(final WaresItemModel model){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(model == null){
                    currentCount.setText("0");
                    scannerCof.setText("");
                    scannerTitle.setText("Товар не знайдено");
                    nameUnit.setText("");
                    barCode.setFocusable(true);
                    barCode.setTag("onBarCode");
                    barCode.setFocusable(false);
                    barCode.setFocusableInTouchMode(true);
                    barCode.requestFocusFromTouch();
                    barCode.setFocusableInTouchMode(false);
                    return;
                }

                if(documentType==2){
                    loader.setVisibility(View.VISIBLE);
                    inputCount.setEnabled(false);
                }

                WaresItem = model;
                codeWares = Integer.toString( model.CodeWares);
                barCode.setText(model.BarCode);
                currentCount.setText("0");
                scannerCof.setText(Integer.toString(model.Coefficient));
                scannerTitle.setText(model.NameWares);
                nameUnit.setText(model.NameUnit + " X");


                if(WaresItem.CodeWares > 0){
                    barCode.setFocusable(true);
                    barCode.setTag(null);
                    barCode.setFocusable(false);
                    inputCount.setFocusableInTouchMode(true);
                    inputCount.requestFocusFromTouch();
                    inputCount.setFocusableInTouchMode(false);
                }

                CheckAlert();
                View similar = WaresTableLayout.findViewWithTag(codeWares);
                if (similar != null){
                    RenderTableItem(true);
                }
            }
        });
    }

    public void RenderTableItem(boolean isAlert){
            CheckEmptyValue();

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

            Title.setText(scannerTitle.getText().toString().length()>25?scannerTitle.getText().toString().substring(0,25):scannerTitle.getText().toString());
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

            WaresTableLayout.addView(tr);

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

                CheckAlert(tr);
            }

            scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                barCode.setFocusable(true);
                Object tag = barCode.getTag();
                barCode.setFocusable(false);
                if(tag == null || !tag.toString().equals("onBarCode")) {
                    inputCount.setFocusableInTouchMode(true);
                    inputCount.requestFocusFromTouch();
                    inputCount.setFocusableInTouchMode(false);
                }else{
                    barCode.setFocusableInTouchMode(true);
                    barCode.requestFocusFromTouch();
                    barCode.setFocusableInTouchMode(false);
                }
            }
        }, 100L);
    }

    public void RenderTable(){
        LinearLayout.LayoutParams params;

        for (DocWaresModel item : ListWares) {
            TableRow tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

            TextView Position = new TextView(this);
            Position.setPadding(padding, padding, padding, padding);
            Position.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            Position.setText(item.OrderDoc);
            Position.setTextColor(Color.parseColor("#000000"));
            Position.setTag(item.CodeWares);
            tr.addView(Position);

            params = (LinearLayout.LayoutParams)Position.getLayoutParams();
            params.weight = 1;
            Position.setLayoutParams(params);

            TextView Title = new TextView(this);
            Title.setPadding(padding, padding, padding, padding);
            Title.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
            String NameWares = item.NameWares.toString().length()>25?item.NameWares.toString().substring(0,25):item.NameWares.toString();
            Title.setText(NameWares);
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

            WaresTableLayout.addView(tr);

        }

        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //replace this line to scroll up or down
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                barCode.setFocusable(true);
                barCode.setTag("onBarCode");
                barCode.setFocusable(false);
                barCode.setFocusableInTouchMode(true);
                barCode.requestFocusFromTouch();
                barCode.setFocusableInTouchMode(false);
            }
        }, 100L);
    }

    public void setNullToExistingPosition(){
        ArrayList<View> existPos = getViewsByTag(WaresTableLayout,"nullable");

        for(View item: existPos){
            if(item instanceof ViewGroup){
                View tx = ((ViewGroup) item).getChildAt(2);
                ((TextView)tx).setText("0");
            }
        }

        saveDocumentItem(true);
    }

    private  void  saveDocumentItem(final Boolean isNullable) {
        String input = inputCount.getText().toString();

        if (input.equals("") || Integer.parseInt(input) <= 0 || scannerTitle.getText().toString().equals("")) {
            RemoveItemFromTable();
        } else {
            loader.setVisibility(View.VISIBLE);
            scanNN++;
            //new AsyncDocWares(worker, this).execute(scannerCount.getText().toString(), scanNN.toString(), codeWares, InventoryNumber, documentType, isNullable);
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.SaveDocWares( Double.valueOf( scannerCount.getText().toString()), scanNN, WaresItem.CodeWares, DocNumber, documentType,isNullable, context);
                    return null;
                }
            }).execute();
        }

        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                barCode.setFocusable(true);
                barCode.setTag("onBarCode");
                barCode.setFocusable(false);
                barCode.setFocusableInTouchMode(true);
                barCode.requestFocusFromTouch();
                barCode.setFocusableInTouchMode(false);
            }
        }, 100L);
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    private void CheckAlert(TableRow tr){
        totalExistingCount = 0f;
        ViewGroup rows = WaresTableLayout;
        for (int i = 0; i < rows.getChildCount(); i++) {
            TableRow trc = (TableRow) rows.getChildAt(i);
            View v = trc.getChildAt(0);
            String tag = (String) v.getTag();
            if(tag != null && tag.equals(codeWares)){
                if(!trc.equals(tr)){
                    trc.setTag("nullable");
                    totalExistingCount += Float.parseFloat(((TextView)trc.getChildAt(2)).getText().toString());
                }else{
                    if(trc.getTag() == null || !trc.getTag().toString().equals("alert"))
                    trc.setTag(null);
                }
                for (int j = 0; j < trc.getChildCount(); j++) {
                    TextView vI = (TextView)trc.getChildAt(j);
                    vI.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_alert));
                    vI.setTextColor(getResources().getColor(R.color.messageAlert));
                }
            }else{
                for (int j = 0; j < trc.getChildCount(); j++) {
                    TextView vI = (TextView)trc.getChildAt(j);
                    vI.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
                    vI.setTextColor(Color.parseColor("#000000"));
                }
            }
        }
        currentCount.setText(String.format(WaresItem.CodeUnit==7 ?"%.3f":"%.0f", totalExistingCount));

    }

    private void CheckAlert(){
        ViewGroup rows = WaresTableLayout;
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
            }else{
                trc.setTag(null);
                for (int j = 0; j < trc.getChildCount(); j++) {
                    TextView vI = (TextView)trc.getChildAt(j);
                    vI.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
                    vI.setTextColor(Color.parseColor("#000000"));
                }
            }
        }

        CheckEmptyValue();
    }

    private  void CheckEmptyValue(){
        ViewGroup rows = WaresTableLayout;
        TableRow tr = (TableRow) rows.getChildAt(rows.getChildCount() - 1);
        TextView tv = (TextView)tr.getChildAt(2);
        String a = tv.getText().toString();
        if(tv.getText().toString().equals("")){
            rows.removeView(tr);
        }
    }

    private void  findWareByArticleOrCode(){
        //new AsyncRevisionScanHelper(worker, aContext).execute(barCode.getText().toString());

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetWaresFromBarcode(barCode.getText().toString(), context);
                return null;
            }
        }).execute();
    }

    private final void focusOnView(final String prevent){
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                Rect scrollBounds = new Rect();
                scrollView.getDrawingRect(scrollBounds);

                switch (prevent) {
                    case "up":
                        //scrollView.scrollTo(0, scrollView.getScrollY());
                        ObjectAnimator.ofInt(scrollView, "scrollY",  (scrollView.getScrollY()-30)).setDuration(100).start();
                        break;
                    case "down":
                        //scrollView.scrollTo(0, scrollView.getScrollY());
                        ObjectAnimator.ofInt(scrollView, "scrollY",  (scrollView.getScrollY()+30)).setDuration(100).start();
                        break;
                }

            }
        });
    }
}
