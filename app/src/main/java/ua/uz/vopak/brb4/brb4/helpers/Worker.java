package ua.uz.vopak.brb4.brb4.helpers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import android.content.Context;

import ua.uz.vopak.brb4.brb4.PriceCheckerActivity;
import ua.uz.vopak.brb4.brb4.enums.PrinterError;
import ua.uz.vopak.brb4.brb4.enums.TypeLanguagePrinter;
import ua.uz.vopak.brb4.brb4.models.LabelInfo;


public class Worker
{
    PriceCheckerActivity scanerContext;
    String CodeWarehouse="000000009";

    private String CodeWares;
    private String BarCode;
    BluetoothPrinter Printer = new BluetoothPrinter();
    GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo LI = new LabelInfo();
    SQLiteAdapter mDbHelper;


   public LabelInfo Start(String parBarCode)
   {
       //Call Progres 10%;
       scanerContext.SetProgres(10);

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
           scanerContext.SetProgres(50);
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
                       e.printStackTrace();
                   }
                   try{
                     Printer.sendData(b);
                   } catch (IOException e) {
                       LI.InfoPrinter="Lost Connect";
                       e.printStackTrace();
                      }
                   if(Printer.varPrinterError!=PrinterError.None)
                       LI.InfoPrinter=Printer.varPrinterError.name();
               }

           }
       }
       mDbHelper.InsLogPrice(BarCode,(LI.OldPrice==LI.Price?1:0));
       scanerContext.SetProgres(100);
       return LI;

   }


    public Worker()
  {
      Printer.findBT();
      try {
          Printer.openBT();
          LI.InfoPrinter= (Printer.varPrinterError==PrinterError.None? Printer.varTypePrinter.name():Printer.varPrinterError.name());
      } catch (IOException e) {
          e.printStackTrace();
          LI.InfoPrinter="Error";
      }

  }
    public Worker(PriceCheckerActivity scaner)
    {
        this();
        scanerContext = scaner;
        Context c=scaner.getApplicationContext();
        mDbHelper = new SQLiteAdapter(c);
        mDbHelper.createDatabase();
        mDbHelper.open();
        int[] varRes=mDbHelper.GetCountScanCode();
        LI.AllScan=varRes[0];
        LI.BadScan=varRes[1];
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
