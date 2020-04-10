package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.DocumentWeightActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.brb4.enums.ePrinterError;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;

public class Worker {
    GlobalConfig config = GlobalConfig.instance();
    private ProgressBar Progress;
    //private String CodeWarehouse=config.getCodeWarehouse();
    public PriceCheckerActivity priceCheckerActivity;
    private String CodeWares;
    private String BarCode;
    public BluetoothPrinter Printer = new BluetoothPrinter();
    public GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo LI = new LabelInfo(config.varApplicationContext, config.isSPAR());
    SQLiteAdapter mDbHelper;

    Vibrator v = (Vibrator) config.varApplicationContext.getSystemService(Context.VIBRATOR_SERVICE);

    public Worker() {
      /*Printer.findBT();
      try {
          Printer.openBT();
          LI.InfoPrinter= (Printer.varPrinterError==ePrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
      } catch (IOException e) {
          e.printStackTrace();
          LI.InfoPrinter="Error";
      }*/
        mDbHelper = config.GetSQLiteAdapter();
        int[] varRes = mDbHelper.GetCountScanCode();
        LI.AllScan = varRes[0];
        LI.BadScan = varRes[1];
    }

    public Worker(ProgressBar parProgressBar) {
        this();
        Progress = parProgressBar;
        //Context c=scaner.getApplicationContext();

    }

    @Override
    public void finalize() {
        try {
            Printer.closeBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // mDbHelper.close();

    }

    protected void Vibrate(int time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(time);
        }
    }

    public void SetProgressBar(ProgressBar parProgressBar) {
        Progress = parProgressBar;
    }

    private void SetProgress(int parProgress) {
        if (Progress != null)
            Progress.setProgress(parProgress);
    }

    public void AddConfigPair(String name, String value) {
        mDbHelper.AddConfigPair(name, value);
    }

    public String GetConfigPair(String name) {
        return mDbHelper.GetConfigPair(name);
    }

    //Прайсчекер

    public void ReInitBT() {
        if (Printer.varPrinterError == ePrinterError.CanNotOpen || Printer.varPrinterError == ePrinterError.TurnOffBluetooth || Printer.varPrinterError == ePrinterError.ErrorSendData) {
            try {
                Printer.closeBT();
            } catch (IOException e) {
            }
        }
        InitBT();
    }

    public void InitBT() {
        Printer.findBT();
        try {
            Printer.openBT();
            LI.InfoPrinter = (Printer.varPrinterError == ePrinterError.None ? Printer.varTypePrinter.name() : Printer.varPrinterError.name());
        } catch (IOException e) {
            //   e.printStackTrace();
            LI.InfoPrinter = ePrinterError.CanNotOpen.name();
        }
    }

    public void SetPriceCheckerActivity(PriceCheckerActivity parPriceCheckerActivity) {
        priceCheckerActivity = parPriceCheckerActivity;
    }

    public LabelInfo Start(String parBarCode, boolean isHandInput) {
        //Call Progres 10%;
        //parBarCode="116897-7700-";
        boolean isError = false;
        SetProgress(10);
        BarCode = parBarCode.trim();

        if (BarCode != null && BarCode.length() > 0) {
            try {
                LI = new PricecheckerHelper().getPriceCheckerData(LI, BarCode, isHandInput, config);
                SetProgress(50);
                if (LI.resHttp != null && !LI.resHttp.isEmpty()) {
                    LI.Init(new JSONObject(LI.resHttp));
                    LI.AllScan++;
                    if (LI.OldPrice != LI.Price || LI.OldPriceOpt != LI.PriceOpt) {
                        Vibrate(500);
                        LI.BadScan++;
                        //Папір не відповідає ціннику
                        if ((LI.Action && config.printType != 1) || (!LI.Action && config.printType != 0)) {
                            isError = true;
                        } else {//Друкуємо

                            byte[] b = new byte[0];
                            try {
                                b = LI.LevelForPrinter(Printer.GetTypeLanguagePrinter());
                            } catch (UnsupportedEncodingException e) {
                                //e.printStackTrace();
                            }
                            try {
                                Printer.sendData(b);
                            } catch (IOException e) {
                                //LI.InfoPrinter="Lost Connect";
                                //e.printStackTrace();
                            }
                            if (Printer.varPrinterError != ePrinterError.None)
                                LI.InfoPrinter = Printer.varPrinterError.name();
                        }
                    } else
                        Vibrate(100);
                    if (LI.Action)
                        Vibrate(500);

                } else
                    Vibrate(200);
            } catch (Exception ex) {
                isError = true;
            }

        }
        try {
            mDbHelper.InsLogPrice(parBarCode, (isError ? -9 : (LI.OldPrice == LI.Price && LI.OldPriceOpt == LI.PriceOpt ? 1 : (this.Printer.varPrinterError != ePrinterError.None ? -1 : 0))), LI.ActionType, config.NumberPackege, LI.Code);
            SetProgress(100);
        } catch (Exception e) {

        }
        return LI;

    }

