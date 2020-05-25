package ua.uz.vopak.brb4.brb4.helpers;

import android.view.View;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.BL;
import ua.uz.vopak.brb4.lib.helpers.BluetoothPrinter;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class BL_PriceChecker extends BL {
    GlobalConfig config = GlobalConfig.instance();
    public LabelInfo LI;
    public BluetoothPrinter Printer = new BluetoothPrinter(config);
    SQLiteAdapter mDbHelper;
    public GetDataHTTP Http = new GetDataHTTP();
    public PriceCheckerActivity priceCheckerActivity;

    public BL_PriceChecker(LabelInfo pLI)
    {        super();
        Init(config.context);
        LI=pLI;
        mDbHelper = config.GetSQLiteAdapter();
        int[] varRes = mDbHelper.GetCountScanCode();
        LI.AllScan = varRes[0];
        LI.BadScan = varRes[1];
    }
    public void Init(LabelInfo pLI, PriceCheckerActivity pPriceCheckerActivity)
    {
        LI=pLI;
        priceCheckerActivity=pPriceCheckerActivity;
    }
    //Прайсчекер
    public void ReInitBT() {
        //Конектимся до блютуз принтера лише при потребі.
        if(config.TypeUsePrinter ==eTypeUsePrinter.AutoConnect || config.TypeUsePrinter ==eTypeUsePrinter.OnlyStart ) {
            if (Printer.varPrinterError == ePrinterError.CanNotOpen || Printer.varPrinterError == ePrinterError.TurnOffBluetooth || Printer.varPrinterError == ePrinterError.ErrorSendData) {
                try {
                    Printer.closeBT();
                } catch (IOException e) {
                }
            }
            InitBT();
        }
    }

    public void InitBT() {
        Printer.findBT();
        try {
            Printer.openBT();
            LI.TypePrinter = Printer.varTypePrinter;
        } catch (IOException e) {
            //   e.printStackTrace();
            LI.PrinterError = ePrinterError.CanNotOpen;
        }
    }

    /*
    public void SetPriceCheckerActivity(PriceCheckerActivity parPriceCheckerActivity) {
        priceCheckerActivity = parPriceCheckerActivity;
    }*/

    public LabelInfo Start(String parBarCode, boolean isHandInput) {
        //Call Progres 10%;
        //parBarCode="116897-7700-";
        config.LineNumber++;
        boolean isError = false;
        SetProgress(10);
        String BarCode = parBarCode.trim();

        if (BarCode != null && BarCode.length() > 0) {
            try {
                new PricecheckerHelper().getPriceCheckerData(LI, BarCode, isHandInput, config);
                SetProgress(50);
                if (LI.resHttp != null && !LI.resHttp.isEmpty()) {
                    LI.Init(new JSONObject(LI.resHttp));
                    LI.AllScan++;
                    if (LI.OldPrice != LI.Price || LI.OldPriceOpt != LI.PriceOpt) {
                        utils.Vibrate(500);
                        LI.BadScan++;
                        //Папір не відповідає ціннику
                        if ((LI.Action() && LI.printType != 1) || (!LI.Action() && LI.printType != 0)) {
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
                                LI.PrinterError = Printer.varPrinterError;
                        }
                    } else
                        utils.Vibrate(100);
                    if (LI.Action())
                        utils.Vibrate(500);

                } else
                    utils.Vibrate(250);
            } catch (Exception ex) {
                isError = true;
            }
        }
        try {

            int vStatus = config.Company== eCompany.SevenEleven?
                    (LI.Code==0?1:(parBarCode.substring(0,2).equals("29")?(LI.OldPrice == LI.Price && LI.PriceOpt==LI.OldPriceOpt?-1:0):(isHandInput?3:2))):
                    (isError ? -9 : (LI.OldPrice == LI.Price && LI.OldPriceOpt == LI.PriceOpt ? 1 : (this.Printer.varPrinterError != ePrinterError.None ? -1 : 0)))
            ;

            mDbHelper.InsLogPrice(parBarCode,vStatus , LI.ActionType, config.NumberPackege, LI.Code,LI.Article,config.LineNumber);
            SetProgress(100);
        } catch (Exception e) {

        }
        return LI;
    }

    public void printPackage(String codeWares) {
        boolean isError = false;
        if (codeWares == null)
            return;
        String CodeWares = codeWares.trim();

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
                        LI.PrinterError = Printer.varPrinterError;
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
                List<String> codeWares = mDbHelper.getPrintPackageCodeWares(config.TypeUsePrinter == eTypeUsePrinter.StationaryWithCutAuto?-1:actionType, packageNumber);
                priceCheckerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetProgress(5);//priceCheckerActivity.loader.setVisibility(View.VISIBLE);
                    }
                });
                if (config.TypeUsePrinter == eTypeUsePrinter.StationaryWithCut || config.TypeUsePrinter == eTypeUsePrinter.StationaryWithCutAuto)
                    printHTTP(codeWares);
                else
                    for (String CodeWares : codeWares) {
                        printPackage(CodeWares);
                    }
                priceCheckerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetProgress(100);
                        //priceCheckerActivity.loader.setVisibility(View.INVISIBLE);
                    }
                });
                return null;
            }
        }).execute();
    }

    public void SendLogPrice() {
        if(config.Company==eCompany.SparPSU||config.Company==eCompany.VopakPSU)
         SendLogPricePSU();
        else
            if(config.Company==eCompany.SevenEleven)
                SendLogPriceSevenEleve();

    }

    public void SendLogPriceSevenEleve() {
        List<LogPrice> list = mDbHelper.GetSendData();
        //List<String> ll = new ArrayList<>(list.size());

        StringBuilder sb = new StringBuilder();
        for (LogPrice s : list)
        {
            sb.append(","+s.GetJsonSE());
        }
        if(sb.length()<=2)
            return;
        String a = "["+sb.substring(1)+"]";


        String data = a;

        String res = Http.HTTPRequest(config.ApiUrl+"pricetag", data, "application/json;charset=utf-8", config.Login, config.Password);
        if (Http.HttpState == eStateHTTP.HTTP_OK) {
            try {
                mDbHelper.AfterSendData();
                int[] varRes = mDbHelper.GetCountScanCode();
                LI.AllScan = varRes[0];
                LI.BadScan = varRes[1];
            } catch (Exception e) {
            }
        }
    }
    public void SendLogPricePSU() {
        List<LogPrice> list = mDbHelper.GetSendData();
        List<String> ll= new ArrayList<>(list.size());
        for (LogPrice el : list)
            ll.add(el.GetJsonPSU());

        String a = new Gson().toJson(ll);
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

    public HashMap<String, String[]> getPrintBlockItemsCount() {
        return mDbHelper.getPrintBlockItemsCount();
    }
    public void SaveReplenishment(double pNumberOfReplenishment)
    {
        mDbHelper.UpdateReplenishment(config.LineNumber,pNumberOfReplenishment);
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

    protected void SetProgress(int parProgress) {
        if (LI != null)
            LI.Progress.set(parProgress);
    }
}
