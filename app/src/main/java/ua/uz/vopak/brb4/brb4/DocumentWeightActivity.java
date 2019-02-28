package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncInventories;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentWeightActivity extends Activity implements IIncomeRender {
    String number, documentType;
    List<Float> PrevValues = new ArrayList<Float>();
    LinearLayout tl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_weight_layout);
        Intent i = getIntent();
        number = i.getStringExtra("number");
        documentType = i.getStringExtra("document_type");
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number,documentType);

        tl = findViewById(R.id.ItemsTable);
    }

    public void RenderTableIncome(final List<DocWaresModelIncome> model, List<DocWaresModel> inventoryModel) {
        final DocumentWeightActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int dpValue = 3;
                    float d = context.getResources().getDisplayMetrics().density;
                    int padding = (int)(dpValue * d);

                    for (DocWaresModelIncome item : model) {

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
                        Title.setTextSize((int)(12*d));
                        Title.setTextColor(Color.parseColor("#000000"));
                        tr.addView(Title);

                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)Title.getLayoutParams();
                        params.width = 0;
                        params.weight = 2;
                        Title.setLayoutParams(params);
                        Title.setPadding(padding, padding, padding, padding);
                        Title.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        TextView QuantityOrdered = new TextView(context);
                        QuantityOrdered.setText(String.format("%.3f",item.QuantityOrdered));
                        QuantityOrdered.setTextSize((int)(12*d));
                        QuantityOrdered.setTextColor(Color.parseColor("#000000"));
                        tr1.addView(QuantityOrdered);

                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)QuantityOrdered.getLayoutParams();
                        params1.width = 0;
                        params1.weight = 1;
                        params1.height = LinearLayout.LayoutParams.MATCH_PARENT;
                        QuantityOrdered.setPadding(padding, padding, padding, padding);
                        QuantityOrdered.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                        QuantityOrdered.setLayoutParams(params1);

                        PrevValues.add(item.QuantityIncoming);
                        EditText QuantityIncomed = new EditText(context);
                        QuantityIncomed.setText(String.format("%.3f",item.QuantityIncoming));
                        QuantityIncomed.setTextColor(Color.parseColor("#000000"));
                        tr1.addView(QuantityIncomed);

                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)QuantityIncomed.getLayoutParams();
                        params2.width = 0;
                        params2.weight = 1;
                        QuantityIncomed.setLayoutParams(params2);
                        QuantityIncomed.setPadding(padding, padding, padding, padding);
                        QuantityIncomed.setBackground(ContextCompat.getDrawable(context, R.drawable.input_style));

                        QuantityIncomed.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean hasFocus) {
                                if (hasFocus) {
                                    int index = tl.indexOfChild(tl0);
                                    onFocus(index);
                                } else {

                                }
                            }
                        });

                        tl0.addView(tr);
                        tl0.addView(tr1);

                        int index = model.indexOf(item);
                        if((index % 2)==0) {
                            ViewGroup rows = tl0;
                            for (int i = 0; i < rows.getChildCount(); i++) {
                                LinearLayout trc = (LinearLayout) rows.getChildAt(i);

                                for(int j = 0; j < trc.getChildCount(); j++){
                                    if(!(trc.getChildAt(j) instanceof EditText))
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

    public void renderTable(final List<DocWaresModel> model){

    }

    public void onFocus(int index){
        int a = index;
    }
}
