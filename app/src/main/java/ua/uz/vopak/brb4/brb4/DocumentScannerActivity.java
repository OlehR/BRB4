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
    EditText barCode,  inputCount;

    ScrollView scrollView;
    RelativeLayout loader;
    TableLayout WaresTableLayout;

    Activity context;
    GlobalConfig config = GlobalConfig.instance();
    DocumentScannerActivityBinding binding;

    List<WaresItemModel> ListWares;
    WaresItemModel WaresItem = new WaresItemModel();

    int padding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.document_scanner_activity);
        Intent i = getIntent();

        float d = this.getResources().getDisplayMetrics().density;
        int dpValue = 3;
        padding = (int) (dpValue * d);

        WaresItem.NumberDoc = i.getStringExtra("inv_number");
        WaresItem.TypeDoc = i.getIntExtra("document_type", 0);
        List<DocWaresModel> LW = (List<DocWaresModel>) i.getSerializableExtra("InventoryItems");
        ListWares = new ArrayList<>();
        for (DocWaresModel item : LW)
            ListWares.add(new WaresItemModel(item));

        if (ListWares.size() > 0)
            WaresItem.OrderDoc = ListWares.get(ListWares.size() - 1).OrderDoc;

        config.InitScaner(this);

        WaresItem.ClearData();
        binding = DataBindingUtil.setContentView(this, R.layout.document_scanner_activity);
        binding.setWaresItem(WaresItem);
        //binding.setEmployee(employee);

        barCode = findViewById(R.id.RevisionBarCode);
        inputCount = findViewById(R.id.RevisionInputCount);

        loader = findViewById(R.id.RevisionLoader);
        WaresTableLayout = findViewById(R.id.RevisionScanItemsTable);
        scrollView = findViewById(R.id.RevisionScrollView);

        Refresh();
        RenderTable(ListWares);

        inputCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Refresh();
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
                }
            }
        });

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            String keyCode = String.valueOf(event.getKeyCode());
            switch (keyCode){
                case "66":
                    if(WaresItem.IsInputQuantity())
                       saveDocumentItem(false);
                    else
                        findWareByArticleOrCode();
                    break;
                case "62"://key SP
                    WaresItem.ClearData();
                    Refresh();
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

    @Override
    public void Run(final String parBarCode) {

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetWaresFromBarcode(WaresItem.TypeDoc,WaresItem.NumberDoc,parBarCode, context);
                return null;
            }
        }).execute();
    }

    public void RenderData(final WaresItemModel model){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(model == null)
                  WaresItem.ClearData("Товар не знайдено");
                else {
                    WaresItem.ClearData();
                    WaresItem.Set(model);
                    WaresItem.BeforeQuantity = CountBeforeQuantity(ListWares, WaresItem.CodeWares);
                }
                Refresh();
                SetAlert(WaresItem.CodeWares);
                return;
            }
        });
    }

    void Refresh( )    {
        binding.invalidateAll();

        if(WaresItem.IsInputQuantity()) {
            //inputCount.setFocusableInTouchMode(true);
            //inputCount.requestFocusFromTouch();
            inputCount.requestFocus();
            // inputCount.setFocusableInTouchMode(false);
            inputCount.setFocusableInTouchMode(true);
            inputCount.requestFocusFromTouch();
            inputCount.setFocusableInTouchMode(false);
        }
        else
        {
            barCode.requestFocus();
            barCode.setFocusableInTouchMode(true);
            barCode.requestFocusFromTouch();
            barCode.setFocusableInTouchMode(false);
        }
    }

    public TableRow RenderTableItem (WaresItemModel parWM ) {
        LinearLayout.LayoutParams params;
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

        TextView Position = new TextView(this);
        Position.setPadding(padding, padding, padding, padding);
        Position.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        Position.setText(parWM.GetOrderDoc());
        Position.setTextColor(Color.parseColor("#000000"));
        Position.setTag(parWM.GetCodeWares());
        tr.addView(Position);

        params = (LinearLayout.LayoutParams)Position.getLayoutParams();
        params.weight = 1;
        Position.setLayoutParams(params);

        TextView Title = new TextView(this);
        Title.setPadding(padding, padding, padding, padding);
        Title.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));

        Title.setText(parWM.NameWares.length()>25?parWM.NameWares.substring(0,25):parWM.NameWares);
        Title.setTextColor(Color.parseColor("#000000"));
        tr.addView(Title);

        params = (LinearLayout.LayoutParams)Title.getLayoutParams();
        params.weight = 3;
        Title.setLayoutParams(params);

        TextView Quantity = new TextView(this);
        Quantity.setPadding(padding, padding, padding, padding);
        Quantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        Quantity.setText(parWM.InputQuantity());
        Quantity.setTextColor(Color.parseColor("#000000"));
        tr.addView(Quantity);

        params = (LinearLayout.LayoutParams)Quantity.getLayoutParams();
        params.weight = 1;
        Quantity.setLayoutParams(params);

        TextView OldQuantity = new TextView(this);
        OldQuantity.setPadding(padding, padding, padding, padding);
        OldQuantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        OldQuantity.setText(parWM.GetQuantityOld());
        OldQuantity.setTextColor(Color.parseColor("#000000"));
        tr.addView(OldQuantity);

        params = (LinearLayout.LayoutParams)OldQuantity.getLayoutParams();
        params.weight = 1;
        OldQuantity.setLayoutParams(params);

       return tr;
    }

    public void RenderTable(List<WaresItemModel> parL )    {
        for (WaresItemModel item : parL) {
            WaresTableLayout.addView(RenderTableItem(item));
        }
    }

    public Double CountBeforeQuantity(List<WaresItemModel> parL,int pCodeWares)    {
        Double res=0d;
        for (WaresItemModel item : parL)
            if(item.CodeWares==pCodeWares)
                res+=item.InputQuantity;
            return res;
    }

    private void saveDocumentItem(final Boolean isNullable) {
        if (WaresItem.InputQuantity>0) {

            loader.setVisibility(View.VISIBLE);
            WaresItem.OrderDoc++;
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.SaveDocWares( WaresItem.InputQuantity, WaresItem.OrderDoc, WaresItem.CodeWares, WaresItem.NumberDoc, WaresItem.TypeDoc ,isNullable, context);
                    return null;
                }
            }).execute();
        }
