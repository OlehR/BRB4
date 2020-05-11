package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

import com.google.gson.Gson;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.DocumentWeightActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.BluetoothPrinter;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;

public class Worker {
    GlobalConfig config = GlobalConfig.instance();


    public GetDataHTTP Http = new GetDataHTTP();
    SQLiteAdapter mDbHelper  = config.GetSQLiteAdapter();



    public Worker() {
      /*Printer.findBT();
      try {
          Printer.openBT();
          LI.InfoPrinter= (Printer.varPrinterError==ePrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
      } catch (IOException e) {
          e.printStackTrace();
          LI.InfoPrinter="Error";
      }*/
     /*   mDbHelper = config.GetSQLiteAdapter();
        int[] varRes = mDbHelper.GetCountScanCode();
        LI.AllScan = varRes[0];
        LI.BadScan = varRes[1];*/
    }

    public Worker(ProgressBar parProgressBar) {
        this();
        //Progress = parProgressBar;
    }

    public void AddConfigPair(String name, String value) {
        mDbHelper.AddConfigPair(name, value);
    }

    public String GetConfigPair(String name) {
        return mDbHelper.GetConfigPair(name);
    }

    public void LoadStartData() {
        String strCompany = GetConfigPair("Company");
        if (TextUtils.isEmpty(strCompany))
            config.Company = eCompany.SevenEleven;
        else
            config.Company = eCompany.fromOrdinal(Integer.valueOf(strCompany));

        config.ApiUrl=GetConfigPair("ApiUrl");
        if(config.ApiUrl==null || config.ApiUrl.isEmpty() )
                config.ApiUrl=(config.Company==eCompany.SevenEleven? "http://176.241.128.13/RetailShop/hs/TSD/":"http://znp.vopak.local/api/api_v1_utf8.php");


        config.Login = GetConfigPair("Login");
        config.CodeWarehouse = GetConfigPair("Warehouse");

        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);

        String var = config.Worker.GetConfigPair("NumberPackege");
        String varNumberPackege = "1";
        if (var.length() > 8 && var.substring(0, 8).equals(todayAsString)) {
            varNumberPackege = var.substring(8);
        } else
            config.Worker.AddConfigPair("NumberPackege", todayAsString + varNumberPackege);

        config.NumberPackege = Integer.valueOf(varNumberPackege);
        config.IsLoadStartData=true;
    }


    //Робота з документами.
    //Завантаження документів в ТЗД (HTTP)
    public Boolean LoadDocsData(String parTypeDoc) {
        String data = config.GetApiJson(150, "\"TypeDoc\":" + parTypeDoc);
        String result = Http.HTTPRequest(config.ApiUrl, data);
        return mDbHelper.LoadDataDoc(result);
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
    public void LoadListDoc(Activity context, int parTypeDoc, String parBarCode,String pExtInfo ) {
        List<DocumentModel> model = mDbHelper.GetDocumentList(parTypeDoc, parBarCode,pExtInfo);
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
