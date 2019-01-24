package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncInventories;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncUpdateDocState;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;

public class DocumentItemsActivity extends Activity implements View.OnClickListener {
    LinearLayout tl;
    Button btn, btnSave;
    String number, documentType;
    List<DocWaresModel> InventoryItems;

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
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number,documentType);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());

        if(keyCode.equals("134") && event.getAction() == KeyEvent.ACTION_UP){
            Intent i = new Intent(this, DocumentScannerActivity.class);
            i.putExtra("inv_number",number);
            i.putExtra("InventoryItems",(Serializable)InventoryItems);
            i.putExtra("document_type",documentType);
            startActivityForResult(i,1);
        }

        if(keyCode.equals("133") && event.getAction() == KeyEvent.ACTION_UP){
            new AsyncUpdateDocState(GlobalConfig.instance().GetWorker(),this).execute("1",number,documentType);
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.F4:
                Intent i = new Intent(this, DocumentScannerActivity.class);
                i.putExtra("inv_number",number);
                i.putExtra("InventoryItems",(Serializable)InventoryItems);
                i.putExtra("document_type",documentType);
                startActivityForResult(i,1);
                break;
            case R.id.F3:
                new AsyncUpdateDocState(GlobalConfig.instance().GetWorker(),this).execute("1",number,documentType);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new AsyncInventories(GlobalConfig.GetWorker(), this).execute(number,documentType);
    }

    public void renderTable(final List<DocWaresModel> model){
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
                        LinearLayout tr = new LinearLayout(context);
                        tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

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
                        for (DocWaresModel item : model) {

                            LinearLayout tl0 = new LinearLayout(context);
                            tl0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tl0.setOrientation(LinearLayout.VERTICAL);


                            LinearLayout tr0 = new LinearLayout(context);
                            tr0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tr0.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout tr = new LinearLayout(context);
                            tr.setOrientation(LinearLayout.HORIZONTAL);
                            tr.setWeightSum(2f);

                            LinearLayout tr1 = new LinearLayout(context);
                            tr1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tr1.setOrientation(LinearLayout.HORIZONTAL);
                            tr1.setWeightSum(2f);

                            LinearLayout tr2 = new LinearLayout(context);
                            tr2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tr2.setOrientation(LinearLayout.HORIZONTAL);
                            tr2.setWeightSum(2f);

                            TextView Date = new TextView(context);
                            Date.setText(item.Number);
                            Date.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Date);

                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)Date.getLayoutParams();
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

                            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)NumberInv.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 1;
                            NumberInv.setLayoutParams(params1);
                            NumberInv.setPadding(padding, padding, padding, padding);
                            NumberInv.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView ExtInfo = new TextView(context);
                            ExtInfo.setText(item.NameWares);
                            ExtInfo.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(ExtInfo);

                            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)ExtInfo.getLayoutParams();
                            params2.width = 0;
                            params2.weight = 2;
                            ExtInfo.setLayoutParams(params2);
                            ExtInfo.setPadding(padding, padding, padding, padding);
                            ExtInfo.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView UserName = new TextView(context);
                            UserName.setText("к-ст: " + item.Quantity);
                            UserName.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(UserName);

                            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams)UserName.getLayoutParams();
                            params3.width = 0;
                            params3.weight = 1;
                            UserName.setLayoutParams(params3);
                            UserName.setPadding(padding, padding, padding, padding);
                            UserName.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                            TextView OldQuantity = new TextView(context);
                            OldQuantity.setText("ст.к-сть: " + item.OldQuantity);
                            OldQuantity.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(OldQuantity);

                            LinearLayout.LayoutParams params4 = (LinearLayout.LayoutParams)OldQuantity.getLayoutParams();
                            params4.width = 0;
                            params4.weight = 1;
                            OldQuantity.setLayoutParams(params4);
                            OldQuantity.setPadding(padding, padding, padding, padding);
                            OldQuantity.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                            tl0.addView(tr);
                            tl0.addView(tr1);
                            tl0.addView(tr2);

                            tr0.addView(tl0);

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