    public void printPackage(String codeWares) {
        boolean isError = false;
        if (codeWares == null)
            return;
        CodeWares = codeWares.trim();

        try {
            LI = new PricecheckerHelper().getPriceCheckerData(LI, CodeWares, false, config);
            if (LI.resHttp != null && !LI.resHttp.isEmpty()) {
                LI.Init(new JSONObject(LI.resHttp));
                if (LI.OldPrice != LI.Price || LI.OldPriceOpt != LI.PriceOpt) {
                    LI.BadScan++;
                    byte[] b = new byte[0];
                    try {
                        b = LI.LevelForPrinter(Printer.GetTypeLanguagePrinter());
                    } catch (UnsupportedEncodingException e) {
                        //e.printStackTrace();
                    }
                    try {
                        Printer.sendData(b);
                    } catch (IOException e) {
                        //LI.InfoPrinter="Lost Connect";
                        //e.printStackTrace();
                    }
                    if (Printer.varPrinterError != ePrinterError.None)
                        LI.InfoPrinter = Printer.varPrinterError.name();
                }

            }

        } catch (Exception ex) {
            isError = true;
        }

        return;

    }

    public void printHTTP(List<String> codeWares) {
        //String listString = String.join(", ", codeWares);
        StringBuilder sb = new StringBuilder();
        for (String s : codeWares) {
            sb.append(s);
            sb.append(",");
        }
//znp.vopak.local
        String json = "{\"CodeWares\":\"" + sb.toString() + "\",\"CodeWarehouse\":" + config.getCodeWarehouse() + "}";
        String res = Http.HTTPRequest("http://znp.vopak.local:8088/Print", json, "application/json;charset=UTF-8");//"http://znp.vopak.local:8088/Print"
    }

