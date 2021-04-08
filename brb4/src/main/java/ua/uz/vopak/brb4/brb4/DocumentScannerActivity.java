package ua.uz.vopak.brb4.brb4;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.databinding.DocumentScannerActivityBinding;
import ua.uz.vopak.brb4.brb4.helpers.MyKeyboard;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.brb4.models.Reason;
import ua.uz.vopak.brb4.lib.enums.MessageType;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeControlDoc;
import ua.uz.vopak.brb4.lib.enums.eTypeOrder;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.helpers.Utils;
import ua.uz.vopak.brb4.lib.helpers.UtilsUI;
import ua.uz.vopak.brb4.lib.models.Result;
import android.hardware.Camera;
public class DocumentScannerActivity extends FragmentActivity implements View.OnClickListener,ScanCallBack, IIncomeRender {
   String  TAG="DocumentScannerActivity";
    EditText barCode,  inputCount,AllCount;
    private Scaner scaner;
    ScrollView scrollView;
    //RelativeLayout loader;
    TableLayout WaresTableLayout;
    BarcodeView barcodeView;
    MyKeyboard keyboard;

    Activity context;
    GlobalConfig config = GlobalConfig.instance();
    Utils utils =  Utils.instance(this);
    ua.uz.vopak.brb4.lib.helpers.UtilsUI UtilsUI = new UtilsUI(this);
    DocumentScannerActivityBinding binding;

