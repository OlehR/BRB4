package ua.uz.vopak.brb4.brb4;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Scaner.ScanCallBack;
import ua.uz.vopak.brb4.brb4.Scaner.Scaner;
import ua.uz.vopak.brb4.brb4.databinding.PriceCheckerLayoutNewBinding;
import ua.uz.vopak.brb4.brb4.databinding.TestLayoutBinding;
import ua.uz.vopak.brb4.brb4.helpers.BL_PriceChecker;
import ua.uz.vopak.brb4.brb4.helpers.IIncomeRender;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.MessageType;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.IPostResult;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
/*

import com.google.android.material.chip.Chip;
import com.google.common.base.Objects;


import com.google.firebase.ml.md.java.camera.GraphicOverlay;
import com.google.firebase.ml.md.java.camera.WorkflowModel;
import com.google.firebase.ml.md.java.camera.WorkflowModel.WorkflowState;
import com.google.firebase.ml.md.java.barcodedetection.BarcodeField;
import com.google.firebase.ml.md.java.barcodedetection.BarcodeProcessor;
import com.google.firebase.ml.md.java.barcodedetection.BarcodeResultFragment;
import com.google.firebase.ml.md.java.camera.CameraSource;
import com.google.firebase.ml.md.java.camera.CameraSourcePreview;
import com.google.firebase.ml.md.java.settings.SettingsActivity;
*/
public class TestActivity extends FragmentActivity implements ScanCallBack,IIncomeRender{
    Context context;
    Activity activity;
    EditText   barCode,inputCount,textBarcodeView;
    TableLayout WaresTableLayout;
    ScrollView scrollView;
    public View BarcodeImageLayout;
    public RelativeLayout loader;
    BarcodeView barcodeView;

    GlobalConfig config = GlobalConfig.instance();
    WaresItemModel WaresItem = new WaresItemModel();
    List<WaresItemModel> ListWares = new ArrayList<>();

    TestLayoutBinding binding;
    final int PERMISSIONS_REQUEST_ACCESS_CAMERA=0;

    int padding;

  //  private WorkflowModel workflowModel;
   // private WorkflowState currentWorkflowState;

// Калбек штрихкода з камери.
private BarcodeCallback callback = new BarcodeCallback() {
    @Override
    public void barcodeResult(BarcodeResult result) {
        if (result.getText() != null) {
            barcodeView.pause();
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
        activity = this;

        setContentView(R.layout.test_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding = DataBindingUtil.setContentView(this, R.layout.test_layout);
        binding.setWaresItem(WaresItem);

        loader = findViewById(R.id.TEST_Loader);
        inputCount = findViewById(R.id.TEST_InputCount);
        barCode = findViewById(R.id.TEST_BarCode);
        scrollView = findViewById(R.id.TEST_ScrollView);

        BarcodeImageLayout = findViewById(R.id.TEST_BarcodeImageLayout);
        barcodeView = findViewById(R.id.TEST_barcode_scanner);
        WaresTableLayout = findViewById(R.id.TEST_ScanItemsTable);

        if(config.TypeScaner==eTypeScaner.Camera) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST_ACCESS_CAMERA);
            } else {
                //barcodeView.setVisibility(View.VISIBLE);
                barcodeView.decodeContinuous(callback);
                barcodeView.resume();
            }
        }else{
            //barcodeView.setVisibility(View.INVISIBLE);
        }


        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(true);
        //Для отримання штрихкодів
        config.InitScaner(this);

        WaresItem.ClearData();

        Intent i = getIntent();
        WaresItem.TypeDoc = i.getIntExtra("document_type", 0);
        WaresItem.NumberDoc = i.getStringExtra("document_number");

        float d = this.getResources().getDisplayMetrics().density;
        int dpValue = 3;
        padding = (int) (dpValue * d);

        inputCount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Refresh();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    WaresItem.InputQuantity= Float.parseFloat(inputCount.getText().toString());
                }
            }
        });
