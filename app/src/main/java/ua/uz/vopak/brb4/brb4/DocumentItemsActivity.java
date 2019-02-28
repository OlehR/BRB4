package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncInventories;
import ua.uz.vopak.brb4.brb4.helpers.AsyncHelpers.AsyncUpdateDocState;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;

public class DocumentItemsActivity extends Activity implements View.OnClickListener, IIncomeRender {
    LinearLayout tl;
    FrameLayout documentItemsFrame;
    ScrollView documentList;
    Context context;
    Button btn, btnSave;
    String number, documentType;
    List<DocWaresModel> InventoryItems;
    int current = 0;
    List<View> menuItems = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_items_layout);
        context = this;
        documentList = findViewById(R.id.DocumentItemsList);
        documentItemsFrame = findViewById(R.id.DocumentItemsFrame);
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

        if(keyCode.equals("15") && event.getAction() == KeyEvent.ACTION_UP){
            selectNext();
            selectItem();
        }

        if(keyCode.equals("9") && event.getAction() == KeyEvent.ACTION_UP){
            selectPrev();
            selectItem();
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
                startActivity(i);
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
                            ExtInfo.setTag("extInfo");
                            ExtInfo.setTextColor(getResources().getColor(R.color.messageSuccess));
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
                            menuItems.add(tr0);

                            int index = model.indexOf(item);
                            if((index % 2)==0) {
                                ViewGroup rows = tl0;
                                for (int i = 0; i < rows.getChildCount(); i++) {
                                    LinearLayout trc = (LinearLayout) rows.getChildAt(i);

                                    for(int j = 0; j < trc.getChildCount(); j++){
                                        trc.getChildAt(j).setBackground(ContextCompat.getDrawable(context, R.drawable.odd_row_bordered));
                                    }

                                }
                            }

                            tl.addView(tr0);

                            tl0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr0.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                            tr2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                        }

                        selectItem();
                    }
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }

    public void RenderTableIncome(final List<DocWaresModelIncome> model, List<DocWaresModel> inventoryModel){
        final DocumentItemsActivity context = this;
        InventoryItems = inventoryModel;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int dpValue = 3;
                float d = context.getResources().getDisplayMetrics().density;
                int padding = (int)(dpValue * d);

                tl.removeAllViews();

                LinearLayout tlTitle = new LinearLayout(context);
                tlTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                tlTitle.setOrientation(LinearLayout.VERTICAL);

                LinearLayout trT = new LinearLayout(context);
                trT.setOrientation(LinearLayout.HORIZONTAL);
                trT.setWeightSum(7f);

                LinearLayout tr1T = new LinearLayout(context);
                tr1T.setOrientation(LinearLayout.HORIZONTAL);
                tr1T.setWeightSum(7f);

                TextView CodeWaresT = new TextView(context);
                CodeWaresT.setText("Код");
                CodeWaresT.setTextColor(Color.parseColor("#000000"));
                trT.addView(CodeWaresT);

                LinearLayout.LayoutParams paramsT = (LinearLayout.LayoutParams)CodeWaresT.getLayoutParams();
                paramsT.width = 0;
                paramsT.weight = 3;
                CodeWaresT.setLayoutParams(paramsT);
                CodeWaresT.setPadding(padding, padding, padding, padding);
                CodeWaresT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                TextView QuantityOrderedT = new TextView(context);
                QuantityOrderedT.setText("Замовлено");
                QuantityOrderedT.setTextColor(Color.parseColor("#000000"));
                trT.addView(QuantityOrderedT);

                LinearLayout.LayoutParams params1T = (LinearLayout.LayoutParams)QuantityOrderedT.getLayoutParams();
                params1T.width = 0;
                params1T.weight = 2;
                QuantityOrderedT.setLayoutParams(params1T);
                QuantityOrderedT.setPadding(padding, padding, padding, padding);
                QuantityOrderedT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                TextView QuantityIncomedT = new TextView(context);
                QuantityIncomedT.setText("Прийнято");
                QuantityIncomedT.setTextColor(Color.parseColor("#000000"));
                trT.addView(QuantityIncomedT);

                LinearLayout.LayoutParams params2T = (LinearLayout.LayoutParams)QuantityIncomedT.getLayoutParams();
                params2T.width = 0;
                params2T.weight = 2;
                QuantityIncomedT.setLayoutParams(params2T);
                QuantityIncomedT.setPadding(padding, padding, padding, padding);
                QuantityIncomedT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                TextView NameWaresT = new TextView(context);
                NameWaresT.setText("Назва");
                NameWaresT.setTextColor(Color.parseColor("#000000"));
                tr1T.addView(NameWaresT);

                LinearLayout.LayoutParams params3T = (LinearLayout.LayoutParams)NameWaresT.getLayoutParams();
                params3T.width = 0;
                params3T.weight = 7;
                NameWaresT.setLayoutParams(params3T);
                NameWaresT.setPadding(padding, padding, padding, padding);
                NameWaresT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                tlTitle.addView(tr1T);
                tlTitle.addView(trT);

                documentList.setPadding((int)(d * 5  ), (int)(d * 54  ),(int)(d * 5  ),(int)(d * 25  ));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tlTitle.getLayoutParams();
                layoutParams.setMargins((int)(d * 5  ),(int)(d * 5  ),(int)(d * 5  ),0 );
                tlTitle.setLayoutParams(layoutParams);

                documentItemsFrame.addView(tlTitle);


                try {

                    for (DocWaresModelIncome item : model) {

                        LinearLayout tl0 = new LinearLayout(context);
                        tl0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        tl0.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout tr = new LinearLayout(context);
                        tr.setOrientation(LinearLayout.HORIZONTAL);
                        tr.setWeightSum(7f);

                        LinearLayout tr1 = new LinearLayout(context);
                        tr1.setOrientation(LinearLayout.HORIZONTAL);
                        tr1.setWeightSum(7f);

                        TextView CodeWares = new TextView(context);
                        CodeWares.setText(item.CodeWares);
                        CodeWares.setTextColor(Color.parseColor("#000000"));
                        tr.addView(CodeWares);

                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)CodeWares.getLayoutParams();
                        params.width = 0;
                        params.weight = 3;
                        CodeWares.setLayoutParams(params);
                        CodeWares.setPadding(padding, padding, padding, padding);
                        CodeWares.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        TextView QuantityOrdered = new TextView(context);
                        QuantityOrdered.setText(item.QuantityOrdered.toString());
                        QuantityOrdered.setTextColor(Color.parseColor("#000000"));
                        tr.addView(QuantityOrdered);

                        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)QuantityOrdered.getLayoutParams();
                        params1.width = 0;
                        params1.weight = 2;
                        QuantityOrdered.setLayoutParams(params1);
                        QuantityOrdered.setPadding(padding, padding, padding, padding);
                        QuantityOrdered.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        TextView QuantityIncomed = new TextView(context);
                        QuantityIncomed.setText(item.QuantityIncoming.toString());
                        QuantityIncomed.setTextColor(Color.parseColor("#000000"));
                        tr.addView(QuantityIncomed);

                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)QuantityIncomed.getLayoutParams();
                        params2.width = 0;
                        params2.weight = 2;
                        QuantityIncomed.setLayoutParams(params2);
                        QuantityIncomed.setPadding(padding, padding, padding, padding);
                        QuantityIncomed.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        TextView NameWares = new TextView(context);
                        NameWares.setText(item.NameWares);
                        NameWares.setTextColor(Color.parseColor("#000000"));
                        tr1.addView(NameWares);

                        LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams)NameWares.getLayoutParams();
                        params3.width = 0;
                        params3.weight = 7;
                        NameWares.setLayoutParams(params3);
                        NameWares.setPadding(padding, padding, padding, padding);
                        NameWares.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        tl0.addView(tr1);
                        tl0.addView(tr);

                        menuItems.add(tl0);

                        int index = model.indexOf(item);
                        if((index % 2)==0) {
                            ViewGroup rows = tl0;
                            for (int i = 0; i < rows.getChildCount(); i++) {
                                LinearLayout trc = (LinearLayout) rows.getChildAt(i);

                                for(int j = 0; j < trc.getChildCount(); j++){
                                    trc.getChildAt(j).setBackground(ContextCompat.getDrawable(context, R.drawable.odd_row_bordered));
                                }

                            }
                        }

                        tl.addView(tl0);
                    }

                    selectItem();


                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }

    public void AfterSave(String DocumentType){
        Intent i = new Intent(this,DocumentActivity.class);
        i.putExtra("document_type", DocumentType);
        this.finish();
        startActivity(i);
    }

    private void selectItem(){
        ViewGroup selectedItem = tl.findViewWithTag("selected");

        if(selectedItem != null){
            int index = menuItems.indexOf(selectedItem);
            if((index % 2)==0) {
                setBackgroundToTableRow(selectedItem, R.drawable.odd_row_bordered, "#000000");
            }else {
                setBackgroundToTableRow(selectedItem, R.drawable.table_cell_border, "#000000");
            }
            selectedItem.setTag(null);
        }
        ViewGroup currentRows = (ViewGroup) menuItems.get(current);
        menuItems.get(current).setTag("selected");
        setBackgroundToTableRow(currentRows, R.drawable.table_cell_selected, "#ffffff");
    }

    private void setBackgroundToTableRow(ViewGroup rows, int backgroundId, String textColor) {
        ViewGroup tr;

        if(documentType.equals("2")){
            tr = rows;
        }else{
            tr = (ViewGroup) rows.getChildAt(0);
        }
        for (int i = 0; i < tr.getChildCount(); i++) {
            ViewGroup row = (ViewGroup) tr.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView v = (TextView) row.getChildAt(j);
                v.setBackground(ContextCompat.getDrawable(context, backgroundId));
                if(v.getTag() != null && v.getTag().toString().equals("extInfo") && backgroundId != R.drawable.table_cell_selected) {
                    v.setTextColor(getResources().getColor(R.color.messageSuccess));
                }else{
                    v.setTextColor(Color.parseColor(textColor));
                }
            }
        }
    }

    private void selectNext(){
        if(current < menuItems.size()-1){
            current++;
            focusOnView("next");
        }
    }

    private void selectPrev(){
        if(current > 0){
            current--;
            focusOnView("prev");
        }
    }

    private final void focusOnView(final String prevent){
        documentList.post(new Runnable() {
            @Override
            public void run() {
                float d = context.getResources().getDisplayMetrics().density;
                Rect scrollBounds = new Rect();
                documentList.getDrawingRect(scrollBounds);

                float top = menuItems.get(current).getY();

                float bottom = top + menuItems.get(current).getHeight();

                if(documentType.equals("2"))
                    bottom = bottom + (int) (53 * d);
                if (scrollBounds.top < top && scrollBounds.bottom > bottom) {
                } else {
                    switch (prevent) {
                        case "next":
                            int dpValue = 30;
                            int padding = (int) (dpValue * d);
                            float invisiblePart = bottom - scrollBounds.bottom;
                            /*if(documentType.equals("2"))
                                padding = (int) (84 * d);*/
                            documentList.scrollTo(0, (documentList.getScrollY() + (int) invisiblePart + padding));
                            break;
                        case "prev":
                            documentList.scrollTo(0, menuItems.get(current).getTop());
                            break;
                    }
                }

            }
        });
    }
}