    List<WaresItemModel> ListWares;
    WaresItemModel WaresItem ;//= new WaresItemModel(this);
    DocSetting DocSetting;
    InputMethodManager imm;
    int padding;

    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;
    // Калбек штрихкода з камери.
    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                //barcodeView.pause();
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 1000);
                toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,250);
                Run(result.getText());//config.Scaner.CallBack.
            }
        }
        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.document_scanner_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = DataBindingUtil.setContentView(this, R.layout.document_scanner_activity);
        barcodeView=findViewById(R.id.DS_scanner);
        barcodeView.setCameraSettings(config.GetCameraSettings());

        AllCount=findViewById(R.id.DS_Count);
        WaresItem = new WaresItemModel(barcodeView);
        binding.setWaresItem(WaresItem);

        //int camNum=Camera.getNumberOfCameras();

        Intent i = getIntent();


        float d = this.getResources().getDisplayMetrics().density;
        int dpValue = 3;
        padding = (int) (dpValue * d);


        ListWares = new ArrayList<>();
        WaresItem.ClearData();

        WaresItem.TypeDoc = i.getIntExtra("document_type", 0);
        WaresItem.NumberDoc = i.getStringExtra("number");
        WaresItem.ListReason.clear();
        for (Reason el: config.Reasons)
            WaresItem.ListReason.add(el.NameReason);

        DocSetting=config.GetDocSetting(WaresItem.TypeDoc);
        WaresItem.DocSetting=DocSetting;

        barCode = findViewById(R.id.DS_BarCode);
        inputCount = findViewById(R.id.DS_InputCount);

        WaresTableLayout = findViewById(R.id.DS_ScanItemsTable);
        scrollView = findViewById(R.id.DS_ScrollView);


        if(config.IsUseCamera()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(android.Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_ACCESS_CAMERA);
            } else {
                //barcodeView.setVisibility(View.VISIBLE);
                barcodeView.decodeContinuous(callback);
                barcodeView.resume();
            }
        }
        keyboard = (MyKeyboard) findViewById(R.id.keyboard);
        Refresh();

        inputCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                //Refresh();
                AllCount.setText(WaresItem.GetQuantityBase());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    try {
                        WaresItem.InputQuantity = Float.parseFloat(inputCount.getText().toString().replace(",", "."));
                    }
                    catch (Exception e) {
                        WaresItem.InputQuantity =0;
                        Utils.WriteLog("e",TAG, "InputQuantity=>" +inputCount.getText().toString()+ " "+ e.getMessage());
                    }

                }
            }
        });

        GetDoc();

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(true);
        //Для отримання штрихкодів
        scaner=config.GetScaner();
        scaner.Init(this,savedInstanceState);

        imm = (InputMethodManager)   getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    @Override
    public void onResume() {
        super.onResume();


        if(config.IsUseCamera())
            barcodeView.resume();
        //Zebra
        scaner.StartScan();
        //IntentIntegrator.forSupportFragment(this).setBeepEnabled(true);
        Date curDate = null;
        try {
            curDate = config.FormatterDate.parse(config.FormatterDate.format(new Date()));
        } catch (Exception ex) {
        }

        if (config.LastFullUpdate == null || !config.LastFullUpdate.equals( curDate)){
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            UtilsUI.Dialog("Необхідно оновити довідники", "Останне оновлення=>" + dateFormat.format(config.LastFullUpdate));
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.DS_F1:
            case R.id.DS_F1_Text:
                setNullToExistingPosition();
                break;
            case R.id.DS_F2:
            case R.id.DS_F2_Text:
                focusOnView("up");
                break;
            case R.id.DS_F3:
            case R.id.DS_F3_Text:
                focusOnView("down");
                break;
            case R.id.DS_F8:
            case R.id.DS_F8_Text:
                saveDocumentItem(false,false);
                break;


        }
    }

    void GetDoc()    {
        new AsyncHelper<List<WaresItemModel>>(new IAsyncHelper() {
            @Override
            public List<WaresItemModel> Invoke() {
                 return  config.Worker.GetDocWares(WaresItem.TypeDoc,WaresItem.NumberDoc,2, eTypeOrder.NoOrder);
            }
        },
                new IPostResult<List<WaresItemModel>>() {
                    @Override
                    public void Invoke(List<WaresItemModel> p) {
                        renderTable(p);
                        return;
                    }}).execute();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            String keyCode = String.valueOf(event.getKeyCode());
            switch (keyCode){
                case "66":
                    if(WaresItem.IsInputQuantity())
                       saveDocumentItem(false,true);
                    else
                        findWareByArticleOrCode(null);
                    break;
                case "62"://key SP
                    WaresItem.ClearData();
                    Refresh();
                    break;
                case "131": //F1
                    setNullToExistingPosition();
                    break;
                case "132": //F2
                    focusOnView("up");
                case "133": //F3
                    focusOnView("down");
                    break;
                case "138": //F8
                    saveDocumentItem(false,false);
                    break;

            }
        }
        return super.dispatchKeyEvent(event);
    }




    @Override
    public void onPause() {
        super.onPause();
        //Camera
        if(config.IsUseCamera())
            barcodeView.pause();
        //Zebra
        scaner.StopScan();
    }

    @Override
    public void Run(final String parBarCode) {
        if(config.IsUseCamera())
            barcodeView.pause();
        findWareByArticleOrCode(parBarCode);
    }

    public void RenderData(final WaresItemModel model){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(model == null) {
                    if(config.Company== eCompany.Sim23)
                        utils.PlaySound();
                    WaresItem.ClearData("Товар не знайдено");
                }
                else {
                    WaresItem.ClearData();

                    if(  !model.IsRecord) {
                        if (WaresItem.DocSetting.TypeControlQuantity == eTypeControlDoc.Ask) {
                            if(config.Company==eCompany.Sim23)
                                utils.PlaySound();
                            AskAddAbsentWares(model);
                            //Refresh();
                            return;
                        }
                        if(WaresItem.DocSetting.TypeControlQuantity == eTypeControlDoc.Control) {
                            if(config.Company==eCompany.Sim23)
                                utils.PlaySound();
                            UtilsUI.Dialog("Товар відсутній в документі", model.NameWares);
                            Refresh();
                            return;
                        }
                    }

                        WaresItem.Set(model);
                        WaresItem.BeforeQuantity = CountBeforeQuantity(ListWares, WaresItem.CodeWares);
                        // WaresItem.InputQuantity = WaresItem.QuantityBarCode;


                }
                /*if(config.TypeScaner==eTypeScaner.Camera)
                    barcodeView.resume();*/

                Refresh();
                if(WaresItem.QuantityBarCode>0)
                    inputCount.setText(Double.toString(WaresItem.QuantityBarCode));
                SetAlert(WaresItem.CodeWares);
                return;
            }
        });
    }

    void Refresh( ){
        binding.invalidateAll();
        if(config.IsUseCamera())
            barcodeView.resume();


    /*    EditText editText = WaresItem.IsInputQuantity() ? inputCount:barCode;
        // prevent system keyboard from appearing when EditText is tapped
        //editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setTextIsSelectable(true);

        // pass the InputConnection from the EditText to the keyboard
        InputConnection ic = editText.onCreateInputConnection(new EditorInfo());
        keyboard.setInputConnection(ic);*/


       if(WaresItem.IsInputQuantity()) {

            inputCount.setFocusable(true);
            inputCount.requestFocus();
           if(config.IsUseCamera()) {
               imm.showSoftInput(inputCount, InputMethodManager.SHOW_IMPLICIT);
           }

           // inputCount.setFocusable(true);
            //inputCount.setFocusableInTouchMode(true);
            //inputCount.requestFocusFromTouch();
           // inputCount.setFocusableInTouchMode(false);

        }
        else
        {
            barCode.setFocusable(true);
            barCode.requestFocus();
       //     if(config.IsUseCamera()) {
        //        imm.showSoftInput(barCode, InputMethodManager.SHOW_IMPLICIT);
         //   }
            //barCode.setFocusable(true);
           // barCode.setFocusableInTouchMode(true);
            //barCode.requestFocusFromTouch();
            //barCode.setFocusableInTouchMode(false);

        }


    }

    public TableRow RenderTableItem (WaresItemModel parWM ) {
        LinearLayout.LayoutParams params;
        TableRow tr = new TableRow(this);
        tr.setTag(parWM.GetCodeWares());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

        TextView Position = new TextView(this);
        Position.setPadding(padding, padding, padding, padding);
        Position.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        Position.setText(parWM.GetOrderDoc());
        Position.setTextColor(Color.parseColor("#000000"));
        //Position.setTag(parWM.GetCodeWares());
        tr.addView(Position);

        params = (LinearLayout.LayoutParams)Position.getLayoutParams();
        params.weight = 1;
        Position.setLayoutParams(params);

        TextView Title = new TextView(this);
        Title.setPadding(padding, padding, padding, padding);
        Title.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));

        Title.setText(parWM.NameWares.length()>25?parWM.NameWares.substring(0,25):parWM.NameWares);
        Title.setTextColor(Color.parseColor("#000000"));
        tr.addView(Title);

        params = (LinearLayout.LayoutParams)Title.getLayoutParams();
        params.weight = 3;
        Title.setLayoutParams(params);

        TextView Quantity = new TextView(this);
        Quantity.setPadding(padding, padding, padding, padding);
        Quantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        Quantity.setText(parWM.GetInputQuantity());
        Quantity.setTextColor(Color.parseColor("#000000"));
        tr.addView(Quantity);

        params = (LinearLayout.LayoutParams)Quantity.getLayoutParams();
        params.weight = 1;
        Quantity.setLayoutParams(params);

        TextView OldQuantity = new TextView(this);
        OldQuantity.setPadding(padding, padding, padding, padding);
        OldQuantity.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        OldQuantity.setText(parWM.GetQuantityOld());
        OldQuantity.setTextColor(Color.parseColor("#000000"));
        tr.addView(OldQuantity);

        params = (LinearLayout.LayoutParams)OldQuantity.getLayoutParams();
        params.weight = 1;
        OldQuantity.setLayoutParams(params);

       return tr;
    }

    public void renderTable(final List<WaresItemModel>  parL ) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListWares = parL;
               // WaresTableLayout.removeAllViews();
                int childCount = WaresTableLayout.getChildCount();
                // Remove all rows except the first one
                if (childCount > 1) {
                    WaresTableLayout.removeViews(1, childCount - 1);
                }

                if (ListWares.size() > 0) {
                    WaresItem.OrderDoc = ListWares.get(ListWares.size() - 1).OrderDoc;
                    for (WaresItemModel item : parL) {
                        TableRow tbl = RenderTableItem(item);
                        WaresTableLayout.addView(tbl,1);
                    }
                }
            }
        });
    }

    public Double CountBeforeQuantity(List<WaresItemModel> parL,int pCodeWares)    {
        Double res=0d;
        for (WaresItemModel item : parL)
            if(item.CodeWares==pCodeWares)
                res+=item.InputQuantity;
            return res;
    }

    private void saveDocumentItem(final Boolean isNullable,final boolean IsAdd) {
        inputCount.setText("");
        if (WaresItem.InputQuantity > 0 || DocSetting.IsAddZero || isNullable) {
            //loader.setVisibility(View.VISIBLE);
            if (WaresItem.InputQuantity > 0 | DocSetting.IsAddZero )
                WaresItem.OrderDoc++;

            //Занулюємо раніше введені кількості.
            if (isNullable)
                for (WaresItemModel el : ListWares) {
                    if (el.CodeWares == WaresItem.CodeWares)
                        if (el.InputQuantity != 0) {
                            el.InputQuantity = el.QuantityOrder;
                            el.InputQuantity = 0;
                        }
                }

            if(!IsAdd)
                if(WaresItem.BeforeQuantity>= WaresItem.InputQuantity)
                     WaresItem.InputQuantity=-WaresItem.InputQuantity;
                else {
                    //loader.setVisibility(View.INVISIBLE);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Не можна відняти більше існуючої кількості", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                //Контроль введеної кількості.
            double FullQuantity = isNullable? WaresItem.InputQuantity: WaresItem.InputQuantity+WaresItem.BeforeQuantity;
            if(WaresItem.QuantityMax<FullQuantity){
                //loader.setVisibility(View.INVISIBLE);
                UtilsUI.Dialog("Введено завелику кількість","Ви перелімітили=>"+String.format(WaresItem.CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",FullQuantity-WaresItem.QuantityMax)) ;
                return;
            }


            new AsyncHelper<Result>(new IAsyncHelper() {
                @Override
                public Result Invoke() {
                    WaresItem.CodeReason = config.Reasons[WaresItem.ListReasonIdx.get()].СodeReason;
                    return config.Worker.SaveDocWares(WaresItem.TypeDoc, WaresItem.NumberDoc, WaresItem.CodeWares, WaresItem.OrderDoc, WaresItem.InputQuantity, WaresItem.CodeReason, isNullable);
                }
            },
                    new IPostResult<Result>() {
                        @Override
                        public void Invoke(Result args) {
                            AfterSave(args);
                        }
                    }
            ).execute();
        }
    }

    public void AfterSave(final Result pResult) {
        final DocumentScannerActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isSave = pResult.State != -1;

                if (isSave) {
                    try {
                        WaresItemModel el = (WaresItemModel) WaresItem.clone();
                        ListWares.add(el);
                    } catch (Exception e) {
                        Utils.WriteLog("e",TAG, "AfterSave=>" + e.getMessage());
                    }

                    WaresTableLayout.addView(RenderTableItem(WaresItem),1);
                }

               // loader.setVisibility(View.INVISIBLE);
                WaresItem.ClearData();
                Refresh();

                if (!isSave) {
                    Intent i = new Intent(context, MessageActivity.class);
                    i.putExtra("messageHeader", "Невдалося зберегти значення!");
                    i.putExtra("message", pResult.TextError);
                    i.putExtra("type", MessageType.ErrorMessage);
                    startActivityForResult(i, 1);
                }
            }
        });
    }

    private void setNullToExistingPosition(){
        String Tag=String.valueOf(WaresItem.CodeWares);
        ArrayList<View> existPos = getViewsByTag(WaresTableLayout,Tag);
        for(View item: existPos){
            if(item instanceof ViewGroup){
                TextView tx = (TextView)((ViewGroup) item).getChildAt(2);
                TextView txOld = (TextView)((ViewGroup) item).getChildAt(3);
                if(tx.getText()!="0") {
                    txOld.setText(tx.getText());
                    tx.setText("0");
                }
            }
        }
        saveDocumentItem(true,true);
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }
        }
        return views;
    }

    private void SetAlert(int pCodeWares){
        ViewGroup rows = WaresTableLayout;
        for (int i = 0; i < rows.getChildCount(); i++) {
            TableRow trc = (TableRow) rows.getChildAt(i);
            View v = trc.getChildAt(0);
            Object tag = trc.getTag();
            if(tag != null && tag.equals(String.valueOf(pCodeWares))){
                for (int j = 0; j < trc.getChildCount(); j++) {
                    TextView vI = (TextView)trc.getChildAt(j);
                    vI.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_alert));
                    vI.setTextColor(ContextCompat.getColor(context,R.color.messageAlert));
                }
            }else{
                trc.setTag(null);
                for (int j = 0; j < trc.getChildCount(); j++) {
                    TextView vI = (TextView)trc.getChildAt(j);
                    vI.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
                    vI.setTextColor(Color.parseColor("#000000"));
                }
            }
        }
        //CheckEmptyValue();
    }


    private void  findWareByArticleOrCode(String  pBarCode){
        final String BarCode = pBarCode==null? barCode.getText().toString(): pBarCode;
        new AsyncHelper<WaresItemModel>(
                new IAsyncHelper<WaresItemModel>() {
                    @Override
                    public WaresItemModel Invoke() {
                        WaresItemModel res= config.Worker.GetWaresFromBarcode(WaresItem.TypeDoc,WaresItem.NumberDoc,BarCode);
                        if(res==null)
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(config.Company==eCompany.Sim23)
                                        utils.PlaySound();
                                    UtilsUI.Dialog("Товар не знайдено", "Даний штрихкод=> "+BarCode+" відсутній в базі");
                                    Refresh();
                                }});
                        return res;
                    }
                },
                new IPostResult<WaresItemModel>() {
                    @Override
                    public void Invoke(WaresItemModel model) {
                        //model.DocSetting.
                        RenderData(model);

                    }
                }).execute();

    }

    private final void focusOnView(final String prevent){
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                Rect scrollBounds = new Rect();
                scrollView.getDrawingRect(scrollBounds);

                switch (prevent) {
                    case "up":
                        //scrollView.scrollTo(0, scrollView.getScrollY());
                        ObjectAnimator.ofInt(scrollView, "scrollY",  (scrollView.getScrollY()-30)).setDuration(100).start();
                        break;
                    case "down":
                        //scrollView.scrollTo(0, scrollView.getScrollY());
                        ObjectAnimator.ofInt(scrollView, "scrollY",  (scrollView.getScrollY()+30)).setDuration(100).start();
                        break;
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(config.IsUseCamera()) {
            if (requestCode == PERMISSIONS_REQUEST_ACCESS_CAMERA) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    barcodeView.setVisibility(View.VISIBLE);
                    barcodeView.decodeContinuous(callback);
                }
            }
        }else{
            barcodeView.setVisibility(View.INVISIBLE);
        }
    }


    public void AskAddAbsentWares(final WaresItemModel model) {
        new AlertDialog.Builder(this)
                .setTitle("Добавити відсутній товар?")
                .setMessage(model.NameWares)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Refresh();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        model.IsRecord=true;
                        RenderData(model);
                    }
                }).create().show();
    }


}
