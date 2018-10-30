package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncRevisionHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class RevisionActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.revision_layout);
        new AsyncRevisionHelper(GlobalConfig.GetWorker(), this).execute();
    }

    public void renderTable(String result){

        try {
            JSONObject jObject = new JSONObject(result);

            if(jObject.getInt("State") == 1){
                JSONArray arrJson = jObject.getJSONArray("ListInventory");

                TableLayout tl = (TableLayout) findViewById(R.id.RevisionsList);

                for(int i = 0; i < arrJson.length(); i++) {
                    JSONArray innerArr = arrJson.getJSONArray(i);

                    String date = innerArr.getString(0);
                    String numberInv = innerArr.getString(1);
                    String extInfo = innerArr.getString(2);
                    String warehouseNumber = innerArr.getString(3);
                    String userName = innerArr.getString(4);

                    TableRow tr = new TableRow(this);
                    tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TableRow tr1 = new TableRow(this);
                    tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TableRow tr2 = new TableRow(this);
                    tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TextView Date = new TextView(this);
                    Date.setPadding(5, 5, 5, 5);
                    Date.setText(date);
                    tr.addView(Date);

                    TextView NumberInv = new TextView(this);
                    NumberInv.setPadding(5, 5, 5, 5);
                    NumberInv.setText(numberInv);
                    tr.addView(NumberInv);

                    TextView ExtInfo = new TextView(this);
                    ExtInfo.setPadding(5, 5, 5, 5);
                    ExtInfo.setText(extInfo);
                    tr1.addView(ExtInfo);

                    TextView WarehouseNumber = new TextView(this);
                    WarehouseNumber.setPadding(5, 5, 5, 5);
                    WarehouseNumber.setText(warehouseNumber);
                    tr1.addView(WarehouseNumber);

                    TextView UserName = new TextView(this);
                    UserName.setPadding(5, 5, 5, 5);
                    UserName.setText(userName);
                    tr2.addView(UserName);

                    tl.addView(tr);
                    tl.addView(tr1);
                    tl.addView(tr2);
                }
            }

        }catch (Exception e){
            e.getMessage();
        }
    }
}
