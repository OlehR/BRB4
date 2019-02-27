package ua.uz.vopak.brb4.lib.models;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import javax.naming.Context;

import ua.uz.vopak.brb4.lib.enums.TypeLanguagePrinter;


public class LabelInfo
{
    public boolean IsShort=false;
    public int Code;
    public String Name;
    public int Price;
    public int OldPrice;

    public int PriceOpt;
    public int OldPriceOpt;
    public double QuantityOpt=1;
    public int PriceBill;
    public int PriceCoin;
    public int PriceBillOpt;
    public int PriceCoinOpt;

    public String Unit;
    public String Article;
    public String BarCode;
    public int AllScan=0; //Кількість відсканованих позицій
    public int BadScan=0;//Кількість позицій, які друкувались
    public boolean Action  = false;
    public String  InfoPrinter = ""; //Стан принтера
    public String  InfoHTTP = "";//Стан HTTP
    public double Rest;
    private Context varApplicationContext;

    public String strPriceCoin() {
        return (PriceCoin < 10 ? "0" : "") + Integer.toString(PriceCoin).trim();
    }

    public String strPriceCoinOpt()
    {
        return (PriceCoinOpt<10?"0":"")+Integer.toString(PriceCoinOpt).trim();
    }

    public LabelInfo(Context parApplicationContext)
    {
       varApplicationContext=parApplicationContext;
    }
    public LabelInfo(Context parApplicationContext,String parData)
    {
        varApplicationContext=parApplicationContext;
        Init(parData);
    }

    public void Init(String parData)
    {


        String [] varData = parData.split(";");
        if(varData.length<5)
            return;
        Code = Integer.parseInt(varData[0]);
        Name = varData[1];
        if(varData[2].length()>1) {
            String[] varPrice = varData[2].split(",");
            PriceBill = Integer.parseInt(varPrice[0]);
            PriceCoin = Integer.parseInt(varPrice[1]);
        }
        else
        {
            PriceBill=0;
            PriceCoin =0;
        }
        Price=PriceBill*100+PriceCoin;

        Unit = varData[3];
        Article = varData[4];
        BarCode = varData[5];
        if(varData[6]=="1")
            Action  = true;

        PriceBillOpt=0;
        PriceCoinOpt=0;
        QuantityOpt=0;
        Rest=0;

        if(varData.length>=10 && varData[8].length()>1) {
            String[] varPrice = varData[8].split(",");
            try {
                QuantityOpt = Double.parseDouble(varData[9]);
            }catch(Exception Ex){}
            if( QuantityOpt!=0) {
                PriceBillOpt = Integer.parseInt(varPrice[0]);
                PriceCoinOpt = Integer.parseInt(varPrice[1]);
            }

        }
        if(varData.length>=11)
        {
            try {
                Rest = Double.parseDouble(varData[10]);
            }catch(Exception Ex){}
        }

        PriceOpt=PriceBillOpt*100+PriceCoinOpt;
        if(Price>0 && PriceOpt==Price)
        {
            PriceOpt=0;
            PriceBillOpt=0;
            PriceCoinOpt=0;
        }


    }
    public String ToHexZebra(String parStr)
    {
        byte[] varByte = parStr.getBytes();
        String Res="";
        for(int i =0 ;i<varByte.length; i++) {
            Res+="_"+Integer.toString(varByte[i],16);
        }
        return Res;
    }

