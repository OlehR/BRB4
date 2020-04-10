package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentWeightActivity extends Activity implements IIncomeRender {
    String number;
    int documentType;
    List<Double> PrevValues = new ArrayList<>();
    LinearLayout tl;
    HashMap<Integer, DocWaresModelIncome> data = new HashMap<Integer, DocWaresModelIncome>();
    List<DocWaresModelIncome> Model;
    int position = 0;
    EditText searchField;
    static Integer scanNN = 0;
    GlobalConfig config = GlobalConfig.instance();
    Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_weight_layout);
        context = this;
        Intent i = getIntent();
        number = i.getStringExtra("number");
        documentType = i.getIntExtra("document_type",0);
        //new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number, documentType);
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetDoc(documentType,number,1,(IIncomeRender) context);
                return null;
            }
        }).execute();

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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());

        if(keyCode.equals("133") && event.getAction() == KeyEvent.ACTION_UP){
            syncData();
        }

        return super.dispatchKeyEvent(event);
    }

    public void RenderTableIncome(final List<WaresItemModel> model) {
        final DocumentWeightActivity context = this;
        //Model = model;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int dpValue = 3;
                    float d = context.getResources().getDisplayMetrics().density;
                    int padding = (int) (dpValue * d);

                    for (WaresItemModel item : model) {

                        final LinearLayout tl0 = new LinearLayout(context);
                        tl0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tl0.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout tr = new LinearLayout(context);
                        tr.setOrientation(LinearLayout.HORIZONTAL);
                        tr.setWeightSum(2f);

                        LinearLayout tr1 = new LinearLayout(context);
                        tr1.setOrientation(LinearLayout.HORIZONTAL);
                        tr1.setWeightSum(2f);

                        TextView Title = new TextView(context);
                        Title.setText(item.NameWares);
                        Title.setTextSize((int) (12 * d));
                        Title.setTextColor(Color.parseColor("#000000"));
                        tr.addView(Title);

                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) Title.getLayoutParams();
                        params.width = 0;
                        params.weight = 2;
                        Title.setLayoutParams(params);
                        Title.setPadding(padding, padding, padding, padding);
                        Title.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        TextView QuantityOrdered = new TextView(context);
                        QuantityOrdered.setText(item.GetQuantityOrder());
                        QuantityOrdered.setTextSize((int) (12 * d));
                        QuantityOrdered.setTextColor(Color.parseColor("#000000"));
                        tr1.addView(QuantityOrdered);

                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) QuantityOrdered.getLayoutParams();
                        params1.width = 0;
                        params1.weight = 1;
                        params1.height = LinearLayout.LayoutParams.MATCH_PARENT;
                        QuantityOrdered.setPadding(padding, padding, padding, padding);
                        QuantityOrdered.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                        QuantityOrdered.setLayoutParams(params1);

                        PrevValues.add(item.InputQuantity);
                        EditText QuantityIncomed = new EditText(context);
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
                        tr1.addView(QuantityIncomed);

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
                        QuantityIncomed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    int index = tl.indexOfChild(tl0);
                                    onFocus(index);
                                } else {
                                    int index = tl.indexOfChild(tl0);
                                    onBlur(index);
                                }
                            }
                        });

                        tl0.addView(tr);
                        tl0.addView(tr1);

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

                        tl.addView(tl0);
                    }


                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });
    }

    public void renderTable(final List<WaresItemModel> model) {

    }

    private void onFocus(int index) {
        position = index;
        ViewGroup row = (ViewGroup) tl.getChildAt(index);
        ViewGroup innerRow = (ViewGroup) row.getChildAt(1);
        EditText v = (EditText) innerRow.getChildAt(1);
        v.setText("");
    }

    private void onBlur(int index) {
        ViewGroup row = (ViewGroup) tl.getChildAt(index);
        ViewGroup innerRow = (ViewGroup) row.getChildAt(1);
        EditText v = (EditText) innerRow.getChildAt(1);
        if (v.getText().toString().equals("") || PrevValues.get(index) == Float.parseFloat(v.getText().toString())) {
            v.setText(String.format("%.3f", PrevValues.get(index)));
        } else {
            PrevValues.set(index,Double.parseDouble(v.getText().toString()));
            data.put(index, Model.get(index));
        }
    }

    private void savePosition(){
        ViewGroup row = (ViewGroup) tl.getChildAt(position);
        ViewGroup innerRow = (ViewGroup) row.getChildAt(1);
        EditText v = (EditText) innerRow.getChildAt(1);
        final String value = v.getText().toString();
        if(!value.equals("") && Float.parseFloat(value) != 0){
            scanNN++;
            //new AsyncDocWares(GlobalConfig.GetWorker(), this).execute(value, scanNN.toString(), Model.get(position).CodeWares, number, documentType, "true");
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    config.Worker.SaveDocWares(documentType, number,Integer.parseInt( Model.get(position).CodeWares),scanNN,  Double.valueOf( value), true, context);
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
            EditText v = (EditText) innerRow.getChildAt(1);
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

    private void syncData(){
        //new AsyncUpdateDocState(GlobalConfig.GetWorker(),this).execute("1",number,documentType);
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.UpdateDocState(1,documentType,number, (Activity) context);
                return null;
            }
        }).execute();
    }

    public void AfterSave(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Документ успішно збережено!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
