package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncInventories;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.InventoryModel;

public class RevisionItemsActivity extends Activity implements View.OnClickListener {
    TableLayout tl;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.revision_items_layout);
        tl = findViewById(R.id.InventoriesList);
        Intent i = getIntent();
        String number = i.getStringExtra("number");
        btn = findViewById(R.id.F4);
        btn.setOnClickListener(this);
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.F4:
                Intent i = new Intent(this, RevisionScannerActivity.class);
                startActivity(i);
                break;
        }
    }

    public void renderTable(final List<InventoryModel> model){
        final RevisionItemsActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    int dpValue = 5;
                    float d = context.getResources().getDisplayMetrics().density;
                    int padding = (int)(dpValue * d);
                    TableRow tr;

                    if(model.size() == 0){
                        tr = new TableRow(context);
                        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        TextView message = new TextView(context);
                        message.setPadding(padding, padding, padding, padding);
                        message.setTextSize(20 * d);
                        message.setGravity(Gravity.CENTER);
                        message.setText("Товар не знайдено");

                        tr.addView(message);
                        tl.addView(tr);
                    }
                    else {
                        padding = (int)(3 * d);
                        for (InventoryModel item : model) {

                            TableLayout tl0 = new TableLayout(context);
                            tl0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
                            tl0.setColumnStretchable(0, true);
                            tl0.setColumnStretchable(1, true);

                            TableRow tr0 = new TableRow(context);
                            tr0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            tr = new TableRow(context);
                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            TableRow tr1 = new TableRow(context);
                            tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            TableRow tr2 = new TableRow(context);
                            tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            TableRow tr3 = new TableRow(context);
                            tr3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

                            TextView Number = new TextView(context);
                            Number.setPadding(padding, padding, padding, padding);
                            Number.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            Number.setText(item.Number);
                            Number.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Number);

                            TextView Code = new TextView(context);
                            Code.setPadding(padding, padding, padding, padding);
                            Code.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            Code.setText(item.CodeWares);
                            Code.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Code);

                            TextView NameWares = new TextView(context);
                            NameWares.setText(item.NameWares);
                            NameWares.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(NameWares);

                            TableRow.LayoutParams params1 = (TableRow.LayoutParams)NameWares.getLayoutParams();
                            params1.span = 2;
                            NameWares.setLayoutParams(params1);
                            NameWares.setPadding(padding, padding, padding, padding);
                            NameWares.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView Quantity = new TextView(context);
                            Quantity.setPadding(padding, padding, padding, padding);
                            Quantity.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            Quantity.setText("к-ть: " + item.Quantity);
                            Quantity.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(Quantity);

                            TextView OldQuantity = new TextView(context);
                            OldQuantity.setPadding(padding, padding, padding, padding);
                            OldQuantity.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            OldQuantity.setText("ст. к-ть: " + item.OldQuantity);
                            OldQuantity.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(OldQuantity);

                            TextView NN = new TextView(context);
                            NN.setText(item.NN);
                            NN.setTextColor(Color.parseColor("#000000"));
                            tr3.addView(NN);

                            TableRow.LayoutParams params = (TableRow.LayoutParams)NN.getLayoutParams();
                            params.span = 2;
                            NN.setLayoutParams(params);
                            NN.setPadding(padding, padding, padding, padding);

                            tr3.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                            tl0.addView(tr);
                            tl0.addView(tr1);
                            tl0.addView(tr2);
                            tl0.addView(tr3);

                            tr0.addView(tl0);

                            tl.addView(tr0);
                        }
                    }
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }
}
