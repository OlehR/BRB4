package ua.uz.vopak.brb4.brb4;


import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.app.PendingIntent.getActivity;


public class LabelInfo
{
    public boolean IsLong=true;
    public int Code;
    public String Name;
    public int Price;
    public int OldPrice;
    public int PriceBill;
    public int PriceCoin;
    public String Unit;
    public String Article;
    public String BarCode;
    public int AllScan=0; //Кількість відсканованих позицій
    public int BadScan=0;//Кількість позицій, які друкувались
    public boolean Action  = false;
    public String  InfoPrinter = ""; //Стан принтера
    public String  InfoHTTP = "";//Стан HTTP
    public LabelInfo()
    {
    }
    public LabelInfo(String parData)
    {
     Init(parData);
    }

    public void Init(String parData)
    {
        String [] varData = parData.split(";");
        if(varData.length<5)
            return;
        Code = Integer.parseInt(varData[0]);
        Name = varData[1];
        String [] varPrice =varData[2].split(",");
        PriceBill = Integer.parseInt(varPrice[0]);
        PriceCoin = Integer.parseInt(varPrice[1]);
        Price=PriceBill*100+PriceCoin;
        Unit = varData[3];
        Article = varData[4];
        BarCode = varData[5];
        if(varData[6]=="1")
            Action  = true;
    }
    public byte[] LevelForPrinter(TypeLanguagePrinter parTLP) throws UnsupportedEncodingException {
        final int  LengName=25;
        byte [] res;
        String Name1,Name2="", varUnit="грн/"+this.Unit,BarCodePrice;
        String  OffsetBill="0",OffsetCoin="350";
        String Space="                                 ";
        String  OffsetEndLine=(IsLong?"260":"247");
        String varPriceBill=Integer.toString(PriceBill).trim();
        String varPriceCoin=Integer.toString(PriceCoin).trim();
        if(this.Name.length()<LengName)
            Name1=this.Name;
        else
        {
            int pos= Name.substring(0,LengName).lastIndexOf(" ");
            Name1=Name.substring(0,pos);
            Name2=Name.substring(pos);
            Name2=Space.substring(0,(LengName-Name2.length())/2) + Name2;
        }
        Name1=Space.substring(0,((LengName-Name1.length())/2)) + Name1;
        BarCodePrice = Integer.toString(Code)+"-"+Integer.toString(Price);

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date today = Calendar.getInstance().getTime();
        String CurentDate = df.format(today);

        switch(varPriceBill.length())
        {
            case 1:
                OffsetBill="120";OffsetCoin="210";
                break;
            case 2:
                OffsetBill="60"; OffsetCoin="280";
                break;
            case 3:
                OffsetBill="10"; OffsetCoin="335";
                break;
            case 4:
                OffsetBill="0";  OffsetCoin="350";
                break;
        }



        String Label="^XA\n" +
                "^LL280\n" +
                "\n" +
                "^FO 0,12^A@N,20,20,B:904_MSSS_24.arf ^FD{Name1}^FS\n" +
                "^FO 0,40^A@N,20,20,B:904_MSSS_24.arf ^FD{Name2}^FS\n" +
                "\n" +
                "^FO {OffsetBill}, 18^A@N,20,20,B:903_AB_120.arf ^FD{PriceBill}^FS\n" +
                "^FO {OffsetCoin}, 51^A@N,20,20,B:901_AB_60.arf ^FD{PriceCoin}^FS\n" +
                "^FO {OffsetCoin},140^A@N,20,20,B:904_MSSS_24.arf ^FD{Unit}^FS\n" +
                "\n" +
                "^FO  15,200^BY2 ^BCN,40,N,Y ^FD{BarCodePrice}^FS\n" +
                "\n" +
                "^FO  15,250^Ab ^FD{BarCode}^FS\n" +
                "^FO 160,250^Ab ^FD{Article}^FS\n" +
                "^FO 270,250^Ab ^FD{Date}^FS\n" +
                "\n" +
                "^FO 0,{OffsetEndLine}^A@N,20,20,B:904_MSSS_24.arf ^FD-------------------------------------^FS\n" +
                "^XZ";

        Label=Label.replace("{Name1}",Name1).replace("{Name2}",Name2).
                    replace("{OffsetBill}",OffsetBill).replace("{OffsetCoin}",OffsetCoin).replace("{Unit}",varUnit).
                    replace("{PriceBill}",varPriceBill).replace("{PriceCoin}",varPriceCoin).
                    replace("{BarCodePrice}",BarCodePrice).replace("{BarCode}",this.BarCode).
                    replace("{Article}",this.Article).replace("{Date}",CurentDate).
                    replace("{OffsetEndLine}",OffsetEndLine);
           res=Label.getBytes("Cp1251");
          return res;

    }

}
