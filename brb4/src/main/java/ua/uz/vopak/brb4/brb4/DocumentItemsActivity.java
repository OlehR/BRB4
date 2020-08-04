package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.Result;

public class DocumentItemsActivity extends Activity implements View.OnClickListener, ScanCallBack {
    private Scaner scaner;
    LinearLayout DataTable,button;
    FrameLayout documentItemsFrame;
    ScrollView documentList;
    final Context context=this;
    Button btn, btnSave;
    String NumberDoc;
    int TypeWeight=0;
    int TypeDoc;
    eTypeOrder TypeOrder = eTypeOrder.Scan;
    DocSetting DocSetting;
    //List<DocWaresModel> InventoryItems;
    int current = 0;
    List<View> menuItems = new ArrayList<View>();
    List<WaresItemModel> ListWares;
    GlobalConfig config = GlobalConfig.instance();
    UtilsUI UtilsUI = new UtilsUI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_items_layout);
        documentList = findViewById(R.id.DocumentItemsList);
        documentItemsFrame = findViewById(R.id.DocumentItemsFrame);
        DataTable = findViewById(R.id.InventoriesList);
        Intent i = getIntent();
        NumberDoc = i.getStringExtra("number");
        TypeDoc = i.getIntExtra("document_type",0);
        TypeWeight =i.getIntExtra("TypeWeight",0);
        DocSetting=config.GetDocSetting(TypeDoc);
        btn = findViewById(R.id.F4);
        btnSave = findViewById(R.id.F3);
        button = findViewById(R.id.DI_Button);
        button.setVisibility(config.TypeScaner== eTypeScaner.Camera? View.VISIBLE:View.GONE );
        btn.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        //Для отримання штрихкодів
        scaner=config.GetScaner();
        scaner.Init(this,savedInstanceState);

    }
    @Override
    public void onResume() {
        super.onResume();
        GetDoc();
        //Zebra
        scaner.Init(this);
        scaner.StartScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Zebra
        scaner.StopScan();
    }
    @Override
    public void Run(final String pBarCode) {

        new AsyncHelper<WaresItemModel>(
                new IAsyncHelper<WaresItemModel>() {
                    @Override
                    public WaresItemModel Invoke() {

                        WaresItemModel res= config.Worker.GetWaresFromBarcode(TypeDoc,NumberDoc,pBarCode);
                        if(res==null)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Dialog("Товар не знайдено", "Даний штрихкод=> "+pBarCode+" відсутній в базі");
                                }});

                        return res;

                        //return config.Worker.GetWaresFromBarcode(TypeDoc,NumberDoc,pBarCode);
                    }
                },
                new IPostResult<WaresItemModel>() {
                    @Override
                    public void Invoke(WaresItemModel model) {
                        Find(model);
                    }
                }).execute();
    }
    void Find(final WaresItemModel pWIM) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                int i = 0;
                for (WaresItemModel wim : ListWares) {
                    if (wim.CodeWares == pWIM.CodeWares)
                        break;
                    i++;
                }
                if (i < menuItems.size()) {
                    current = i;
                    focusOnView("prev");
                    selectItem();
                }
                else
                {
                    Dialog("Товар відсутній",pWIM.NameWares);

                }
            }
        });
    }

 void Dialog(String pHead,String pText)
 {
     new AlertDialog.Builder(context)
             .setTitle(pHead)
             .setMessage(pText)
             .setPositiveButton(android.R.string.ok, null)
             .create().show();

 }

    void GetDoc()
    {
        new AsyncHelper<List<WaresItemModel>>(new IAsyncHelper() {
            @Override
            public List<WaresItemModel> Invoke() {
                return config.Worker.GetDoc(TypeDoc, NumberDoc,1,TypeOrder);
            }
        },
                new IPostResult<List<WaresItemModel>>() {
                    @Override
                    public void Invoke(List<WaresItemModel> p) {
                        renderTable(p);
                        return;
                    }}).execute();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        String keyCode = String.valueOf(event.getKeyCode());
        if(event.getAction() == KeyEvent.ACTION_UP) {
            switch (keyCode) {
                case "9": //2
                    selectPrev();
                    selectItem();
                    break;
                case "15": //8
                    selectNext();
                    selectItem();
                    break;
                case "132":
                    SendDoc(); //F2 Зберегти (відправити документ в 1С
                    break;
                case "133": //F3 Режим сканування.
                    ExecuteDocumentScannerActivity();
                    break;
                case "134": //F4 Ваговий режим
                    ExecuteDocumentWeightActivity();
                    break;
                case "135": //F5 Сортування Назва,Порядок сканування
                    TypeOrder= (TypeOrder == eTypeOrder.Scan)? eTypeOrder.Name :eTypeOrder.Scan;
                    GetDoc();
                    break;

            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.F4:
                ExecuteDocumentScannerActivity();
                break;
            case R.id.F3:
                SendDoc();
                break;
        }
    }

    public void SendDoc()    {
        new AsyncHelper<Result>(new IAsyncHelper() {
            @Override
            public Result Invoke() {
                return config.Worker.UpdateDocState(1, TypeDoc, NumberDoc);
            }
        },
                new IPostResult<Result>() {
                    @Override
                    public void Invoke(Result p) {
                        AfterSave(p);
                        return;
                    }}
        ).execute();
    }

    public void ExecuteDocumentScannerActivity() {
        Intent i = new Intent(this, DocumentScannerActivity.class);
        i.putExtra("number", NumberDoc);
        i.putExtra("document_type", TypeDoc);
        startActivity(i);
    }
    public void ExecuteDocumentWeightActivity() {
        Intent i = new Intent(this, DocumentWeightActivity.class);
        i.putExtra("number", NumberDoc);
        i.putExtra("document_type", TypeDoc);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        GetDoc();
    }

    public void renderTable(final List<WaresItemModel> model){
        ListWares=model;
        if(model.size() == 0)
            return;
        menuItems.clear();
        final DocumentItemsActivity context = this;
        //InventoryItems = inventoryModel;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                int dpValue = 3;
                float d = context.getResources().getDisplayMetrics().density;
                int padding = (int)(dpValue * d);

                DataTable.removeAllViews();

                LinearLayout tlTitle = new LinearLayout(context);
                tlTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                tlTitle.setOrientation(LinearLayout.VERTICAL);

                LinearLayout Line1H = new LinearLayout(context);
                Line1H.setOrientation(LinearLayout.HORIZONTAL);
                Line1H.setWeightSum(7f);

                TextView NameWaresT = new TextView(context);
                NameWaresT.setText("Назва");
                NameWaresT.setTextColor(Color.parseColor("#000000"));
                Line1H.addView(NameWaresT);
                LinearLayout.LayoutParams params3T = (LinearLayout.LayoutParams)NameWaresT.getLayoutParams();
                params3T.width = 0;
                params3T.weight = 7;
                NameWaresT.setLayoutParams(params3T);
                NameWaresT.setPadding(padding, padding, padding, padding);
                NameWaresT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));


                tlTitle.addView(Line1H);


                LinearLayout Line2H = new LinearLayout(context);
                Line2H.setOrientation(LinearLayout.HORIZONTAL);

                float WS = 4f+ (DocSetting.IsViewPlan?2f:0f)+(DocSetting.IsViewReason?2f:0f);
                Line2H.setWeightSum(WS);

                TextView CodeWaresT = new TextView(context);
                CodeWaresT.setText("Код");
                CodeWaresT.setTextColor(Color.parseColor("#000000"));
                Line2H.addView(CodeWaresT);
                LinearLayout.LayoutParams paramsT = (LinearLayout.LayoutParams)CodeWaresT.getLayoutParams();
                paramsT.width = 0;
                paramsT.weight = 2;
                CodeWaresT.setLayoutParams(paramsT);
                CodeWaresT.setPadding(padding, padding, padding, padding);
                CodeWaresT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                if( DocSetting.IsViewPlan) {
                    TextView QuantityOrderedT = new TextView(context);
                    QuantityOrderedT.setText("План");
                    QuantityOrderedT.setTextColor(Color.parseColor("#000000"));
                    Line2H.addView(QuantityOrderedT);
                    LinearLayout.LayoutParams params1T = (LinearLayout.LayoutParams) QuantityOrderedT.getLayoutParams();
                    params1T.width = 0;
                    params1T.weight = 2;
                    QuantityOrderedT.setLayoutParams(params1T);
                    QuantityOrderedT.setPadding(padding, padding, padding, padding);
                    QuantityOrderedT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                }

                TextView QuantityIncomedT = new TextView(context);
                QuantityIncomedT.setText("Факт");
                QuantityIncomedT.setTextColor(Color.parseColor("#000000"));
                Line2H.addView(QuantityIncomedT);
                LinearLayout.LayoutParams params2T = (LinearLayout.LayoutParams)QuantityIncomedT.getLayoutParams();
                params2T.width = 0;
                params2T.weight = 2;
                QuantityIncomedT.setLayoutParams(params2T);
                QuantityIncomedT.setPadding(padding, padding, padding, padding);
                QuantityIncomedT.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                if( DocSetting.IsViewReason) {
                    TextView QuantityReason = new TextView(context);
                    QuantityReason.setText("Пробл.");
                    QuantityReason.setTextColor(Color.parseColor("#000000"));
                    Line2H.addView(QuantityReason);
                    LinearLayout.LayoutParams params1T = (LinearLayout.LayoutParams) QuantityReason.getLayoutParams();
                    params1T.width = 0;
                    params1T.weight = 2;
                    QuantityReason.setLayoutParams(params1T);
                    QuantityReason.setPadding(padding, padding, padding, padding);
                    QuantityReason.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                }
                tlTitle.addView(Line2H);

                documentList.setPadding((int)(d * 5  ), (int)(d * 54  ),(int)(d * 5  ),(int)(d * 25  ));
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tlTitle.getLayoutParams();
                layoutParams.setMargins((int)(d * 5  ),(int)(d * 5  ),(int)(d * 5  ),0 );
                tlTitle.setLayoutParams(layoutParams);

                documentItemsFrame.addView(tlTitle);


                try {

                    for (WaresItemModel item : model) {

                        LinearLayout TableBlock = new LinearLayout(context);
                        TableBlock.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        TableBlock.setOrientation(LinearLayout.VERTICAL);



                        LinearLayout Line1 = new LinearLayout(context);
                        Line1.setOrientation(LinearLayout.HORIZONTAL);
                        Line1.setWeightSum(7f);

                        TextView NameWares = new TextView(context);
                        NameWares.setText(item.NameWares);
                        NameWares.setTextColor(Color.parseColor("#000000"));
                        Line1.addView(NameWares);

                        LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams)NameWares.getLayoutParams();
                        params3.width = 0;
                        params3.weight = 7;
                        NameWares.setLayoutParams(params3);
                        NameWares.setPadding(padding, padding, padding, padding);
                        NameWares.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        TableBlock.addView(Line1);

                        LinearLayout Line2 = new LinearLayout(context);
                        Line2.setOrientation(LinearLayout.HORIZONTAL);
                        Line2.setWeightSum(WS);

                        TextView CodeWares = new TextView(context);
                        CodeWares.setText(item.GetCodeWares());
                        CodeWares.setTextColor(Color.parseColor("#000000"));
                        Line2.addView(CodeWares);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)CodeWares.getLayoutParams();
                        params.width = 0;
                        params.weight = 2;
                        CodeWares.setLayoutParams(params);
                        CodeWares.setPadding(padding, padding, padding, padding);
                        CodeWares.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        if( DocSetting.IsViewPlan) {
                            TextView QuantityOrdered = new TextView(context);
                            QuantityOrdered.setText(item.GetQuantityOrder());
                            QuantityOrdered.setTextColor(Color.parseColor("#000000"));
                            Line2.addView(QuantityOrdered);

                            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) QuantityOrdered.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 2;
                            QuantityOrdered.setLayoutParams(params1);
                            QuantityOrdered.setPadding(padding, padding, padding, padding);
                            QuantityOrdered.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                        }

                        TextView QuantityIncomed = new TextView(context);
                        QuantityIncomed.setText(item.GetInputQuantity());
                        QuantityIncomed.setTextColor(Color.parseColor("#000000"));
                        Line2.addView(QuantityIncomed);

                        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)QuantityIncomed.getLayoutParams();
                        params2.width = 0;
                        params2.weight = 2;
                        QuantityIncomed.setLayoutParams(params2);
                        QuantityIncomed.setPadding(padding, padding, padding, padding);
                        QuantityIncomed.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                        if( DocSetting.IsViewReason) {
                            TextView QuantityReason = new TextView(context);
                            QuantityReason.setText(item.GetQuantityReason());
                            QuantityReason.setTextColor(Color.parseColor("#000000"));
                            Line2.addView(QuantityReason);

                            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) QuantityReason.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 2;
                            QuantityReason.setLayoutParams(params1);
                            QuantityReason.setPadding(padding, padding, padding, padding);
                            QuantityReason.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                        }

                        TableBlock.addView(Line2);

                        menuItems.add(TableBlock);

                        int index = model.indexOf(item);

                        UtilsUI.SetColor(TableBlock,"#000000","#"+((index % 2)==0?"FF":"70")+item.GetBackgroundColor());

                        /*if((index % 2)==0) {
                            ViewGroup rows = TableBlock;
                            for (int i = 0; i < rows.getChildCount(); i++) {
                                LinearLayout trc = (LinearLayout) rows.getChildAt(i);

                                for(int j = 0; j < trc.getChildCount(); j++){
                                    trc.getChildAt(j).setBackground(ContextCompat.getDrawable(context, R.drawable.odd_row_bordered));
                                }

                            }
                        }*/

                        DataTable.addView(TableBlock);
                    }

                    selectItem();


                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
    }

    public void AfterSave(final Result pResult) {
        final String vMessage;
        if(pResult.State==-1)
            vMessage="Документ успішно збережено!!!\n"+pResult.Info!=null?pResult.Info:"";
         else
            vMessage="Помилка збереження документа:\n"+pResult.TextError;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, vMessage, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        Intent i = new Intent(this, DocumentActivity.class);
        i.putExtra("document_type", TypeDoc);
        this.finish();
        startActivity(i);
    }

    private void selectItem(){
        ViewGroup selectedItem = DataTable.findViewWithTag("selected");

        if(selectedItem != null){
            int index = menuItems.indexOf(selectedItem);
            String BackgroundColor=ListWares.get(index).GetBackgroundColor();
            UtilsUI.SetColor(selectedItem,"#000000","#"+((index % 2)==0?"FF":"70")+BackgroundColor);
            selectedItem.setTag(null);
        }
        ViewGroup currentRows = (ViewGroup) menuItems.get(current);
        menuItems.get(current).setTag("selected");
        //setBackgroundToTableRow(currentRows, R.drawable.table_cell_selected, "#ffffff");
        UtilsUI.SetColor(currentRows,"#ffffff","#008577");
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

                if(TypeDoc ==2)
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
