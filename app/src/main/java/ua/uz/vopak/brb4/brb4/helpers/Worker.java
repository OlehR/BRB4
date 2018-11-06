package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.widget.ProgressBar;
import com.google.gson.Gson;
import org.json.JSONObject;
import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.RevisionActivity;
import ua.uz.vopak.brb4.brb4.RevisionItemsActivity;
import ua.uz.vopak.brb4.brb4.enums.PrinterError;
import ua.uz.vopak.brb4.brb4.enums.TypeLanguagePrinter;
import ua.uz.vopak.brb4.brb4.models.GlobalConfig;
import ua.uz.vopak.brb4.brb4.models.InventoryModel;
import ua.uz.vopak.brb4.brb4.models.LabelInfo;


public class Worker
{
    GlobalConfig config = GlobalConfig.instance();
    private ProgressBar Progress;
    private String CodeWarehouse=config.getCodeWarehouse();
    public PriceCheckerActivity priceCheckerActivity;
    private String CodeWares;
    private String BarCode;
    public Activity RenderActivity;
    BluetoothPrinter Printer = new BluetoothPrinter();
    GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo LI = new LabelInfo();
    SQLiteAdapter mDbHelper;

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
       SetProgress(10);

       BarCode=parBarCode;
       if(BarCode.indexOf('-')>0)
       {
           String [] str =BarCode.split("-");
           if (str.length ==2)
           {
               CodeWares=str[0];
               LI.OldPrice=Integer.parseInt(str[1]);
           }
           else
           {
               CodeWares="";
               LI.OldPrice=0;
               BarCode="";
           }

       }
       else {
           CodeWares="";
           LI.OldPrice = 0;
       }

       if(BarCode.length()>7 || !CodeWares.isEmpty() )
       {
           String resHttp=Http.GetData(CodeWarehouse,BarCode,CodeWares);
           resHttp=resHttp.replace("&amp;","&");
           //Call Progres 50%;
           LI.InfoHTTP= Http.HttpState.name();
           SetProgress(50);
           if(resHttp!=null && !resHttp.isEmpty())
           {
               LI.Init(resHttp);
               LI.AllScan++;
               if(LI.OldPrice!=LI.Price)
               {
                   LI.BadScan++;
                   byte[] b = new byte[0];
                   try {
                       b = LI.LevelForPrinter(TypeLanguagePrinter.ZPL);
                   } catch (UnsupportedEncodingException e) {
                       //e.printStackTrace();
                   }
                   try{
                     Printer.sendData(b);
                   } catch (IOException e) {
                       //LI.InfoPrinter="Lost Connect";
                       //e.printStackTrace();
                      }
                   if(Printer.varPrinterError!=PrinterError.None)
                       LI.InfoPrinter=Printer.varPrinterError.name();
               }

           }
       }
       try {
           mDbHelper.InsLogPrice(BarCode, (LI.OldPrice == LI.Price ? 1 : 0));
           SetProgress(100);
       }
       catch (Exception e)
       {

       }
       return LI;

   }

   public void LoadDataInventory()
   {
       String data="{\"CodeData\":152,\"NumberInventory\":\"00006165\","+GlobalConfig.GetLoginJson()+"}";
       String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);
       mDbHelper.LoadDataInventory(result);
   }

    public void LoadRevisionsList(Activity context)
    {
        String data="{\"CodeData\":151,\"Warehouse\":"+config.CodeWarehouse+","+GlobalConfig.GetLoginJson()+"}";
        String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

        RevisionActivity activity = (RevisionActivity)context;

        activity.renderTable(result);
    }

   public void SendLogPrice()
   {
       List<ArrayList> list = mDbHelper.GetSendData();
       //ArrayList[] stockArr = new ArrayList[list.size()];
       //stockArr = list.toArray(stockArr);

       //Gson g= new Gson(stockArr).toJson(obj);
       String a = new Gson().toJson(list);
       String data="{\"CodeData\":141,\"Warehouse\":\""+GlobalConfig.CodeWarehouse +"\","+ GlobalConfig.GetLoginJson()   +",\"LogPrice\":"+a+"}";

       String result = new GetDataHTTP().HTTPRequest(config.ApiUrl, data);

       try {
           JSONObject jObject = new JSONObject(result);

           if(jObject.getInt("State") == 0){
               mDbHelper.AfterSendData();
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
          LI.InfoPrinter= (Printer.varPrinterError==PrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
      } catch (IOException e) {
          e.printStackTrace();
          LI.InfoPrinter="Error";
      }*/
      mDbHelper = GlobalConfig.GetSQLiteAdapter();
      int[] varRes=mDbHelper.GetCountScanCode();
      LI.AllScan=varRes[0];
      LI.BadScan=varRes[1];
  }
  public void ReInitBT(){
       if(Printer.varPrinterError == PrinterError.CanNotOpen || Printer.varPrinterError == PrinterError.TurnOffBluetooth ||  Printer.varPrinterError == PrinterError.ErrorSendData ) {
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
            LI.InfoPrinter= (Printer.varPrinterError==PrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
        } catch (IOException e) {
         //   e.printStackTrace();
            LI.InfoPrinter= PrinterError.CanNotOpen.name();
        }
    }

    public void AddConfigPair(String name, String value){
       mDbHelper.AddConfigPair(name, value);
    }

    public String GetConfigPair(String name){
        return mDbHelper.GetConfigPair(name);
    }

    public void GetInventories(String number, Activity context){
        List<InventoryModel> model = mDbHelper.GetInventories(number);

        RevisionItemsActivity activity = (RevisionItemsActivity)context;

        activity.renderTable(model);
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


}
