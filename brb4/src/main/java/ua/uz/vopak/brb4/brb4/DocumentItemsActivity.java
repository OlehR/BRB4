package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.databinding.DocumentItemsLayoutBinding;
import ua.uz.vopak.brb4.brb4.models.Doc;
import ua.uz.vopak.brb4.brb4.models.DocModel;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.DocItemModel;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.Result;

public class DocumentItemsActivity extends Activity implements View.OnClickListener, ScanCallBack {
    private Scaner scaner;
    LinearLayout DataTable,DIOut;
    FrameLayout documentItemsFrame;
    ScrollView documentList;
    EditText NumberOut;
    Spinner DateOut;
    CheckBox IsClose;
    DocumentItemsLayoutBinding binding;
    final Context context=this;
    String NumberDoc;
    int TypeWeight=0;
    int TypeDoc;
    eTypeOrder TypeOrder = eTypeOrder.Scan;
    DocSetting DocSetting;
    DocItemModel DocItemModel = new DocItemModel();
    //List<DocWaresModel> InventoryItems;
    int current = 0;
    List<View> menuItems = new ArrayList<View>();
    List<WaresItemModel> ListWares;
    GlobalConfig config = GlobalConfig.instance();
    UtilsUI UtilsUI = new UtilsUI(this);
   
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_items_layout);
        documentList = findViewById(R.id.DocumentItemsList);
        documentItemsFrame = findViewById(R.id.DocumentItemsFrame);
        DataTable = findViewById(R.id.DI_Table);
        Intent i = getIntent();
        NumberDoc = i.getStringExtra("number");
        TypeDoc = i.getIntExtra("document_type",0);
        TypeWeight =i.getIntExtra("TypeWeight",0);
        DocSetting=config.GetDocSetting(TypeDoc);


        DIOut = findViewById(R.id.DI_OutLL);
        NumberOut=findViewById(R.id.DI_NumberOut);
        IsClose = findViewById(R.id.DI_IsClose);

       // binding=  DataBindingUtil.setContentView(this, R.layout.document_items_layout);
        //binding.setDWI (DocWaresItemModel);
        //Для отримання штрихкодів
        scaner=config.GetScaner();
        scaner.Init(this,savedInstanceState);

        DateOut = (Spinner) findViewById(R.id.DI_DateOut);
        DateOut.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
                // TODO Auto-generated method stub
                DocItemModel.ListDateIdx.set(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,DocItemModel.ListDate);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        DateOut.setAdapter(aa);

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
                                    UtilsUI.Dialog("Товар не знайдено", "Даний штрихкод=> "+pBarCode+" відсутній в базі");
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
                   UtilsUI.Dialog("Товар відсутній",pWIM.NameWares);
                }
            }
        });
    }


    void GetDoc()    {
        new AsyncHelper<DocModel>(new IAsyncHelper() {
            @Override
            public DocModel Invoke() {
                Doc d= config.Worker.GetDocOut(TypeDoc, NumberDoc);
                DocItemModel.SetDate(d.GetDateOutInvoice());
                DocItemModel.NumberOutInvoice.set(d.NumberOutInvoice);
                return config.Worker.GetDoc(TypeDoc, NumberDoc,1,TypeOrder);
            }
        },
                new IPostResult<DocModel>() {
                    @Override
                    public void Invoke(DocModel p) {
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
                case "136": //F6 Ввід даних розхідної накладної
                    SetViewOut();
                    break;
                case "137": //F7 генерація csv файла документа
                    GenCSV();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.DI_F2:
            case R.id.DI_F2_Text:
                SendDoc();
                break;
            case R.id.DI_F3:
            case R.id.DI_F3_Text:
                ExecuteDocumentScannerActivity();
                break;
            case R.id.DI_F4:
            case R.id.DI_F4_Text:
                ExecuteDocumentWeightActivity();
                break;
            case R.id.DI_F6:
            case R.id.DI_F6_Text:
                SetViewOut();
                break;

        }
    }

    private void GenCSV()
    {
        String FileName= NumberDoc+"_"+String.valueOf(TypeDoc)+".csv";
        StringBuilder sb=new StringBuilder();

        for (WaresItemModel item : ListWares) {
            if(item.InputQuantity>0)
                sb.append(String.format("%d;%.3f\n",item.CodeWares,item.InputQuantity));
        }

        String Text=sb.toString();

        try {
            Utils.SaveData(FileName, Text.getBytes("UTF-8"),true);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void SendDoc()    {
        GetDIM();
        final Date DateOut = DocItemModel.GetDate();
        final String NumberOut = DocItemModel.NumberOutInvoice.get();


        new AsyncHelper<Result>(new IAsyncHelper() {
            @Override
            public Result Invoke() {
                return config.Worker.UpdateDocState(1, TypeDoc, NumberDoc,DateOut,NumberOut,DocItemModel.IsClose);
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

    public void renderTable(final DocModel pDoc){

        final List<WaresItemModel> model=pDoc.WaresItem;
        ListWares=model;
        if(model.size() == 0)
            return;
        menuItems.clear();
        final DocumentItemsActivity context = this;
        //InventoryItems = inventoryModel;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DocItemModel.SetDate(pDoc.GetDateOutInvoice());
                DocItemModel.NumberOutInvoice.set(pDoc.NumberOutInvoice);
                RefreshOut();

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

                        UtilsUI.SetColor(TableBlock,"#000000","#"+((index % 2)==0?"FF":"60")+item.GetBackgroundColor());

                        /*if((index % 2)==0) {
                            ViewGroup rows = TableBlock;
                            for (int i = 0; i < rows.getChildCount(); i++) {
                                LinearLayout trc = (LinearLayout) rows.getChildAt(i);

                                for(int j = 0; j < trc.getChildCount(); j++){
                                    trc.getChildAt(j).setBackground(ContextCompat.getDrawable(context, R.drawable.odd_row_bordered));
                                }

                            }
                        }*/

                        //TableRow TableRowBlock = new TableRow(context);
                        //TableRowBlock.addView(TableBlock);
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
        if(pResult.State==0) {
            vMessage = "Документ успішно збережено!!!\n" + (pResult.Info != null ? pResult.Info : "");
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
         else {
            UtilsUI.Dialog("Помилка збереження документа:\n",pResult.TextError);
        }


    }

    private void selectItem(){
        ViewGroup selectedItem = DataTable.findViewWithTag("selected");

        if(selectedItem != null){
            int index = menuItems.indexOf(selectedItem);
            String BackgroundColor=ListWares.get(index).GetBackgroundColor();
            UtilsUI.SetColor(selectedItem,"#000000","#"+((index % 2)==0?"FF":"60")+BackgroundColor);
            selectedItem.setTag(null);
        }
        ViewGroup currentRows = (ViewGroup) menuItems.get(current);
        menuItems.get(current).setTag("selected");
        String BackgroundColor=ListWares.get(current).GetBackgroundColor();
        //setBackgroundToTableRow(currentRows, R.drawable.table_cell_selected, "#ffffff");
         UtilsUI.SetColor(currentRows,"#4c0099","#"+"A0"+BackgroundColor);
         //UtilsUI.SetColor(currentRows,"#ffffff","#008577");

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

    private  void SetViewOut()    {
        if(!DocSetting.IsViewOut)
            return;
        GetDIM();
        if(DocItemModel.IsView.get())
        {
            Doc d = new Doc(TypeDoc,NumberDoc);
            d.DateOutInvoice= DocItemModel.GetStrDate();
            d.NumberOutInvoice= DocItemModel.NumberOutInvoice.get();
            config.Worker.SaveDocOut(d);
        }

        DocItemModel.SetView();

        int view = DocItemModel.IsView.get() ? View.VISIBLE : View.INVISIBLE;
        DIOut.setVisibility(view);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,  DocItemModel.IsView.get()?120:0, 0, 0);
        documentItemsFrame.setLayoutParams(params);

    }

    private  void RefreshOut(){
        NumberOut.setText(DocItemModel.NumberOutInvoice.get());
        IsClose.setChecked(true);
        DateOut.setSelection(DocItemModel.ListDateIdx.get());
    }

    private void GetDIM()
    {
        DocItemModel.NumberOutInvoice.set(NumberOut.getText().toString());
        DocItemModel.IsClose= IsClose.isChecked()?1:0;
    }

}