/*
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                barCode.setFocusable(true);
                barCode.setFocusable(false);
                barCode.setFocusableInTouchMode(true);
                barCode.requestFocusFromTouch();
                barCode.setFocusableInTouchMode(false);
            }
        }, 100L);*/
    }

    public void AfterSave(final ArrayList args){
        final DocumentScannerActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isSave = (boolean) args.get(0);
                String message = (String) args.get(1);

                if(isSave) {
                    ListWares.add(WaresItem);
                    WaresTableLayout.addView(RenderTableItem(WaresItem));
                }

                loader.setVisibility(View.INVISIBLE);
                WaresItem.ClearData();
                Refresh();

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

    private void setNullToExistingPosition(){
        ArrayList<View> existPos = getViewsByTag(WaresTableLayout,"nullable");
        for(View item: existPos){
            if(item instanceof ViewGroup){
                View tx = ((ViewGroup) item).getChildAt(2);
                ((TextView)tx).setText("0");
            }
        }
        saveDocumentItem(true);
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

    private void SetAlert(int pCodeWares){
        ViewGroup rows = WaresTableLayout;
        for (int i = 0; i < rows.getChildCount(); i++) {
            TableRow trc = (TableRow) rows.getChildAt(i);
            View v = trc.getChildAt(0);
            String tag = (String) v.getTag();
            if(tag != null && tag.equals(String.valueOf(pCodeWares))){
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

        //CheckEmptyValue();
    }

    private void  findWareByArticleOrCode(){
        //new AsyncRevisionScanHelper(worker, aContext).execute(barCode.getText().toString());

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetWaresFromBarcode(WaresItem.TypeDoc,WaresItem.NumberDoc ,barCode.getText().toString(), context);
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
