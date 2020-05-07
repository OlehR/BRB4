package ua.uz.vopak.brb4.brb4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

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
import ua.uz.vopak.brb4.brb4.models.DocumentViewModel;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;

public class DocumentActivity extends Activity implements View.OnClickListener, ScanCallBack {
    LinearLayout tl;
    ScrollView documentList;
    int DocumentType;
    DocumentActivity context;
    int current = 0;
    List<View> menuItems = new ArrayList<View>();
    GlobalConfig config = GlobalConfig.instance();
    private Scaner scaner;
    TextView  FilterKey,FilterText,FilterEDRPOText,FilterEDRPO;
    EditText DocumentZKPO;
    DocumentViewModel DM= new DocumentViewModel();

    DocumentLayoutBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.document_layout);
        tl = findViewById(R.id.RevisionsList);
        documentList = findViewById(R.id.DocumentList);
        DocumentZKPO =findViewById(R.id.DocumentZKPO);
        FilterEDRPO =findViewById(R.id.FilterEDRPO);
        FilterEDRPOText =findViewById(R.id.FilterEDRPOText);

        context = this;

        Intent i = getIntent();
        DocumentType =  i.getIntExtra("document_type",0);

        DM.TypeDoc.set(DocumentType);
        //binding = DataBindingUtil.setContentView(this, R.layout.document_layout);
        //binding.setDM (DM);

        scaner=config.GetScaner();
        scaner.Init(this,savedInstanceState);

        FilterKey=findViewById(R.id.FilterKey);
        FilterText=findViewById(R.id.FilterText);
        //new AsyncLoadListDoc(GlobalConfig.GetWorker(), this).execute(DocumentType);

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.LoadListDoc(context,DocumentType,null,null);
                return null;
            }
        }).execute();
        ViewFilter();
    }

    private void ViewFilter()
    {
        //!!!!TMP Через проблеми з дата біндінгом Жутко костиляю. Буду мати змогу - перероблю. Бо програмно будую Грід.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int view = DM.IsFilter.get() ? View.VISIBLE : View.INVISIBLE;
                FilterKey.setVisibility(view);
                FilterText.setVisibility(view);


                view = DM.IsEnterCodeZKPO.get() ? View.VISIBLE : View.GONE;
                DocumentZKPO.setText(DM.ZKPO.get());
                DocumentZKPO.setVisibility(view);
                DocumentZKPO.setFocusable(DM.IsEnterCodeZKPO.get());

                view = DM.TypeDoc.get() == 2 ? View.VISIBLE : View.GONE;
                FilterEDRPO.setVisibility(view);
                FilterEDRPOText.setVisibility(view);
            }});
    }

    @Override
    public void Run(final String parBarCode) {

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.LoadListDoc(context,DocumentType,parBarCode,null);

                return null;
            }
        }).execute();
        DM.IsFilter.set(true);
        ViewFilter();

        /*
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetRevisionScannerData(parBarCode, context);
                return null;
            }
        }).execute();*/
    }

    @Override
    public void onClick(View v) {
        TextView currentNumber = v.findViewWithTag("number_inv");
        Intent i;
        Object tag = ((ViewGroup)v).getChildAt(0).getTag();
        if(tag != null && tag.toString().equals("2")){
            i = new Intent(context, DocumentWeightActivity.class);
        }else{
          i = new Intent(context, DocumentItemsActivity.class);
        }
        i.putExtra("number", currentNumber.getText().toString());
        i.putExtra("document_type", DocumentType);
        startActivity(i);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            String keyCode = String.valueOf(event.getKeyCode());
            switch (keyCode) {
                case "15":
                    selectNext();
                    selectItem();
                    break;
                case "9":
                    selectPrev();
                    selectItem();
                    break;
                case "66":
                    if(DM.IsEnterCodeZKPO.get())
                    {
                        new AsyncHelper<Void>(new IAsyncHelper() {
                            @Override
                            public Void Invoke() {

                                String find= DocumentZKPO.getText().toString().replace("\n","").replace(" ","");
                                DM.ZKPO.set("");
                                DM.IsEnterCodeZKPO.set(false);
                                DM.IsFilter.set(true);
                                ViewFilter();
                                config.Worker.LoadListDoc(context, DocumentType, null,find);
                                return null;
                            }
                        }).execute();
                    }
                    else
                    tl.findViewWithTag("selected").callOnClick();
                    break;
                case "131": //F2 Пошук по коду ЄДРПОУ для прихідних
                  DM.IsEnterCodeZKPO.set(true);
                  ViewFilter();
                    break;
                case "132": //F2 Перерисовуємо без фільтра
                    new AsyncHelper<Void>(new IAsyncHelper() {
                        @Override
                        public Void Invoke() {
                            config.Worker.LoadListDoc(context, DocumentType, null,null);

                            return null;
                        }
                    }).execute();
                    DM.IsFilter.set(false);
                    DM.IsEnterCodeZKPO.set(false);
                    ViewFilter();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void renderTable(final List<DocumentModel> model){
        current=0;
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
                        tl.removeAllViews();
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
                            ExtInfo.setTag("extInfo");
                            ExtInfo.setTextColor(ContextCompat.getColor(context,R.color.messageSuccess));
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
                            tl0.setTag(item.WaresType);

                            tr0.addView(tl0);
                            tr0.setOnClickListener(context);
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
                        }

                        selectItem();
                    ViewFilter();
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
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
        currentRows.setTag("selected");
        setBackgroundToTableRow(currentRows, R.drawable.table_cell_selected, "#ffffff");
    }

    private void setBackgroundToTableRow(ViewGroup rows, int backgroundId, String textColor) {
        ViewGroup tr = (ViewGroup) rows.getChildAt(0);
        for (int i = 0; i < tr.getChildCount(); i++) {
            ViewGroup row = (ViewGroup) tr.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView v = (TextView) row.getChildAt(j);
                v.setBackground(ContextCompat.getDrawable(context, backgroundId));
                if(v.getTag() != null && v.getTag().toString().equals("extInfo") && backgroundId != R.drawable.table_cell_selected) {
                    v.setTextColor(ContextCompat.getColor(context,R.color.messageSuccess));
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