    public void printPackage(final Integer actionType, final Integer packageNumber) {
        new AsyncHelper<Void>(new IAsyncHelper<Void>() {
            @Override
            public Void Invoke() {
                List<String> codeWares = mDbHelper.getPrintPackageCodeWares(actionType, packageNumber);
                config.Worker.priceCheckerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        config.Worker.priceCheckerActivity.loader.setVisibility(View.VISIBLE);
                    }
                });
                if (config.connectionPrinterType == 3)
                    printHTTP(codeWares);
                else
                    for (String CodeWares : codeWares) {
                        printPackage(CodeWares);
                    }

                config.Worker.priceCheckerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        config.Worker.priceCheckerActivity.loader.setVisibility(View.INVISIBLE);
                    }
                });
                return null;
            }
        }).execute();
    }

    public void SendLogPrice() {
        List<ArrayList> list = mDbHelper.GetSendData();
        //ArrayList[] stockArr = new ArrayList[list.size()];
        //stockArr = list.toArray(stockArr);

        //Gson g= new Gson(stockArr).toJson(obj);
        String a = new Gson().toJson(list);
        String data = config.GetApiJson(141, "\"LogPrice\":" + a);

        String result = Http.HTTPRequest(config.ApiUrl, data);

        try {
            JSONObject jObject = new JSONObject(result);

            if (jObject.getInt("State") == 0) {
                mDbHelper.AfterSendData();
                int[] varRes = mDbHelper.GetCountScanCode();
                LI.AllScan = varRes[0];
                LI.BadScan = varRes[1];
            }

        } catch (Exception e) {

        }

    }

    public HashMap<String, String[]> getPrintBlockItemsCount(String packages) {
        return mDbHelper.getPrintBlockItemsCount(packages);
    }


    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public void LoadDocsData(String parTypeDoc, MainActivity context) {
        String data = config.GetApiJson(150, "\"TypeDoc\":" + parTypeDoc);
        String result = Http.HTTPRequest(config.ApiUrl, data);
        mDbHelper.LoadDataDoc(result);
        if (context != null)
            context.HideLoader();
    }
    //Вивантаження документів з ТЗД (HTTP)
    public String SyncDocsData(int parTypeDoc, String NumberDoc, List<WaresItemModel> Wares) {
        List<String> wares = new ArrayList<String>();
        for (WaresItemModel ware : Wares) {
            String war = "";
            war += "[" + ware.GetOrderDoc() + ",";
            war += ware.GetCodeWares() + ",";
            war += ware.GetInputQuantityZero() + "]";
            wares.add(war);
        }
        String data = config.GetApiJson(153, "\"TypeDoc\":" + parTypeDoc + ",\"NumberDoc\":\"" + NumberDoc + "\",\"Wares\":[" + TextUtils.join(",", wares) + "]");
        String result = Http.HTTPRequest(config.ApiUrl, data);
        return result;
    }
    // Отримати список документів з БД
    public void LoadListDoc(Activity context, int parTypeDoc, String parBarCode) {
        List<DocumentModel> model = mDbHelper.GetDocumentList(parTypeDoc, parBarCode);
        DocumentActivity activity = (DocumentActivity) context;
        activity.renderTable(model);
    }
    // Отримати Товари документа з БД
    public void GetDoc(int pTypeDoc, String pNumberDoc, int pTypeResult, IIncomeRender context) {
        List<WaresItemModel> model = mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, pTypeResult);
        context.renderTable(model);
    }
    // Отримати Товар по штрихкоду
    public void GetWaresFromBarcode(int pTypeDoc, String pNumberDoc, String pBarCode, Activity pContext) {
        WaresItemModel model = mDbHelper.GetScanData(pTypeDoc, pNumberDoc, pBarCode);
        DocumentScannerActivity activity = (DocumentScannerActivity) pContext;
        activity.RenderData(model);
    }
    // Збереження товару в БД
    public void SaveDocWares(int pTypeDoc, String pNumberDoc, int pCodeWares, int pOrderDoc, Double pQuantity, Boolean pIsNullable, Activity pContext) {
        if (pIsNullable)
            mDbHelper.SetNullableWares(pTypeDoc, pNumberDoc, pCodeWares);

        ArrayList args = mDbHelper.SaveDocWares(pTypeDoc, pNumberDoc, pCodeWares, pOrderDoc, pQuantity);

        if (pContext instanceof DocumentScannerActivity) {
            DocumentScannerActivity activity = (DocumentScannerActivity) pContext;
            activity.AfterSave(args);
        }
    }
    // Зміна стану документа і
    public void UpdateDocState(int pState, int pTypeDoc, String pNumberDoc, Activity pActivity) {

        mDbHelper.UpdateDocState(pState, pTypeDoc, pNumberDoc);
        List<WaresItemModel> wares = mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, 1);

        if (pState == 1) {
            String text=SyncDocsData(pTypeDoc, pNumberDoc, wares);
            if (pActivity instanceof DocumentItemsActivity)
                ((DocumentItemsActivity) pActivity).AfterSave(text);
            if (pActivity instanceof DocumentWeightActivity)
                ((DocumentWeightActivity) pActivity).AfterSave();
        }
    }


}
