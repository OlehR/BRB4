package ua.uz.vopak.brb4.brb4;

import android.os.AsyncTask;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import ua.uz.vopak.brb4.brb4.fragments.ScanFragment;



public class Worker //extends  AsyncTask<BarcodeResult , Void, LabelInfo>
{
/*
@Override
protected LabelInfo doInBackground(BarcodeResult... param)
{

  return Start(param[0]);
}
    @Override
    protected void onPostExecute(LabelInfo parLI)
    {
        scanerContext.setScanResult(parLI);
    }

*/
    ScanFragment scanerContext;
    String CodeWarehouse="000000009";

    private String CodeWares;
    private String BarCode;
    BluetoothPrinter Printer = new BluetoothPrinter();
    GetDataHTTP Http = new GetDataHTTP();
    LabelInfo LI = new LabelInfo();
   public LabelInfo Start(BarcodeResult parBarCode)
   {
       //Call Progres 10%;
       scanerContext.SetProgres(10);

       BarCode=parBarCode.getText();
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
       else
           LI.OldPrice=0;

       if(BarCode.length()>7 || !CodeWares.isEmpty() )
       {
           String resHttp=Http.GetData(CodeWarehouse,BarCode,CodeWares);

           //Call Progres 50%;
           scanerContext.SetProgres(50);
           if(resHttp!=null && !resHttp.isEmpty())
           {
               LI.Init(resHttp);



               if(LI.OldPrice!=LI.Price)
               {
                   byte[] b = new byte[0];
                   try {
                       b = LI.LevelForPrinter(TypeLanguagePrinter.ZPL);
                   } catch (UnsupportedEncodingException e) {
                       e.printStackTrace();
                   }
                   try{
                     Printer.sendData(b);
                   } catch (IOException e) {
                       e.printStackTrace();
                      }
               }

           }
       }
       scanerContext.SetProgres(100);
       return LI;

   }


    public Worker()
  {
      Printer.findBT();
      try {
          Printer.openBT();
      } catch (IOException e) {
          e.printStackTrace();
      }
  }
    public Worker(ScanFragment scaner)
    {
        scanerContext = scaner;
        Printer.findBT();
        try {
            Printer.openBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void finalize()
    {
        try {
            Printer.closeBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
