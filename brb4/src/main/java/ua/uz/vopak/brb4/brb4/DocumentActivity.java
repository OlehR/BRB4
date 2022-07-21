package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.databinding.DocumentLayoutBinding;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.DocumentViewModel;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.Config;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;

public class DocumentActivity extends Activity implements View.OnClickListener, ScanCallBack {
    DocSetting DS;
    LinearLayout Table;
    ScrollView documentList;
    int DocumentType;
    DocumentActivity context;
    List<DocumentModel> modelDoc;
    int current = 0;
    List<View> menuItems = new ArrayList<View>();
    Config config = Config.instance();
    UtilsUI UtilsUI = new UtilsUI( this);
    private Scaner scaner;
    TextView  FilterKey,FilterText,FilterEDRPOText,FilterEDRPO;
    EditText DocumentZKPO;
    DocumentViewModel DM= new DocumentViewModel();

    DocumentLayoutBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_layout);
        Table = findViewById(R.id.RevisionsList);
        documentList = findViewById(R.id.DocumentList);
        DocumentZKPO =findViewById(R.id.DocumentZKPO);
        FilterEDRPO =findViewById(R.id.FilterEDRPO);
        FilterEDRPOText =findViewById(R.id.FilterEDRPOText);

        context = this;

        Intent i = getIntent();
        DocumentType =  i.getIntExtra("document_type",0);

        DS=config.GetDocSetting(DocumentType);
        DM.TypeDoc.set(DocumentType);
        //binding = DataBindingUtil.setContentView(this, R.layout.document_layout);
        //binding.setDM (DM);

        scaner=config.GetScaner();
        if(scaner!=null)
            scaner.Init(this,savedInstanceState);

        FilterKey=findViewById(R.id.FilterKey);
        FilterText=findViewById(R.id.FilterText);
        //new AsyncLoadListDoc(GlobalConfig.GetWorker(), this).execute(DocumentType);

     //  RefreshTable(null,null);
    }


    @Override
    public void onResume() {
        super.onResume();
        //Zebra
        if(scaner!=null) {
            scaner.Init(this);
            scaner.StartScan();
        }
        RefreshTable(null,null);
    }

    private void ViewFilter()
    {
        //!!!!TMP Через проблеми з дата біндінгом  костиляю. Буду мати змогу - перероблю. Бо програмно будую Грід.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int view = DM.IsFilter.get() ? View.VISIBLE : View.INVISIBLE;
                FilterKey.setVisibility(view);
                FilterText.setVisibility(view);

                view = DM.IsEnterCodeZKPO.get() ? View.VISIBLE : View.GONE;
                DocumentZKPO.setText(DM.ZKPO.get());
                DocumentZKPO.setVisibility(view);

                DocumentZKPO.requestFocus();
                DocumentZKPO.setFocusableInTouchMode(true);
                DocumentZKPO.requestFocusFromTouch();
                DocumentZKPO.setFocusableInTouchMode(false);

                view = DM.TypeDoc.get() == 2 ? View.VISIBLE : View.GONE;
                FilterEDRPO.setVisibility(view);
                FilterEDRPOText.setVisibility(view);
            }});
    }

    @Override
    public void Run(final String pBarCode) {
        RefreshTable(pBarCode,null);
    }

    private void RefreshTable(final String pBarCode,final String pExtInfo)    {
        DM.IsFilter.set(pBarCode!=null ||pExtInfo!=null);
        DM.IsEnterCodeZKPO.set(false);

        new AsyncHelper<List<DocumentModel>>(new IAsyncHelper() {
            @Override
            public List<DocumentModel> Invoke() {
                if(DS==null)
                    return null;
                if(DS.IsAddBarCode)
                    config.Worker.LoadData(DocumentType,pBarCode,null,false);
                return config.Worker.LoadListDoc(DocumentType,pBarCode,pExtInfo);
            }
        },
                new IPostResult<List<DocumentModel>>() {
                    @Override
                    public void Invoke(List<DocumentModel> p) {
                        renderTable(p);
                        return;
                    }}).execute();
    }

    @Override
    public void onClick(View v) {
        TextView currentNumber = v.findViewWithTag("number");
        Intent i;

        Object tag = ((ViewGroup)v).getChildAt(0).getTag();
        int TypeWeight = tag != null?Integer.parseInt( tag.toString()):0;


 //       if(tag != null && (tag.toString().equals("2")|| tag.toString().equals("0")))
//            i = new Intent(context, DocumentWeightActivity.class);
 //       else
          i = new Intent((Context) this, DocumentItemsActivity.class);

        i.putExtra("number", currentNumber.getText().toString());
        i.putExtra("document_type", DocumentType);
        i.putExtra("TypeWeight", TypeWeight);
        startActivity(i);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_2:
                    if(!DM.IsEnterCodeZKPO.get())
                        if(keyCode==KeyEvent.KEYCODE_DPAD_UP || config.TypeScaner != eTypeScaner.Zebra)  selectPrev(); else selectNext();
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                case KeyEvent.KEYCODE_8:
                    if(!DM.IsEnterCodeZKPO.get())
                        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN || config.TypeScaner!= eTypeScaner.Zebra)  selectNext(); else selectPrev();
                    break;

                case KeyEvent.KEYCODE_ENTER:
                    if(DM.IsEnterCodeZKPO.get()) {
                        String find = DocumentZKPO.getText().toString().replace("\n", "").replace(" ", "");
                        DM.ZKPO.set("");
                        RefreshTable(null, find);
                    }
                    else {
                        View r = Table.findViewWithTag("selected");
                        if(r!=null)
                            r.callOnClick();
                    }
                    break;
                case  131: //F2 Пошук по коду ЄДРПОУ для прихідних
                  DM.IsEnterCodeZKPO.set(true);
                  ViewFilter();
                    break;
                case 132: //F2 Перерисовуємо без фільтра
                    RefreshTable(null, null);
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void renderTable(final List<DocumentModel> model){
        current=0;
        modelDoc=model;
        final DocumentActivity context = this;
        menuItems.clear();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                        int dpValue = 10;
                        float d = context.getResources().getDisplayMetrics().density;
                        dpValue = 3;
                        int padding = (int)(dpValue * d);
                        LinearLayout.LayoutParams lp;
                        Table.removeAllViews();
                        for (DocumentModel item : model) {

                            String date = item.DateDoc;
                            String numberInv = item.NumberDoc;
                            String Description = item.Description;
                            String userName = item.NameUser;

                            LinearLayout tl0 = new LinearLayout(context);
                            tl0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tl0.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout tr0 = new LinearLayout(context);
                            tr0.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            tr0.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout Line1 = new LinearLayout(context);
                            Line1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            Line1.setOrientation(LinearLayout.HORIZONTAL);
                            Line1.setWeightSum(2f);


                            TextView Date = new TextView(context);
                            Date.setText(date);
                            Date.setTextColor(Color.parseColor("#000000"));
                            Line1.addView(Date);

                            Date.setPadding(padding, padding, padding, padding);
                            Date.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)Date.getLayoutParams();
                            params.width = 0;
                            params.weight = 1;
                            Date.setLayoutParams(params);

                            TextView NumberInv = new TextView(context);
                            NumberInv.setText(numberInv);
                            NumberInv.setTextColor(Color.parseColor("#000000"));
                            NumberInv.setTag("number");
                            Line1.addView(NumberInv);

                            NumberInv.setPadding(padding, padding, padding, padding);
                            NumberInv.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));
                            LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams)NumberInv.getLayoutParams();
                            params1.width = 0;
                            params1.weight = 1;
                            NumberInv.setLayoutParams(params1);
                            tl0.addView(Line1);


                            LinearLayout Line2 = new LinearLayout(context);
                            Line2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            Line2.setOrientation(LinearLayout.HORIZONTAL);
                            Line2.setWeightSum(2f);

                            TextView eDescription = new TextView(context);
                            eDescription.setText(Description);
                            eDescription.setTag("extInfo");
                            eDescription.setTextColor(ContextCompat.getColor(context,R.color.messageSuccess));
                            Line2.addView(eDescription);

                            LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams)eDescription.getLayoutParams();
                            params2.width = 0;
                            params2.weight = 2;
                            eDescription.setLayoutParams(params2);
                            eDescription.setPadding(padding, padding, padding, padding);
                            eDescription.setBackground(ContextCompat.getDrawable(context, R.drawable.table_cell_border));

                            tl0.addView(Line2);

                            if(DS.IsShowUser) {
                                LinearLayout Line3 = new LinearLayout(context);
                                Line3.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                Line3.setOrientation(LinearLayout.HORIZONTAL);
                                Line3.setWeightSum(2f);
                                TextView UserName = new TextView(context);
                                UserName.setText(userName);
                                UserName.setTextColor(Color.parseColor("#000000"));
                                Line3.addView(UserName);

                                LinearLayout.LayoutParams params3 = (LinearLayout.LayoutParams) UserName.getLayoutParams();
                                params3.width = 0;
                                params3.weight = 2;
                                UserName.setLayoutParams(params3);
                                UserName.setPadding(padding, padding, padding, padding);
                                UserName.setBackground(ContextCompat.getDrawable(context, R.drawable.row_border));

                                tl0.addView(Line3);
                            }
                            tl0.setTag(item.WaresType);

                            tr0.addView(tl0);
                            tr0.setOnClickListener(context);
                            menuItems.add(tr0);

                            int index = model.indexOf(item);
                            UtilsUI.SetColor(tl0,"#000000","#"+((index % 2)==0?"FF":"80")+item.GetBackgroundColor());

                            Table.addView(tr0);
                        }

                        selectItem();

                } catch (Exception e) {
                    e.getMessage();
                }
                ViewFilter();
            }
        });
    }

    private void selectItem(){
        ViewGroup selectedItem = Table.findViewWithTag("selected");
        if(selectedItem != null){
            int index = menuItems.indexOf(selectedItem);
            UtilsUI.SetColor(selectedItem,"#000000","#"+((index % 2)==0?"FF":"80")+modelDoc.get(index).GetBackgroundColor()); //"fff3cd";
            selectedItem.setTag(null);
        }
        ViewGroup currentRows = (ViewGroup) menuItems.get(current);
        currentRows.setTag("selected");
        UtilsUI.SetColor(currentRows,"#ffffff","#008577");
    }


    private void selectNext(){
        if(current < menuItems.size()-1){
            current++;
            focusOnView("next");
            selectItem();
        }
    }

    private void selectPrev(){
        if(current > 0){
            current--;
            focusOnView("prev");
            selectItem();
        }
    }

    private final void focusOnView(final String prevent){
        documentList.post(new Runnable() {
            @Override
            public void run() {
                Rect scrollBounds = new Rect();
                documentList.getDrawingRect(scrollBounds);

                float top = menuItems.get(current).getY();
                float bottom = top + menuItems.get(current).getHeight();

                if (scrollBounds.top < top && scrollBounds.bottom > bottom) {
                }else{
                    switch (prevent){
                        case "next":
                            int dpValue = 30;
                            float d = context.getResources().getDisplayMetrics().density;
                            int padding = (int)(dpValue * d);
                            float invisiblePart = bottom - scrollBounds.bottom;
                            documentList.scrollTo(0, (documentList.getScrollY() + (int)invisiblePart + padding));
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
