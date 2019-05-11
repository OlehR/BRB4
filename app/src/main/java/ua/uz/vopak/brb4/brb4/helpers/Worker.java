package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.widget.ProgressBar;
import com.google.gson.Gson;
import org.json.JSONObject;

import ua.uz.vopak.brb4.brb4.DocumentWeightActivity;
import ua.uz.vopak.brb4.brb4.MainActivity;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.DocumentActivity;
import ua.uz.vopak.brb4.brb4.DocumentItemsActivity;
import ua.uz.vopak.brb4.brb4.DocumentScannerActivity;
import ua.uz.vopak.brb4.brb4.enums.ePrinterError;
import ua.uz.vopak.brb4.brb4.models.DocWaresModelIncome;
import ua.uz.vopak.brb4.brb4.models.DocumentModel;
import ua.uz.vopak.brb4.brb4.models.QuantityModel;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.DocWaresModel;
import ua.uz.vopak.brb4.lib.models.LabelInfo;
import ua.uz.vopak.brb4.brb4.models.RevisionItemModel;
import ua.uz.vopak.brb4.lib.helpers.GetDataHTTP;

public class Worker
{
    GlobalConfig config = GlobalConfig.instance();
    private ProgressBar Progress;
    //private String CodeWarehouse=config.getCodeWarehouse();
    public PriceCheckerActivity priceCheckerActivity;
    private String CodeWares;
    private String BarCode;
    public BluetoothPrinter Printer = new BluetoothPrinter();
    public GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo LI = new LabelInfo(config.varApplicationContext);
    SQLiteAdapter mDbHelper;

    Vibrator v = (Vibrator) config.varApplicationContext.getSystemService(Context.VIBRATOR_SERVICE);

    public void SetProgressBar(ProgressBar parProgressBar)
    {
        Progress=parProgressBar;
    };
    public void SetPriceCheckerActivity(PriceCheckerActivity parPriceCheckerActivity)
    {
        priceCheckerActivity=parPriceCheckerActivity;
    };

