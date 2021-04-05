package ua.uz.vopak.brb4.brb4.helpers;

import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import ua.uz.vopak.brb4.brb4.Connector.Connector;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.WaresItemModel;
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.BL;
import ua.uz.vopak.brb4.lib.helpers.BluetoothPrinter;
import ua.uz.vopak.brb4.lib.helpers.IAsyncHelper;
import ua.uz.vopak.brb4.lib.helpers.PricecheckerHelper;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.lib.models.Result;

public class BL_PriceChecker extends BL {
    protected static final String TAG = "BRB4/BL_PriceChecker";
    GlobalConfig config = GlobalConfig.instance();
    public LabelInfo LI;
    public BluetoothPrinter Printer = new BluetoothPrinter(config);
    SQLiteAdapter mDbHelper;

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

            if (LI.IsOnLine.get()) {


                try {
                    new PricecheckerHelper().getPriceCheckerData(LI, BarCode, isHandInput, config);
                    // Thread.sleep(10000);
                    SetProgress(50);
                    if (LI.resHttp != null && !LI.resHttp.isEmpty()) {
                        LI.Init(new JSONObject(LI.resHttp));
                        LI.AllScan++;
                        if (LI.OldPrice != LI.Price || LI.OldPriceOpt != LI.PriceOpt) {
                            utils.Vibrate(500);
                            if(config.Company==eCompany.Sim23)
                                utils.PlaySound();
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
            } else {
                WaresItemModel el = config.Worker.GetWaresFromBarcode(0, null, BarCode);
                if(el!=null) {
                    LI.AllScan++;
                    el.SetLI(LI);
                }

            }
        }
        try {

            int vStatus = config.Company== eCompany.Sim23 ?
                    (!LI.IsOnLine.get()?-999 : (LI.Code==0?1:(parBarCode.substring(0,2).equals("29")?(LI.OldPrice == LI.Price && LI.PriceOpt==LI.OldPriceOpt?-1:0):(isHandInput?3:2)))):
                    (isError ? -9 : (LI.OldPrice == LI.Price && LI.OldPriceOpt == LI.PriceOpt ? 1 : (this.Printer.varPrinterError != ePrinterError.None ? -1 : 0)))
            ;

            mDbHelper.InsLogPrice(parBarCode,vStatus , LI.ActionType, config.NumberPackege, LI.Code,LI.Article,config.LineNumber);
            Log.e(TAG, "vStatus  >>"+ vStatus );
            SetProgress(100);
        } catch (Exception e) {
            Log.e(TAG, "InsLogPrice  >>"+ e.getMessage() );
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


    public void printPackage(final Integer actionType, final Integer packageNumber,final boolean IsMultyLabel) {
        new AsyncHelper<Void>(new IAsyncHelper<Void>() {
            @Override
            public Void Invoke() {
                List<String> codeWares = mDbHelper.getPrintPackageCodeWares(config.TypeUsePrinter == eTypeUsePrinter.StationaryWithCutAuto?-1:actionType, packageNumber,IsMultyLabel);
                priceCheckerActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetProgress(5);//priceCheckerActivity.loader.setVisibility(View.VISIBLE);
                    }
                });
                if (config.TypeUsePrinter == eTypeUsePrinter.StationaryWithCut || config.TypeUsePrinter == eTypeUsePrinter.StationaryWithCutAuto) {
                    Connector con = Connector.instance();
                    final String res=con.printHTTP(codeWares);
                    priceCheckerActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(priceCheckerActivity, res, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.BOTTOM, 0, 0);
                            toast.show();
                        }
                    });
                }
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
        for (int i = 0; i < 20; i++) {
            SetProgress(5 + i * 5);
            List<LogPrice> list = mDbHelper.GetSendData(100);
            //List<String> ll = new ArrayList<>(list.size());
            if (list == null && list.size() == 0)
                break;
            Connector con = Connector.instance();
            Result res = con.SendLogPrice(list);

            if (res.State == 0) {
                try {
                    mDbHelper.AfterSendData();
                    int[] varRes = mDbHelper.GetCountScanCode();
                    LI.AllScan = varRes[0];
                    LI.BadScan = varRes[1];
                } catch (Exception e) {
                    Log.e(TAG, "SendLogPricePSU  >>" + e.getMessage());
                }

            }
            else
                break;
        }
        SetProgress(100);
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
