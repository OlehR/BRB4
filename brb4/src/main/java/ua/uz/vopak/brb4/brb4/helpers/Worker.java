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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.databinding.ObservableInt;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.Connector.SE.Connector;
import ua.uz.vopak.brb4.brb4.DocumentWeightActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.brb4.models.DocSetting;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.BluetoothPrinter;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.models.Result;

public class Worker {
    protected static final String TAG = "BRB4/Worker";
    GlobalConfig config = GlobalConfig.instance();

    public GetDataHTTP Http = new GetDataHTTP();
    SQLiteAdapter mDbHelper  = config.GetSQLiteAdapter();
    public Connector c = new Connector();

    public Worker() {
    }

    public DocSetting[] GenSettingDocs(eCompany pCompany) {
        DocSetting[] Setting=null;
        switch (pCompany)
        {
            case SevenEleven:
                Setting =  new  DocSetting[2];
                Setting[0] = new DocSetting(2,"Ревізія");
                Setting[1] = new DocSetting(5,"РЦ Лоти",true,true,true,true);
                break;
            case SparPSU:
            case VopakPSU:
                Setting =  new  DocSetting[5];
                Setting[0] = new DocSetting(1,"Ревізія");
                Setting[1] = new DocSetting(2,"Прийомка");
                Setting[2] = new DocSetting(3,"Переміщення");
                Setting[3] = new DocSetting(4,"Списання");
                Setting[4] = new DocSetting(5,"Повернення");
                break;
        }
       return Setting;
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
            config.Company = eCompany.NotDefined;
        else
            config.Company = eCompany.fromOrdinal(Integer.valueOf(strCompany));

        config.DocsSetting=GenSettingDocs(config.Company);


        config.ApiUrl=GetConfigPair("ApiUrl");
        if(config.ApiUrl==null || config.ApiUrl.isEmpty() )
                config.ApiUrl=(config.Company==eCompany.SevenEleven? "http://176.241.128.13/RetailShop/hs/TSD/":"http://znp.vopak.local/api/api_v1_utf8.php");
        config.ApiURLadd=GetConfigPair("ApiUrladd");

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

        config.Reasons= mDbHelper.GetReason();
    }
    //Робота з документами.
    // Завантаження документів в ТЗД (HTTP)
    public Boolean LoadData(int pTypeDoc,String  pNumberDoc,ObservableInt pProgress,boolean pIsClearDoc) {
        if(config.Company==eCompany.SevenEleven) {
            if(pTypeDoc==-1)
                c.LoadGuidData((pTypeDoc==-1),pProgress);
            return c.LoadDocsData(pTypeDoc, pNumberDoc, pProgress, pIsClearDoc);
        }
        else
          return  LoadDocsData(pTypeDoc,pProgress);
    }

    //Завантаження документів в ТЗД (HTTP)
    //PSU Треба перенести в окремий конектор
    public Boolean LoadDocsData(int pTypeDoc, ObservableInt pProgress) {
        if(pProgress!=null)
            pProgress.set(5);

        String data = config.GetApiJson(150, "\"TypeDoc\":" + pTypeDoc);
        String result = Http.HTTPRequest(config.ApiUrl, data);
        Log.d(TAG, "Load=>"+result.length());
        if(Http.HttpState!= eStateHTTP.HTTP_OK) {
            if(pProgress!=null)
                pProgress.set(0);
            return false;
        }
        if(pProgress!=null)
            pProgress.set(45);
        return mDbHelper.LoadDataDoc(result,pProgress);
    }

    //Вивантаження документів з ТЗД (HTTP)
    public Result SyncDocsData(int parTypeDoc, String NumberDoc, List<WaresItemModel> Wares) {
        List<String> wares = new ArrayList<String>();
        for (WaresItemModel ware : Wares) {
            String war = "";
            war += "[" + ware.GetOrderDoc() + ",";
            war += ware.GetCodeWares() + ",";
            war += ware.GetInputQuantityZero() + "]";
            wares.add(war);
        }
        String data = config.GetApiJson(153, "\"TypeDoc\":" + parTypeDoc + ",\"NumberDoc\":\"" + NumberDoc + "\",\"Wares\":[" + TextUtils.join(",", wares) + "]");
        try {
            String result = Http.HTTPRequest(config.ApiUrl, data);
            Gson gson = new Gson();
            Result res= gson.fromJson(result, Result.class);
            return  res;
        }
        catch(Exception e)
        {
            return new Result(-1,e.getMessage());
        }
    }

    // Отримати список документів з БД
    public void LoadListDoc(Activity context, int parTypeDoc, String parBarCode,String pExtInfo ) {
        List<DocumentModel> model = mDbHelper.GetDocumentList(parTypeDoc, parBarCode,pExtInfo);
        DocumentActivity activity = (DocumentActivity) context;
        activity.renderTable(model);
    }
    // Отримати Товари документа з БД
    public List<WaresItemModel> GetDoc(int pTypeDoc, String pNumberDoc, int pTypeResult) {
          return mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, pTypeResult);
        //context.renderTable(model);
    }
    // Отримати Товар по штрихкоду
    public WaresItemModel GetWaresFromBarcode(int pTypeDoc, String pNumberDoc, String pBarCode, Activity pContext) {
        WaresItemModel model = mDbHelper.GetScanData(pTypeDoc, pNumberDoc, pBarCode);
        if(pContext!=null) {
            DocumentScannerActivity activity = (DocumentScannerActivity) pContext;
            activity.RenderData(model);
        }
        return model;
    }
    // Збереження товару в БД
    public Result SaveDocWares(int pTypeDoc, String pNumberDoc, int pCodeWares, int pOrderDoc, Double pQuantity, int pCodeReason , Boolean pIsNullable) {
        if (pIsNullable)
            mDbHelper.SetNullableWares(pTypeDoc, pNumberDoc, pCodeWares);

        return mDbHelper.SaveDocWares(pTypeDoc, pNumberDoc, pCodeWares, pOrderDoc, pQuantity, pCodeReason);
    }
    // Зміна стану документа і відправляємо в 1С
    public Result UpdateDocState(int pState, int pTypeDoc, String pNumberDoc) {
        mDbHelper.UpdateDocState(pState, pTypeDoc, pNumberDoc);
        List<WaresItemModel> wares = mDbHelper.GetDocWares(pTypeDoc, pNumberDoc, 2);
        if(config.Company==eCompany.SevenEleven) //TMP!!! Треба буде зробити полюдськи
                return c.SyncDocsData(pTypeDoc, pNumberDoc, wares);
            else return SyncDocsData(pTypeDoc, pNumberDoc, wares);
    }

}
