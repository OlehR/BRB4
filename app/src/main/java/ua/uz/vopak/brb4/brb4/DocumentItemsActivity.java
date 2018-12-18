package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncInventories;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncUpdateDocState;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.InventoryModel;

public class DocumentItemsActivity extends Activity implements View.OnClickListener {
    TableLayout tl;
    Button btn, btnSave;
    String number, documentType;
    List<InventoryModel> InventoryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_items_layout);
        tl = findViewById(R.id.InventoriesList);
        Intent i = getIntent();
        number = i.getStringExtra("number");
        documentType = i.getStringExtra("document_type");
        btn = findViewById(R.id.F4);
        btnSave = findViewById(R.id.F3);
        btn.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.F4:
                Intent i = new Intent(this, DocumentScannerActivity.class);
                i.putExtra("inv_number",number);
                i.putExtra("InventoryItems",(Serializable)InventoryItems);
                startActivityForResult(i,1);
                break;
            case R.id.F3:
                new AsyncUpdateDocState(GlobalConfig.instance().GetWorker(),this).execute("1",number,documentType);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number);
    }

    public void renderTable(final List<InventoryModel> model){
        InventoryItems = model;
        final DocumentItemsActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                tl.removeAllViews();

                try {
                    int dpValue = 5;
                    float d = context.getResources().getDisplayMetrics().density;
                    int padding = (int)(dpValue * d);

                    if(model.size() == 0){
                        TableRow tr = new TableRow(context);
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
                            tl0.setWeightSum(2f);


                            TableRow tr0 = new TableRow(context);

                            TableRow tr = new TableRow(context);

                            TableRow tr1 = new TableRow(context);

                            TableRow tr2 = new TableRow(context);

                            TextView Date = new TextView(context);
                            Date.setText(item.Number);
                            Date.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Date);

                            TableRow.LayoutParams params = (TableRow.LayoutParams)Date.getLayoutParams();
                            params.width = 0;
                            params.weight = 1;
                            Date.setLayoutParams(params);
                            Date.setPadding(padding, padding, padding, padding);
                            Date.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView NumberInv = new TextView(context);
                            NumberInv.setText(item.CodeWares);
                            NumberInv.setTextColor(Color.parseColor("#000000"));
                            NumberInv.setTag("number_inv");
                            tr.addView(NumberInv);

                            TableRow.LayoutParams params1 = (TableRow.LayoutParams)NumberInv.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 1;
                            NumberInv.setLayoutParams(params1);
                            NumberInv.setPadding(padding, padding, padding, padding);
                            NumberInv.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView ExtInfo = new TextView(context);
                            ExtInfo.setText(item.NameWares);
                            ExtInfo.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(ExtInfo);

                            TableRow.LayoutParams params2 = (TableRow.LayoutParams)ExtInfo.getLayoutParams();
                            params2.weight = 2;
                            ExtInfo.setLayoutParams(params2);
                            ExtInfo.setPadding(padding, padding, padding, padding);
                            ExtInfo.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView UserName = new TextView(context);
                            UserName.setText("к-ст: " + item.Quantity);
                            UserName.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(UserName);

                            TableRow.LayoutParams params3 = (TableRow.LayoutParams)UserName.getLayoutParams();
                            params3.width = 0;
                            params3.weight = 1;
                            UserName.setLayoutParams(params3);
                            UserName.setPadding(padding, padding, padding, padding);
                            UserName.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                            TextView OldQuantity = new TextView(context);
                            OldQuantity.setText("ст.к-сть: " + item.OldQuantity);
                            OldQuantity.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(OldQuantity);

                            TableRow.LayoutParams params4 = (TableRow.LayoutParams)OldQuantity.getLayoutParams();
                            params4.width = 0;
                            params4.weight = 1;
                            OldQuantity.setLayoutParams(params4);
                            OldQuantity.setPadding(padding, padding, padding, padding);
                            OldQuantity.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                            tl0.addView(tr);
                            tl0.addView(tr1);
                            tl0.addView(tr2);

                            tr0.addView(tl0);
                            tr0.setOnClickListener(context);

                            tl.addView(tr0);

                            tl0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        }
                    }
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }

    public void AfterSave(String DocumentType){
        Intent i = new Intent(this,DocumentActivity.class);
        i.putExtra("document_type", DocumentType);
        startActivity(i);
    }
}
