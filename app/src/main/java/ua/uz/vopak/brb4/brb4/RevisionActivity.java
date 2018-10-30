package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncRevisionHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class RevisionActivity extends Activity {
    TableLayout tl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.revision_layout);
        tl = (TableLayout) findViewById(R.id.RevisionsList);
        new AsyncRevisionHelper(GlobalConfig.GetWorker(), this).execute();
    }

    public void renderTable(final String result){
        final RevisionActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    JSONObject jObject = new JSONObject(result);

                    if (jObject.getInt("State") == 1) {
                        JSONArray arrJson = jObject.getJSONArray("ListInventory");

                        for (int i = 0; i < arrJson.length(); i++) {
                            JSONArray innerArr = arrJson.getJSONArray(i);

                            String date = innerArr.getString(0);
                            String numberInv = innerArr.getString(1);
                            String extInfo = innerArr.getString(2);
                            String warehouseNumber = innerArr.getString(3);
                            String userName = innerArr.getString(4);

                            TableRow tr = new TableRow(context);
                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                            TableRow tr1 = new TableRow(context);
                            tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                            TableRow tr2 = new TableRow(context);
                            tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                            TextView Date = new TextView(context);
                            Date.setPadding(3, 3, 3, 3);
                            Date.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            Date.setText(date);
                            Date.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Date);

                            TextView NumberInv = new TextView(context);
                            NumberInv.setPadding(3, 3, 3, 3);
                            NumberInv.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            NumberInv.setText(numberInv);
                            NumberInv.setTextColor(Color.parseColor("#000000"));
                            tr.addView(NumberInv);

                            TextView ExtInfo = new TextView(context);
                            ExtInfo.setPadding(3, 3, 3, 3);
                            ExtInfo.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            ExtInfo.setText(extInfo);
                            ExtInfo.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(ExtInfo);

                            TextView WarehouseNumber = new TextView(context);
                            WarehouseNumber.setPadding(3, 3, 3, 3);
                            WarehouseNumber.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            WarehouseNumber.setText(warehouseNumber);
                            WarehouseNumber.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(WarehouseNumber);

                            TextView UserName = new TextView(context);
                            UserName.setPadding(3, 3, 3, 3);
                            UserName.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            UserName.setText(userName);
                            UserName.setTextColor(Color.parseColor("#000000"));

                            TableRow.LayoutParams params = (TableRow.LayoutParams)tr2.getLayoutParams();
                            params.span = 2;
                            tr2.setLayoutParams(params);

                            tr2.addView(UserName);

                            tl.addView(tr);
                            tl.addView(tr1);
                            tl.addView(tr2);
                        }
                    }

                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }
}
