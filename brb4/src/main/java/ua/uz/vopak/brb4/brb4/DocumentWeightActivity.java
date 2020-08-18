package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;

public class DocumentWeightActivity extends Activity  {
    String NumberDoc;
    int documentType;
    DocSetting DocSetting;
    List<Double> PrevValues = new ArrayList<>();
    LinearLayout tl;
    HashMap<Integer, DocWaresModelIncome> data = new HashMap<Integer, DocWaresModelIncome>();
    List<WaresItemModel> Model;
    int position = 0;
    EditText searchField;
    Integer scanNN = 0;
    GlobalConfig config = GlobalConfig.instance();
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_weight_layout);
        context = this;
        Intent i = getIntent();
        NumberDoc = i.getStringExtra("number");
        documentType = i.getIntExtra("document_type",0);
        DocSetting=config.GetDocSetting(documentType);
        GetDoc();

        tl = findViewById(R.id.ItemsTable);
        searchField = findViewById(R.id.searchFild);
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                filter(s.toString().toLowerCase());
            }
        });
    }


    void GetDoc()
    {
        new AsyncHelper<List<WaresItemModel>>(new IAsyncHelper() {
            @Override
            public List<WaresItemModel> Invoke() {
                return config.Worker.GetDocWares(documentType, NumberDoc,1, eTypeOrder.Scan);
            }
        },
                new IPostResult<List<WaresItemModel>>() {
                    @Override
                    public void Invoke(List<WaresItemModel> p) {
                        renderTable(p);
                        return;
                    }}).execute();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());