    public byte[] LevelForPrinter(TypeLanguagePrinter parTLP) throws UnsupportedEncodingException {

        /*//Test Begin
        PriceOpt=243199;
        PriceBillOpt=2431;
        PriceCoinOpt=99;
        Price=242239;
        PriceBill=2422;
        PriceCoin=39;
        //Test End*/

        final int  LengName=(PriceOpt==0?25:32);
        byte [] res;
        String Name1,Name2="";
        String varUnit="грн/"+this.Unit,BarCodePrice;
        String UnitOpt="від "+ (Math.round(QuantityOpt)==(long) QuantityOpt? Long.toString((long)  QuantityOpt) : Double.toString(this.QuantityOpt)) +" " +this.Unit;
        String  OffsetBill="0",OffsetCoin="350";

        String  OffsetBill2="0",OffsetCoin2="350";
        String Space="                                 ";
        String OffsetEndLine=(IsShort?"260":"247");
        String LabelLength = (IsShort?"295":"280");

        String varPriceBill=Integer.toString(PriceBill).trim();
        //String varPriceCoin=(PriceCoin<10?"0":"")+Integer.toString(PriceCoin).trim();

        String varPriceBill2=Integer.toString(PriceBillOpt).trim();
        //String varPriceCoin2=(PriceCoinOpt<10?"0":"")+Integer.toString(PriceCoinOpt).trim();
        String varWidthBill ="150";

        if(this.Name.length()<LengName)
            Name1=this.Name;
        else
        {
            int pos= Name.substring(0,LengName).lastIndexOf(" ");
            Name1=Name.substring(0,pos);
            Name2=Name.substring(pos);
            if(Name2.length()<LengName)
               Name2=Space.substring(0,(LengName-Name2.length())/2) + Name2;
        }
        Name1=Space.substring(0,((LengName-Name1.length())/2)) + Name1;
        BarCodePrice = Integer.toString(Code)+"-"+Integer.toString(Price)+(PriceOpt==0?"":"-"+Integer.toString(PriceOpt));

        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date today = Calendar.getInstance().getTime();
        String CurrentDate = df.format(today);
        if(PriceOpt==0) {
            switch (varPriceBill.length()) {
                case 1:
                    OffsetBill = "110";
                    OffsetCoin = "225";
                    break;
                case 2:
                    OffsetBill = "60";
                    OffsetCoin = "280";
                    break;
                case 3:
                    OffsetBill = "10";
                    OffsetCoin = "335";
                    varWidthBill = "100";
                    break;
                case 4:
                    OffsetBill = "0";
                    OffsetCoin = "350";
                    varWidthBill = "75";
                    break;
            }
        }else
        {
            switch (varPriceBill.length()) {
                case 1:
                    OffsetBill = "50";
                    OffsetCoin = "110";
                    OffsetBill2 = "160";
                    OffsetCoin2 = "240";

                    break;
                case 2:
                    OffsetBill = "10";
                    OffsetCoin = "120";
                    OffsetBill2 = "150";
                    OffsetCoin2 = "315";

                    break;
                case 3:
                    OffsetBill = "10";
                    OffsetCoin = "170";
                    OffsetBill2 = "50";
                    OffsetCoin2 = "300";

                    break;
                case 4:
                    OffsetBill = "50";
                    OffsetCoin = "260";
                    OffsetBill2 = "0";
                    OffsetCoin2 = "320";

                    break;
            }
        }

        if(parTLP==TypeLanguagePrinter.ZPL_ZEBRA)
        {
            OffsetCoin = "220";
            OffsetBill = (varPriceBill.length()>1?"10":"100");
            Name1=ToHexZebra(Name1);
            Name2=ToHexZebra(Name2);
            varUnit=ToHexZebra(varUnit);

        }

        String Label="";
        try {
            String varName_file = parTLP.toString().toLowerCase();
            InputStream inputStream = varApplicationContext.getAssets().open("Label/" + varName_file /*"zpl_"*/ +"_"+ (PriceOpt==0?"1":"2"    ) + ".prn");

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            for (String line; (line = r.readLine()) != null; ) {
                total.append(line).append('\n');
            }
            Label=total.toString();
        }
        catch (Exception ex)
        {

        }


        Label=Label.replace("{Name1}",Name1).replace("{Name2}",Name2).
                    replace("{OffsetBill}",OffsetBill).replace("{OffsetCoin}",OffsetCoin).replace("{Unit}",varUnit).
                    replace("{PriceBill}",varPriceBill).replace("{PriceCoin}",strPriceCoin()).
                    replace("{WidthBill}",varWidthBill).
                    replace("{PriceBill2}",varPriceBill2).replace("{PriceCoin2}",strPriceCoinOpt()).
                    replace("{BarCodePrice}",BarCodePrice).replace("{BarCode}",this.BarCode).
                    replace("{Article}",this.Article).replace("{Date}",CurrentDate).
                    replace("{OffsetBill2}",OffsetBill2).replace("{OffsetCoin2}",OffsetCoin2).
                    replace("{OffsetEndLine}",OffsetEndLine).
                    replace("{LabelLength}",LabelLength).replace("{LabelLength_1}",Integer.toString(Integer.parseInt(LabelLength)-1)).
                    replace("{UnitOpt}",UnitOpt).
                    replace("{OffsetUnit}",Integer.toString(Integer.parseInt(OffsetCoin)+80));
        ;
        //byte[] ptext = String.getBytes("UTF-8")
           res=Label.getBytes("Cp1251");
          return res;

    }

}
