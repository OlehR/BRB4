package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncLoadListDoc;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentActivity extends Activity implements View.OnClickListener {
    TableLayout tl;
    String DocumentType;
    DocumentActivity context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_layout);
        tl = findViewById(R.id.RevisionsList);
        context = this;

        Intent i = getIntent();
        DocumentType = i.getStringExtra("document_type");

        new AsyncLoadListDoc(GlobalConfig.GetWorker(), this).execute(DocumentType);
    }

    @Override
    public void onClick(View v) {
        TextView currentNumber = v.findViewWithTag("number_inv");
        Intent i = new Intent(context, DocumentItemsActivity.class);
        i.putExtra("number", currentNumber.getText().toString());
        i.putExtra("document_type", DocumentType);
        startActivity(i);
    }

    public void renderTable(final List<DocumentModel> model){
        final DocumentActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                        int dpValue = 10;
                        float d = context.getResources().getDisplayMetrics().density;
                        dpValue = 3;
                        int padding = (int)(dpValue * d);

                        for (DocumentModel item : model) {

                            String date = item.DateDoc;
                            String numberInv = item.NumberDoc;
                            String extInfo = item.ExtInfo;
                            String userName = item.NameUser;

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

                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }
}
