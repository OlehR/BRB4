package ua.uz.vopak.brb4.lib.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//import javax.naming.Context;

import ua.uz.vopak.brb4.lib.enums.TypeLanguagePrinter;

public class LabelInfo
{
    boolean isSpar=false;
    boolean isInit=false;
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
    public int ActionType  = 0; //0 - без акції, 1 - жовтий цінник
    public String InfoPrinter = ""; //Стан принтера
    public String InfoHTTP = "";//Стан HTTP
    public String resHttp;
    public double Rest;
    private Context varApplicationContext;
    byte [] DecodeChar;
    String LogoPicture;

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
    public LabelInfo(Context parApplicationContext,boolean parIsSpar)
    {
        varApplicationContext=parApplicationContext;
        isSpar=parIsSpar;
        LogoPicture= GetStringFromAssetsFile("Label/" + (isSpar?"spar":"vopak") + ".prn");
    }

    public void SetTypeShop(boolean parIsSpar)
    {
        isSpar=parIsSpar;
        LogoPicture= GetStringFromAssetsFile("Label/" + (isSpar?"spar":"vopak") + ".prn");
    }


    public LabelInfo(Context parApplicationContext,String parData)
    {
        varApplicationContext=parApplicationContext;
        Init(parData);
    }

    public void Init(JSONObject parData)
    {


        if(parData.length() == 0)
            return;

        try {
            Code = parData.getInt("Code");
            Name = parData.getString("Name");
            if (parData.getString("Price").length() > 1) {
                String[] varPrice = parData.getString("Price").split("\\.");
                PriceBill = Integer.parseInt(varPrice[0]);
                if(varPrice[1].length() == 1)
                    varPrice[1] = varPrice[1] + "0";
                PriceCoin = Integer.parseInt(varPrice[1]);
            } else {
                PriceBill = 0;
                PriceCoin = 0;
            }
            Price = PriceBill * 100 + PriceCoin;

            Unit = parData.getString("Unit");
            Article =  parData.getString("Article");
            BarCode = parData.getString("BarCodes");
            ActionType = parData.getInt("ActionType");
            if (ActionType == 1 || ActionType == 2)
                Action = true;
            else
                Action = false;

            PriceBillOpt = 0;
            PriceCoinOpt = 0;
            QuantityOpt = 0;
            Rest = 0;

            if (parData.has("QuantityOpt") && parData.has("PriceOpt")) {
                    QuantityOpt = parData.getDouble("QuantityOpt");
                if (QuantityOpt != 0) {
                    String[] varPrice = parData.getString("PriceOpt").split(",");
                    PriceBillOpt = Integer.parseInt(varPrice[0]);
                    PriceCoinOpt = Integer.parseInt(varPrice[1]);
                }

            }
            if (parData.has("Rest")) {

                Rest = parData.getDouble("Rest");

            }

            PriceOpt = PriceBillOpt * 100 + PriceCoinOpt;
            if (Price > 0 && PriceOpt == Price) {
                PriceOpt = 0;
                PriceBillOpt = 0;
                PriceCoinOpt = 0;
            }

        }catch (Exception e){
            e.getMessage();
        }


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
        ActionType=0;

        try {
            ActionType = Integer.parseInt(varData[6]);
        }catch(Exception Ex){}

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
    public String ToHexZebra( byte[] varByte)
    {
        StringBuilder sb = new StringBuilder(varByte.length * 3);
        for(byte b: varByte)
            sb.append("_"+String.format("%02x", b));
        return sb.toString();

    /*    String Res="";
        for(int i =0 ;i<varByte.length; i++) {
            Res+="_"+ Integer.toString(varByte[i],16);
        }
        return Res;*/
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
        //PriceBill=7;

        final int  LengName=(PriceOpt==0?26:32);
        byte [] res;
        String Name1,Name2="  ";
        String varUnit="грн/"+this.Unit,BarCodePrice;
        String UnitOpt="від "+ (Math.round(QuantityOpt)==(long) QuantityOpt? Long.toString((long)  QuantityOpt) : Double.toString(this.QuantityOpt)) +" " +this.Unit;
        String  OffsetBill="0",OffsetCoin="350";

        String  OffsetBill2="0",OffsetCoin2="350";
        String Space="                                 ";
        String OffsetEndLine=(IsShort?"260":"247");
        String LabelLength = (IsShort?"295":"280");

        String varPriceBill=Integer.toString(PriceBill).trim();
        //varPriceBill="8";

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
        if(Name2.length()>LengName+3)
          Name2=Name2.substring(0,LengName+3);
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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

        if(parTLP==TypeLanguagePrinter.ZPL_ARGOX )
        {
            switch (varPriceBill.length()) {
                case 1:
                    OffsetBill = "110";
                    OffsetCoin = "180";
                    break;
                case 2:
                    OffsetBill = "70";
                    OffsetCoin = "200";
                    break;
                case 3:
                    OffsetBill = "20";
                    OffsetCoin = "200";
                    break;
                case 4:
                    OffsetBill = "0";
                    OffsetCoin = "250";
                    break;
            }

        }



        if(parTLP==TypeLanguagePrinter.ZPL_SEWOO || parTLP==TypeLanguagePrinter.CPCL_SEWOO )
        {
            switch (varPriceBill.length()) {
                case 1:
                    OffsetBill = "100";
                    varWidthBill = "225";
                    break;
                case 2:
                    OffsetBill = "0";
                    varWidthBill = "225";
                    break;
                case 3:
                    OffsetBill = "0";
                    varWidthBill = "150";
                    break;
                case 4:
                    OffsetBill = "0";
                    varWidthBill = "110";
                    break;
            }

        }

        if(parTLP==TypeLanguagePrinter.EZPL )
        {
            switch (varPriceBill.length()) {
                case 1:
                    OffsetBill = "70";
                    OffsetCoin = "180";
                    varWidthBill ="155";
                    break;
                case 2:
                    OffsetBill = "0";
                    OffsetCoin = "180";
                    varWidthBill ="135";
                    break;
                case 3:
                    OffsetBill = "0";
                    OffsetCoin = "180";
                    varWidthBill ="90";

                    break;
                case 4:
                    OffsetBill = "0";
                    OffsetCoin = "190";
                    varWidthBill ="72";

            }

        }

        if(parTLP==TypeLanguagePrinter.ZPL_ZEBRA||parTLP==TypeLanguagePrinter.ZPL_SEWOO )
        {
            OffsetCoin = "220";
            OffsetBill = (varPriceBill.length()>1?"10":"100");
            Name1=ToHexZebra(Name1.getBytes("UTF-8"));
            Name2=ToHexZebra(Name2.getBytes("UTF-8"));
            varUnit=ToHexZebra(varUnit.getBytes("UTF-8"));

        }

        String Label="";
        String varName_file = parTLP.toString().toLowerCase();
        if(parTLP==TypeLanguagePrinter.CPCL_SEWOO && !isInit) {
            isInit=true;
            Label=GetStringFromAssetsFile("Label/" + varName_file +"_head"+ ".prn");
        }


        Label=Label+GetStringFromAssetsFile("Label/" + varName_file /*"zpl_"*/ +"_"+ (PriceOpt==0?"1":"2"    ) + ".prn");

        //Name1="АБВГД ЮЯ";
        //Name2=Name1.toLowerCase();

        Label=Label.replace("{Name1}",Name1).replace("{Name2}",Name2).
                    replace("{OffsetBill}",OffsetBill).replace("{OffsetCoin}",OffsetCoin).replace("{Unit}",varUnit).
                    replace("{PriceBill}",varPriceBill).replace("{PriceCoin}",strPriceCoin()).
                    replace("{WidthBill}",varWidthBill).
                    replace("{PriceBill2}",varPriceBill2).replace("{PriceCoin2}",strPriceCoinOpt()).
                    replace("{BarCodePrice}",BarCodePrice).replace("{BarCode}",this.BarCode.substring(0,this.BarCode.length()>=13?13:this.BarCode.length())).
                    replace("{Article}",this.Article).replace("{Date}",CurrentDate).
                    replace("{OffsetBill2}",OffsetBill2).replace("{OffsetCoin2}",OffsetCoin2).
                    replace("{OffsetEndLine}",OffsetEndLine).
                    replace("{LabelLength}",LabelLength).replace("{LabelLength_1}",Integer.toString(Integer.parseInt(LabelLength)-1)).
                    replace("{UnitOpt}",UnitOpt).
                    replace("{OffsetUnit}",Integer.toString(Integer.parseInt(OffsetCoin)+80)).
                    replace("{Logo}",LogoPicture);//isSpar?"SPAR":"VOPAK"
        ;
        //byte[] ptext = String.getBytes("UTF-8")
        if(parTLP==TypeLanguagePrinter.EZPL)
            res=Label.getBytes();//("UTF-8");
          else
            res=Label.getBytes("Cp1251");

        //Магія для кодових сторінок SEWOO в режимі CPCL
        if(parTLP==TypeLanguagePrinter.CPCL_SEWOO)
        {
            if(DecodeChar == null) {
                try {
                    AssetManager aa = varApplicationContext.getAssets();
                    InputStream inputStream = varApplicationContext.getAssets().open("Label/" + "to_sewoo_lk.map");

                    //BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                    DecodeChar = new byte[128];
                    inputStream.read(DecodeChar);
                } catch (Exception ex) {
                    String er = ex.getMessage();

                }
            }
             if(DecodeChar!=null)
                    for(int i=0;i<res.length;i++)
                    {

                        if(res[i]<0&&DecodeChar[128+res[i]]!=0) {
/*                            byte s = res[i];
                            byte ch = DecodeChar[128 + res[i]];
                            ch = DecodeChar[-res[i]];*/
                            res[i] = DecodeChar[128 + res[i]];
                        }
                    }



        }
        String path=Environment.getExternalStorageDirectory()+"/Download/label.prn";
        try (FileOutputStream stream = new FileOutputStream(path)) {
            stream.write(res);
        }
        catch (Exception ex)
        {
            String r=ex.getMessage();
        }


        return res;

    }
    String GetStringFromAssetsFile(String parPath)
    {
        String Label="";
        try {
            InputStream inputStream = varApplicationContext.getAssets().open(parPath);
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
     return Label;
    }

}