/*
        if(keyCode.equals("133") && event.getAction() == KeyEvent.ACTION_UP){
            syncData();
        }*/

        return super.dispatchKeyEvent(event);
    }

    public void RenderTableIncome(final List<WaresItemModel> model) {
        final DocumentWeightActivity context = this;
        Model = model;
        scanNN=0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {

                    int dpValue = 3;
                    float d = context.getResources().getDisplayMetrics().density;
                    int padding = (int) (dpValue * d);

                    for (WaresItemModel item : model) {
                        if (item.OrderDoc > scanNN && item.OrderDoc < 100000)
                            scanNN = item.OrderDoc;

                        final LinearLayout tl0 = new LinearLayout(context);
                        tl0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tl0.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout Line1 = new LinearLayout(context);
                        Line1.setOrientation(LinearLayout.HORIZONTAL);
                        Line1.setWeightSum(2f);

                        LinearLayout Line2 = new LinearLayout(context);
                        Line2.setOrientation(LinearLayout.HORIZONTAL);
                        Line2.setWeightSum(2f);

                        TextView Title = new TextView(context);
                        Title.setText(item.NameWares);
                        Title.setTextSize((int) (12 * d));
                        Title.setTextColor(Color.parseColor("#000000"));
                        Line1.addView(Title);

                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) Title.getLayoutParams();
                        params.width = 0;
                        params.weight = 2;
                        Title.setLayoutParams(params);
                        Title.setPadding(padding, padding, padding, padding);
                        Title.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                        if(DocSetting.IsViewPlan) {
                            TextView QuantityOrdered = new TextView(context);
                            QuantityOrdered.setText(item.GetQuantityOrder());
                            QuantityOrdered.setTextSize((int) (12 * d));
                            QuantityOrdered.setTextColor(Color.parseColor("#000000"));
                            Line2.addView(QuantityOrdered);

                            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) QuantityOrdered.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 1;
                            params1.height = LinearLayout.LayoutParams.MATCH_PARENT;
                            QuantityOrdered.setPadding(padding, padding, padding, padding);
                            QuantityOrdered.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            QuantityOrdered.setLayoutParams(params1);
                        }

                        PrevValues.add(item.InputQuantity);
                        final EditText QuantityIncomed = new EditText(context);
                        QuantityIncomed.setText( item.GetInputQuantity());
                        QuantityIncomed.setTextColor(Color.parseColor("#000000"));
                        QuantityIncomed.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                        QuantityIncomed.setOnKeyListener(new View.OnKeyListener() {
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                // If the event is a key-down event on the "enter" button
                                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    savePosition();
                                    return true;
                                }
                                return false;
                            }
                        });
                        Line2.addView(QuantityIncomed);

                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) QuantityIncomed.getLayoutParams();
                        params2.width = 0;
                        params2.weight = 1;
                        QuantityIncomed.setLayoutParams(params2);
                        QuantityIncomed.setPadding(padding, padding, padding, padding);
                        QuantityIncomed.setBackground(ContextCompat.getDrawable(context, R.drawable.input_style));
                        QuantityIncomed.setFocusable(false);
                        QuantityIncomed.setFocusableInTouchMode(false);
                        QuantityIncomed.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodManager img = (InputMethodManager)
                                        getSystemService(INPUT_METHOD_SERVICE);
                                img.hideSoftInputFromWindow(v.getWindowToken(), 0);
                                v.setFocusableInTouchMode(true);
                                v.requestFocusFromTouch();
                                v.setFocusableInTouchMode(false);
                            }
                        });

                        tl0.addView(Line1);
                        tl0.addView(Line2);
                        tl.addView(tl0);

                        QuantityIncomed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    QuantityIncomed.setText("");
                                    position = tl.indexOfChild(tl0);
                                } else {
                                    int index = tl.indexOfChild(tl0);
                                    onBlur(index,QuantityIncomed);
                                }
                            }
                        });



                        int index = model.indexOf(item);
                        if ((index % 2) == 0) {
                            ViewGroup rows = tl0;
                            for (int i = 0; i < rows.getChildCount(); i++) {
                                LinearLayout trc = (LinearLayout) rows.getChildAt(i);

                                for (int j = 0; j < trc.getChildCount(); j++) {
                                    if (!(trc.getChildAt(j) instanceof EditText))
                                        trc.getChildAt(j).setBackground(ContextCompat.getDrawable(context, R.drawable.odd_row_bordered));
                                }

                            }
                        }


                    }


                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });
    }

    public void renderTable(final List<WaresItemModel> model) {
        RenderTableIncome(model);
    }



    private void onFocus(int index) {
        position = index;
        ViewGroup row = (ViewGroup) tl.getChildAt(index);
        ViewGroup innerRow = (ViewGroup) row.getChildAt(1);
        EditText v = (EditText) innerRow.getChildAt(1);
        v.setText("");
    }

    private void onBlur(int index,EditText v ) {
        if (v.getText().toString().equals("") || PrevValues.get(index) == Float.parseFloat(v.getText().toString())) {
            v.setText(String.format("%.3f", PrevValues.get(index)));
        } else {
            PrevValues.set(index,Double.parseDouble(v.getText().toString()));
            //data.put(index, Model.get(index));
        }
    }

    private void savePosition(){
        ViewGroup row = (ViewGroup) tl.getChildAt(position);
        ViewGroup innerRow = (ViewGroup) row.getChildAt(1);
        EditText v = (EditText) innerRow.getChildAt(DocSetting.IsViewPlan?1:0);
        final String value = v.getText().toString();
        if(!value.equals("") && Float.parseFloat(value) != 0){
            scanNN++;
            //new AsyncDocWares(GlobalConfig.GetWorker(), this).execute(value, scanNN.toString(), Model.get(position).CodeWares, number, documentType, "true");
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.SaveDocWares(documentType, NumberDoc, Model.get(position).CodeWares,scanNN,  Double.valueOf( value), 0,true);
                    return null;
                }
            }).execute();
        }
        moveNext();
    }

    private void moveNext(){
        if(position < Model.size()-1){
            ViewGroup row = (ViewGroup) tl.getChildAt(position+1);
            ViewGroup innerRow = (ViewGroup) row.getChildAt(1);
            EditText v = (EditText) innerRow.getChildAt(DocSetting.IsViewPlan?1:0);
            v.setFocusableInTouchMode(true);
            v.requestFocusFromTouch();
            v.setFocusableInTouchMode(false);
        }
    }

    private void filter(String content){
        for(int i = 0; i<tl.getChildCount(); i++ ) {
            ViewGroup row = (ViewGroup) tl.getChildAt(i);
            ViewGroup innerRow = (ViewGroup)row.getChildAt(0);
            TextView title = (TextView) innerRow.getChildAt(0);
            if(content == null || content.length() == 0){
                row.setVisibility(View.VISIBLE);
                return;
            }

            if(title.getText().toString().toLowerCase().contains(content)){
                row.setVisibility(View.VISIBLE);
            }else{
                row.setVisibility(View.GONE);
            }
        }
    }

}