   public LabelInfo Start(String parBarCode)
   {
       //Call Progres 10%;
       //parBarCode="116897-7700-";
       boolean isError=false;
       SetProgress(10);
       BarCode=parBarCode.trim();
       LI.OldPrice=0;
       LI.OldPriceOpt=0;

       if(BarCode.indexOf('-')>0)
       {
           try {
               String[] str = BarCode.split("-");
               switch (str.length) {
                   case 0:
                   case 1:
                       CodeWares = "";
                       BarCode = "";
                       break;
                   case 3:

                       LI.OldPriceOpt = Integer.parseInt(str[2]);
                   case 2:
                       CodeWares = str[0];
                       LI.OldPrice = Integer.parseInt(str[1]);
                       break;
               }
           }
           catch (Exception ex)
           {
               isError=true;
           }
       }
       else {
           CodeWares="";
          }

       if(BarCode.length()>7 || !CodeWares.isEmpty() )
       {
           try {
               String resHttp = Http.GetData(config.getCodeWarehouse(), BarCode, CodeWares);
               resHttp = resHttp.replace("&amp;", "&");
               //Call Progres 50%;
               LI.InfoHTTP = Http.HttpState.name();
               SetProgress(50);
               if (resHttp != null && !resHttp.isEmpty()) {
                   LI.Init(resHttp);
                   LI.AllScan++;
                   if (LI.OldPrice != LI.Price || LI.OldPriceOpt != LI.PriceOpt) {
                       Vibrate(500);
                       LI.BadScan++;
                       byte[] b = new byte[0];
                       try {
                           b = LI.LevelForPrinter( Printer.GetTypeLanguagePrinter());
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
                   else
                       Vibrate(100);
                   if(LI.ActionType != 0)
                       Vibrate(1000);

               }
               else
                   Vibrate(500);
           }
           catch (Exception ex)
           {
               isError=true;
           }

       }
       try {

           mDbHelper.InsLogPrice(BarCode,(isError?-9: (LI.OldPrice == LI.Price && LI.OldPriceOpt == LI.PriceOpt ? 1 : (this.Printer.varPrinterError!= ePrinterError.None ?-1:0))));
           SetProgress(100);
       }
       catch (Exception e)
       {

       }
       return LI;

   }

    public void LoadListDoc(Activity context,String parTypeDoc)
    {
        List<DocumentModel> model = mDbHelper.GetDocumentList(parTypeDoc);

        DocumentActivity activity = (DocumentActivity)context;

        activity.renderTable(model);
    }

    public void LoadDocsData(String parTypeDoc, MainActivity context)
    {
        String data=config.GetApiJson(150,"\"TypeDoc\":"+parTypeDoc);
        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

        mDbHelper.LoadDataDoc(result);

        if(context != null)
        context.HideLoader();
    }

    public void SyncDocsData(String parTypeDoc, String NumberDoc, List<DocWaresModel> Wares)
    {
        List<String> wares = new ArrayList<String>();
        for(DocWaresModel ware: Wares){
            String war = "";
            war += "[" + ware.OrderDoc+",";
            war += ware.CodeWares+",";
            war += ware.Quantity+"]";
            wares.add(war);
        }
        String data=config.GetApiJson(153,"\"TypeDoc\":"+parTypeDoc+ ",\"NumberDoc\":\""+ NumberDoc +"\",\"Wares\":["+ TextUtils.join(",",wares) +"]");
        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

    }

    public void UpdateDocState(String state, String number, String DocumentType, Activity activity){

       mDbHelper.UpdateDocState(state,number.replace("ПСЮ",""));

        List<DocWaresModel> wares = mDbHelper.GetDocWares(number,DocumentType);

       if(state.equals("1")) {
           SyncDocsData(DocumentType, number, wares);
           if(activity instanceof  DocumentItemsActivity)
               ((DocumentItemsActivity)activity).AfterSave(DocumentType);
           if(activity instanceof DocumentWeightActivity)
               ((DocumentWeightActivity)activity).AfterSave();
       }
    }

   public void SendLogPrice()
   {
       List<ArrayList> list = mDbHelper.GetSendData();
       //ArrayList[] stockArr = new ArrayList[list.size()];
       //stockArr = list.toArray(stockArr);

       //Gson g= new Gson(stockArr).toJson(obj);
       String a = new Gson().toJson(list);
       String data=config.GetApiJson(141,"\"LogPrice\":"+a);

       String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

       try {
           JSONObject jObject = new JSONObject(result);

           if(jObject.getInt("State") == 0){
               mDbHelper.AfterSendData();
               int[] varRes=mDbHelper.GetCountScanCode();
               LI.AllScan=varRes[0];
               LI.BadScan=varRes[1];
           }

       }catch (Exception e){

       }

   }

    private void SetProgress(int parProgress)
    {
        if(Progress!=null)
            Progress.setProgress(parProgress);
    }

  public Worker()
  {
      /*Printer.findBT();
      try {
          Printer.openBT();
          LI.InfoPrinter= (Printer.varPrinterError==ePrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
      } catch (IOException e) {
          e.printStackTrace();
          LI.InfoPrinter="Error";
      }*/
      mDbHelper = config.GetSQLiteAdapter();
      int[] varRes=mDbHelper.GetCountScanCode();
      LI.AllScan=varRes[0];
      LI.BadScan=varRes[1];
  }
  public void ReInitBT(){
       if(Printer.varPrinterError == ePrinterError.CanNotOpen || Printer.varPrinterError == ePrinterError.TurnOffBluetooth ||  Printer.varPrinterError == ePrinterError.ErrorSendData ) {
           try {
               Printer.closeBT();
           } catch (IOException e) {
           }
       }
           InitBT();
  }
    public void InitBT()
    {
        Printer.findBT();
        try {
            Printer.openBT();
            LI.InfoPrinter= (Printer.varPrinterError== ePrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
        } catch (IOException e) {
         //   e.printStackTrace();
            LI.InfoPrinter= ePrinterError.CanNotOpen.name();
        }
    }

    public void AddConfigPair(String name, String value){
       mDbHelper.AddConfigPair(name, value);
    }

    public String GetConfigPair(String name){
        return mDbHelper.GetConfigPair(name);
    }

    public void GetDoc(String number,String DocType, IIncomeRender context){
       if(DocType.equals("2")){
           List<DocWaresModelIncome> model = mDbHelper.GetDocWaresIncome(number);
           List<DocWaresModel> inventoryModel = mDbHelper.GetDocWares(number,DocType);

           context.RenderTableIncome(model, inventoryModel);
       }else{
           List<DocWaresModel> model = mDbHelper.GetDocWares(number,DocType);
           context.renderTable(model);
       }
    }

    public void GetRevisionScannerData(String BarCode, Activity context){
        RevisionItemModel model = mDbHelper.GetScanData(BarCode);

        DocumentScannerActivity activity = (DocumentScannerActivity) context;
        activity.RenderData(model);
    }

    public void SaveDocWares(String count, String scanNN, String CodeWares, String DocNumber,String TypeDoc , String isNullable, Activity context){
        if(isNullable.equals("true")){
            mDbHelper.SetNullableWares(CodeWares);
        }

        ArrayList args = mDbHelper.SaveDocWares(count, scanNN, CodeWares, DocNumber,TypeDoc);

        if(context instanceof DocumentScannerActivity) {
            DocumentScannerActivity activity = (DocumentScannerActivity) context;

            activity.AfterSave(args);
        }

    }

    public void GetQuantity(String typeDoc, String numberDoc, String CodeWares , Activity context){
        QuantityModel model = mDbHelper.GetQuantity(typeDoc, numberDoc, CodeWares);

        DocumentScannerActivity activity = (DocumentScannerActivity) context;

        activity.SetQuantity(model);


    }


    public Worker(ProgressBar parProgressBar )
    {
        this();
        Progress = parProgressBar;
        //Context c=scaner.getApplicationContext();

    }
    @Override
    public void finalize()
    {
        try {
            Printer.closeBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
       // mDbHelper.close();

    }

    protected void Vibrate(int time){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(time);
        }
    }
}
