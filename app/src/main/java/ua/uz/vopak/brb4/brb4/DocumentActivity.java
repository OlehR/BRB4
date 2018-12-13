package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncLoadListDoc;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentActivity extends Activity implements View.OnClickListener {
    TableLayout tl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.revision_layout);
        tl = findViewById(R.id.RevisionsList);
        new AsyncLoadListDoc(GlobalConfig.GetWorker(), this).execute("1");//!!!TMP
    }

    @Override
    public void onClick(View v) {
        TextView currentNumber = v.findViewWithTag("number_inv");
        Intent i = new Intent(this, DocumentItemsActivity.class);
        i.putExtra("number", currentNumber.getText());
        startActivity(i);

    }

    public void renderTable(final String result){
        final DocumentActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    JSONObject jObject = new JSONObject(result);

                    if (jObject.getInt("State") == 1) {
                        JSONArray arrJson = jObject.getJSONArray("ListInventory");
                        int dpValue = 10;
                        float d = context.getResources().getDisplayMetrics().density;
                        dpValue = 3;
                        int padding = (int)(dpValue * d);

                        for (int i = 0; i < arrJson.length(); i++) {
                            JSONArray innerArr = arrJson.getJSONArray(i);

                            String date = innerArr.getString(0);
                            String numberInv = innerArr.getString(1);
                            String extInfo = innerArr.getString(2);
                            String userName = innerArr.getString(4);

                            TableLayout tl0 = new TableLayout(context);
                            tl0.setWeightSum(2f);


                            TableRow tr0 = new TableRow(context);

                            TableRow tr = new TableRow(context);

                            TableRow tr1 = new TableRow(context);

                            TableRow tr2 = new TableRow(context);

                            TextView Date = new TextView(context);
                            Date.setText(date);
                            Date.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Date);

                            TableRow.LayoutParams params = (TableRow.LayoutParams)Date.getLayoutParams();
                            params.width = 0;
                            params.weight = 1;
                            Date.setLayoutParams(params);
                            Date.setPadding(padding, padding, padding, padding);
                            Date.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView NumberInv = new TextView(context);
                            NumberInv.setText(numberInv);
                            NumberInv.setTextColor(Color.parseColor("#000000"));
                            NumberInv.setTag("number_inv");
                            tr.addView(NumberInv);

                            TableRow.LayoutParams params1 = (TableRow.LayoutParams)NumberInv.getLayoutParams();
                            params.width = 0;
                            params1.weight = 1;
                            NumberInv.setLayoutParams(params1);
                            NumberInv.setPadding(padding, padding, padding, padding);
                            NumberInv.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView ExtInfo = new TextView(context);
                            ExtInfo.setText(extInfo);
                            ExtInfo.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(ExtInfo);

                            TableRow.LayoutParams params2 = (TableRow.LayoutParams)ExtInfo.getLayoutParams();
                            params.width = 0;
                            params2.weight = 2;
                            ExtInfo.setLayoutParams(params2);
                            ExtInfo.setPadding(padding, padding, padding, padding);
                            ExtInfo.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView UserName = new TextView(context);
                            UserName.setText(userName);
                            UserName.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(UserName);

                            TableRow.LayoutParams params3 = (TableRow.LayoutParams)UserName.getLayoutParams();
                            params.width = 0;
                            params3.weight = 2;
                            UserName.setLayoutParams(params);
                            UserName.setPadding(padding, padding, padding, padding);
                            UserName.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

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
}
