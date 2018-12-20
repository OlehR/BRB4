package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncLoadListDoc;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentActivity extends Activity implements View.OnClickListener {
    LinearLayout tl;
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
                        LinearLayout.LayoutParams lp;

                        for (DocumentModel item : model) {

                            String date = item.DateDoc;
                            String numberInv = item.NumberDoc;
                            String extInfo = item.Description;
                            String userName = item.NameUser;

                            LinearLayout tl0 = new LinearLayout(context);
                            tl0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tl0.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout tr0 = new LinearLayout(context);
                            tr0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tr0.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout tr = new LinearLayout(context);
                            tr.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
                            Date.setText(date);
                            Date.setTextColor(Color.parseColor("#000000"));
                            tr.addView(Date);

                            Date.setPadding(padding, padding, padding, padding);
                            Date.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)Date.getLayoutParams();
                            params.width = 0;
                            params.weight = 1;
                            Date.setLayoutParams(params);

                            TextView NumberInv = new TextView(context);
                            NumberInv.setText(numberInv);
                            NumberInv.setTextColor(Color.parseColor("#000000"));
                            NumberInv.setTag("number_inv");
                            tr.addView(NumberInv);

                            NumberInv.setPadding(padding, padding, padding, padding);
                            NumberInv.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)NumberInv.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 1;
                            NumberInv.setLayoutParams(params1);

                            TextView ExtInfo = new TextView(context);
                            ExtInfo.setText(extInfo);
                            ExtInfo.setTextColor(Color.parseColor("#000000"));
                            tr1.addView(ExtInfo);

                            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)ExtInfo.getLayoutParams();
                            params2.width = 0;
                            params2.weight = 2;
                            ExtInfo.setLayoutParams(params2);
                            ExtInfo.setPadding(padding, padding, padding, padding);
                            ExtInfo.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            TextView UserName = new TextView(context);
                            UserName.setText(userName);
                            UserName.setTextColor(Color.parseColor("#000000"));
                            tr2.addView(UserName);

                            LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams)UserName.getLayoutParams();
                            params3.width = 0;
                            params3.weight = 2;
                            UserName.setLayoutParams(params3);
                            UserName.setPadding(padding, padding, padding, padding);
                            UserName.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                            tl0.addView(tr);
                            tl0.addView(tr1);
                            tl0.addView(tr2);

                            tr0.addView(tl0);
                            tr0.setOnClickListener(context);

                            tl.addView(tr0);
                        }

                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }
}