/*
        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetDoc(WaresItem.TypeDoc,WaresItem.NumberDoc,2,(IIncomeRender) context);
                return null;
            }
        }).execute();
  */
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            String keyCode = String.valueOf(event.getKeyCode());
            switch (keyCode){
                case "66":
                    if(WaresItem.IsInputQuantity())
                        saveDocumentItem(false);
                    else
                        findWareByArticleOrCode();
                    break;
                case "62"://key SP
                    WaresItem.ClearData();
                    Refresh();
                    break;
                case "131":
                    setNullToExistingPosition();
                    break;
                case "132":
                    focusOnView("up");
                case "133":
                    focusOnView("down");
                    break;
            }

        }
        return super.dispatchKeyEvent(event);
    }

    @Override //Приходить штрихкод.
        public void Run(final String parBarCode) {
        if(config.TypeScaner==eTypeScaner.Camera)
            barcodeView.pause();

        new AsyncHelper<WaresItemModel>(
                new IAsyncHelper<WaresItemModel>() {
                    @Override
                    public WaresItemModel Invoke() {
                        return config.Worker.GetWaresFromBarcode(WaresItem.TypeDoc,WaresItem.NumberDoc,parBarCode);
                    }
                },
                new IPostResult<WaresItemModel>() {
                    @Override
                    public void Invoke(WaresItemModel model) {
                        RenderData(model);
                    }
                }).execute();

        }

    void Refresh( ){
        binding.invalidateAll();
        if(config.TypeScaner==eTypeScaner.Camera)
            barcodeView.resume();

        if(WaresItem.IsInputQuantity()) {
            //inputCount.setFocusableInTouchMode(true);
            //inputCount.requestFocusFromTouch();
            inputCount.requestFocus();
            // inputCount.setFocusableInTouchMode(false);
            inputCount.setFocusableInTouchMode(true);
            inputCount.requestFocusFromTouch();
            inputCount.setFocusableInTouchMode(false);
        }
        else
        {
            barCode.requestFocus();
            barCode.setFocusableInTouchMode(true);
            barCode.requestFocusFromTouch();
            barCode.setFocusableInTouchMode(false);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(config.TypeScaner==eTypeScaner.Camera)
             barcodeView.resume();
        //IntentIntegrator.forSupportFragment(this).setBeepEnabled(true);
    }
    @Override
    public void onPause() {
        super.onPause();
        if(config.TypeScaner==eTypeScaner.Camera)
            barcodeView.pause();
    }
    //We need to handle any incoming intents, so let override the onNewIntent method
    //Необхідно для Zebta  TC20 Оскільки повідомлення приходять саме так. !!!TMP Можливо перероблю через повідомлення
    @Override
    public void onNewIntent(Intent i) {
        config.GetScaner().handleDecodeData(i);
    }
    @Override
    protected void onDestroy() {
         // TODO Auto-generated method stub
        super.onDestroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(config.TypeScaner==eTypeScaner.Camera) {
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

    public TableRow RenderTableItem (WaresItemModel parWM ) {
        LinearLayout.LayoutParams params;
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

        TextView Position = new TextView(this);
        Position.setPadding(padding, padding, padding, padding);
        Position.setBackground(ContextCompat.getDrawable(this, R.drawable.table_cell_border));
        Position.setText(parWM.GetOrderDoc());
        Position.setTextColor(Color.parseColor("#000000"));
        Position.setTag(parWM.GetCodeWares());
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
                if (ListWares.size() > 0) {
                    WaresItem.OrderDoc = ListWares.get(ListWares.size() - 1).OrderDoc;
                    for (WaresItemModel item : parL) {
                        TableRow tbl = RenderTableItem(item);
                        WaresTableLayout.addView(tbl);
                    }
                }
            }
        });
    }

    public void RenderData(final WaresItemModel model){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(model == null)
                    WaresItem.ClearData("Товар не знайдено");
                else {
                    WaresItem.ClearData();
                    WaresItem.Set(model);
                    WaresItem.BeforeQuantity = CountBeforeQuantity(ListWares, WaresItem.CodeWares);
                }

                Refresh();
                SetAlert(WaresItem.CodeWares);
                return;
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

    private void saveDocumentItem(final Boolean isNullable) {
        if (WaresItem.InputQuantity>0) {

            loader.setVisibility(View.VISIBLE);
            WaresItem.OrderDoc++;
            new AsyncHelper<Void>(new IAsyncHelper() {
                @Override
                public Void Invoke() {
                    //config.Worker.SaveDocWares( WaresItem.TypeDoc ,WaresItem.NumberDoc, WaresItem.CodeWares, WaresItem.OrderDoc, WaresItem.InputQuantity, isNullable, activity);
                    return null;
                }
            }).execute();
        }
/*
        scrollView.postDelayed(new Runnable() {
            @Override
            public void run() {
                barCode.setFocusable(true);
                barCode.setFocusable(false);
                barCode.setFocusableInTouchMode(true);
                barCode.requestFocusFromTouch();
                barCode.setFocusableInTouchMode(false);
            }
        }, 100L);*/
    }

    public void AfterSave(final ArrayList args){
        final TestActivity context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isSave = (boolean) args.get(0);
                String message = (String) args.get(1);

                if(isSave) {
                    ListWares.add(WaresItem);
                    WaresTableLayout.addView(RenderTableItem(WaresItem));
                }

                loader.setVisibility(View.INVISIBLE);
                WaresItem.ClearData();
                Refresh();

                if (!isSave) {
                    Intent i = new Intent(context, MessageActivity.class);
                    i.putExtra("messageHeader", "Невдалося зберегти значення!");
                    i.putExtra("message", message);
                    i.putExtra("type", MessageType.ErrorMessage);
                    startActivityForResult(i, 1);
                }
            }
        });
    }

    private void setNullToExistingPosition(){
        ArrayList<View> existPos = getViewsByTag(WaresTableLayout,"nullable");
        for(View item: existPos){
            if(item instanceof ViewGroup){
                View tx = ((ViewGroup) item).getChildAt(2);
                ((TextView)tx).setText("0");
            }
        }
        saveDocumentItem(true);
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
        if(rows==null)
            return;
        for (int i = 0; i < rows.getChildCount(); i++) {
            TableRow trc = (TableRow) rows.getChildAt(i);
            View v = trc.getChildAt(0);
            String tag = (String) v.getTag();
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

    private void  findWareByArticleOrCode(){
        //new AsyncRevisionScanHelper(worker, aContext).execute(barCode.getText().toString());

        new AsyncHelper<Void>(new IAsyncHelper() {
            @Override
            public Void Invoke() {
                config.Worker.GetWaresFromBarcode(WaresItem.TypeDoc,WaresItem.NumberDoc ,barCode.getText().toString());
                return null;
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


}
