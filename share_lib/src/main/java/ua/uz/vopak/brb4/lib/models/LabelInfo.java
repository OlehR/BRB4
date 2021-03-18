package ua.uz.vopak.brb4.lib.models;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
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
import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.ePrinterError;
import ua.uz.vopak.brb4.lib.enums.eStateHTTP;
import ua.uz.vopak.brb4.lib.enums.eTypePrinter;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;
import ua.uz.vopak.brb4.lib.helpers.Utils;

import androidx.core.content.ContextCompat;
import androidx.databinding.*;

public class LabelInfo
{

    boolean isInit=false;
    public AbstractConfig config;

    public int Code;
    public String strCode(){return config.Company==eCompany.SevenEleven?Integer.toString(Code):Article.replaceFirst("^0+(?!$)", ""); }
    public String strCodeArticle(){ return config.Company==eCompany.SevenEleven? "Код:":"Арт.:";}
    public String Name="";

    //зберігаємо в копійках із за відсутності Decimal
    public int Price;
    public String strPrice() {return  String.format("%.2f", (double) Price/100d);}
    public int OldPrice;
    public String strOldPrice() {return  String.format("%.2f", (double) OldPrice/100d);}
    public int ColorPrice() {return Color.parseColor(OldPrice != Price || OldPriceOpt!=PriceOpt? "#ee4343" : "#3bb46e");}
    public int PriceBill;
    public int PriceCoin;
    public String strPriceCoin() {return (PriceCoin < 10 ? "0" : "") + Integer.toString(PriceCoin).trim();}

    public double QuantityOpt=1;
    public int PriceOpt;
    public String strPriceOpt() {return  String.format("%.2f", (double) PriceOpt /100d);}
    public int OldPriceOpt;
    public String strOldPriceOpt() {return  String.format("%.2f", (double) OldPriceOpt/100d);}
    public int ColorPriceOpt(){return Color.parseColor(OldPriceOpt != PriceOpt ? "#ee4343" : "#3bb46e");}
    public int PriceBillOpt;
    public int PriceCoinOpt;
    public String strPriceCoinOpt() {return (PriceCoinOpt<10?"0":"")+Integer.toString(PriceCoinOpt).trim(); }
    public Boolean VisibleOpt() {return OldPriceOpt != 0 || PriceOpt != 0;}

    public String Unit="";
    public String Article="";
    public ObservableField<String> BarCode= new ObservableField<String>("");
    public double Rest=0;
    public double Sum=0;
    public String  strRest(){return String.format("%.2f", Rest);}

    public int AllScan=0; //Кількість відсканованих позицій
    public int BadScan=0;//Кількість позицій, які друкувались
    public String InfoScan(){ return Integer.toString(BadScan) + "/" + Integer.toString(AllScan) +
                (AllScan == 0 ? "" : " ("+Integer.toString(100 * (AllScan - BadScan) / AllScan)+"%)");}


    public int ActionType  = 0; //0 - без акції, 1 - жовтий цінник
    public boolean Action()  { return ActionType>0;}

    public eTypePrinter TypePrinter = eTypePrinter.NotDefined;
    public ePrinterError PrinterError=ePrinterError.None;
    public String InfoPrinter() {return PrinterError==ePrinterError.None? TypePrinter.name() :PrinterError.name();} ; //Стан принтера
    public int ColorPrinter(){return Color.parseColor(HttpState != eStateHTTP.HTTP_OK ? "#ee4343" : "#856404");}

    public eStateHTTP HttpState= eStateHTTP.HTTP_OK;
    public String InfoHTTP() {return HttpState.name();};//Стан HTTP
    public int ColorHTTP(){return Color.parseColor(HttpState != eStateHTTP.HTTP_OK ? "#ee4343" : "#856404");}
    public String resHttp="";

    public int printType = 0;//Колір чека 0-звичайнийб 1-жовтий, -1 не розділяти.
    public void SetPrintType(){if(printType==0) printType=1; else if(printType==1) printType=0;}
    public int ColorPrintColorType(){return Color.parseColor( HttpState != eStateHTTP.HTTP_OK ? "#ffb3b3":( printType==0 ? "#ffffff" : "#3fffff00"));}
    public String NamePrintColorType(){if(printType==0) return "Звичайний";if(printType==1)return "Жовтий";  return"";}

    public boolean IsShort=false;
    public String NameTypeLabel(){ return IsShort?"Коротка":"Стандартна";}


    public boolean IsUseCamera(){return config.TypeScaner== eTypeScaner.Camera;}
    public boolean IsLookPackege() {return config.Company!=eCompany.SevenEleven;}

    public boolean IsEnableYellowButtom() {return !(config.TypeUsePrinter== eTypeUsePrinter.NotDefined || config.TypeUsePrinter== eTypeUsePrinter.StationaryWithCutAuto );}

    public String strNumberPackege() {return Integer.toString( config.NumberPackege);}

    public ObservableArrayList<String> ListPackege = new ObservableArrayList<>();
    public ObservableInt  ListPackegeIdx = new ObservableInt(0);
    public void SetListPackege(){};
    public boolean IsSoftKeyboard(){return config.TypeScaner==eTypeScaner.Camera;}
    public ObservableField<String> NumberOfReplenishment= new ObservableField<>("");
    public boolean IsViewReplenishment(){ return config.Company==eCompany.SevenEleven;}

    public ObservableInt InputFocus = new  ObservableInt(1);//1- штрихкод,2-Поповнення

    public int GetColorBackground(){return Color.parseColor(HttpState != eStateHTTP.HTTP_OK  || PrinterError!=ePrinterError.None ?  "#ffb3b3" : "#b3ffb3");}
    public ObservableInt  Progress = new ObservableInt(0);

    public ObservableBoolean IsOnLine = new ObservableBoolean((true));
    public ObservableField<String> OnLineText =new ObservableField<String>("OffLine");



    public void ChangeOnLineState()
    {
        IsOnLine.set(!IsOnLine.get());
        OnLineText.set(IsOnLine.get()?"OffLine":"OnLine");
    }

    public boolean IsEnableMultyLabel() {return  config.Company!=eCompany.SevenEleven;}
    public boolean IsMultyLabel=false;
    public ObservableField<String> MultyLabelText =new ObservableField<String>("Унікальні");
    public void ChangeMultyLabel()
    {
        IsMultyLabel=!IsMultyLabel;
        MultyLabelText.set(IsMultyLabel?"Дублювати":"Унікальні");
    }
    //public boolean isEdit=false;



    String LogoPicture=null;
    protected Utils utils=Utils.instance();

    public void Clear() {
        Code = 0;
        Price = 0;
        OldPrice = 0;
        PriceBill = 0;
        PriceCoin = 0;
        PriceOpt = 0;
        OldPriceOpt = 0;
        PriceBillOpt = 0;
        PriceCoinOpt = 0;
        QuantityOpt = 1;
        Name = "";
        Unit = "";
        Article = "";
        BarCode.set("");
        Rest = 0;
        Sum=0;
        InputFocus.set(0);
        NumberOfReplenishment.set("");
        //isEdit=false;
    }

    public LabelInfo(AbstractConfig pConfig)  {
        config=pConfig;
    }

    public String GetLogoPicture(){
        if(LogoPicture==null) {
            String vNameLogo = "";
            switch (config.Company) {
                case SparPSU:
                    vNameLogo = "spar";
                    break;
                case VopakPSU:
                    vNameLogo = "vopak";
                    break;
                case SevenEleven:
                    vNameLogo = "seveneleven";
                    break;
            }
            LogoPicture = utils.GetStringFromAssetsFile("Label/" + vNameLogo + ".prn");
        }
        return LogoPicture;
    }

    public void Init(JSONObject parData) {
        if(parData.length() == 0)
            return;
        PriceBill = 0;
        PriceCoin = 0;
        try {
            Code = parData.getInt("Code");
            Name = parData.getString("Name");
            if (parData.getString("Price").length() > 0) {
                String[] varPrice = parData.getString("Price").replace(',','.').split("\\.");
                PriceBill = Integer.parseInt(varPrice[0]);
                if(varPrice.length ==2) {
                    if (varPrice[1].length() == 1)
                        varPrice[1] = varPrice[1] + "0";
                    PriceCoin = Integer.parseInt(varPrice[1]);
                }
            }
            Price = PriceBill * 100 + PriceCoin;

            Unit = parData.getString("Unit");
            Article =  parData.getString("Article");

            if(parData.has("BarCodes"))
                BarCode.set(parData.getString("BarCodes").replace("\"","").replace("[","").replace("]",""));

            ActionType = 0;
            if(parData.has("ActionType"))
              ActionType = parData.getInt("ActionType");

            PriceBillOpt = 0;
            PriceCoinOpt = 0;
            QuantityOpt = 0;
            Rest = 0;

            if(parData.has("PromotionPrice"))//SevenEleven В ціннику Оптова ціна.
            {
                String[] varPrice = parData.getString("PromotionPrice").replace(',','.').split("\\.");
                PriceBillOpt = Integer.parseInt(varPrice[0]);
                if(varPrice.length ==2) {
                    if (varPrice[1].length() == 1)
                        varPrice[1] = varPrice[1] + "0";
                    PriceCoinOpt = Integer.parseInt(varPrice[1]);
                }
                PriceOpt = PriceBillOpt * 100 + PriceCoinOpt;
                /*if(PriceOpt!=Price)
                {
                    OldPriceOpt = OldPrice;
                    OldPrice = Price;
                }*/

                Price=PriceOpt;
                PriceBillOpt=0;
                PriceCoinOpt=0;
            }

            if (parData.has("QuantityOpt") && parData.has("PriceOpt")) {
                    QuantityOpt = parData.getDouble("QuantityOpt");
                if (QuantityOpt != 0) {
                    String pr=parData.getString("PriceOpt");
                    String[] varPrice = pr.split("\\.");
                    PriceBillOpt = Integer.parseInt(varPrice[0]);
                    if(varPrice[1].length() == 1)
                        varPrice[1] = varPrice[1] + "0";
                    PriceCoinOpt = Integer.parseInt(varPrice[1]);
                }
            }
            if (parData.has("Rest")) {
                Rest = parData.getDouble("Rest");
                NumberOfReplenishment.set(String.valueOf( Rest));

            }

            PriceOpt = PriceBillOpt * 100 + PriceCoinOpt;
            if (Price > 0 && PriceOpt == Price) {
                PriceOpt = 0;
                PriceBillOpt = 0;
                PriceCoinOpt = 0;
            }

            if (parData.has("Sum")) {
                Sum = parData.getDouble("Sum");
            }


        }catch (Exception e){
            e.getMessage();
        }

    }
/*
    public void Init(String parData){
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


    }*/

    public String ToHexZebra( byte[] varByte) {
        StringBuilder sb = new StringBuilder(varByte.length * 3);
        for(byte b: varByte)
            sb.append("_"+String.format("%02x", b));
        return sb.toString();
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
        byte [] DecodeChar=null;
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
            Label=utils.GetStringFromAssetsFile("Label/" + varName_file +"_head"+ ".prn");
        }

        Label=Label+utils.GetStringFromAssetsFile("Label/" + varName_file /*"zpl_"*/ +"_"+ (PriceOpt==0?"1":"2"    ) + ".prn");

        //Name1="АБВГД ЮЯ";
        //Name2=Name1.toLowerCase();
        String BarCode= this.BarCode.get();
        Label=Label.replace("{Name1}",Name1).replace("{Name2}",Name2).
                    replace("{OffsetBill}",OffsetBill).replace("{OffsetCoin}",OffsetCoin).replace("{Unit}",varUnit).
                    replace("{PriceBill}",varPriceBill).replace("{PriceCoin}",strPriceCoin()).
                    replace("{WidthBill}",varWidthBill).
                    replace("{PriceBill2}",varPriceBill2).replace("{PriceCoin2}",strPriceCoinOpt()).
                    replace("{BarCodePrice}",BarCodePrice).replace("{BarCode}",BarCode.substring(0,BarCode.length()>=13?13:BarCode.length())).
                    replace("{Article}",this.Article).replace("{Date}",CurrentDate).
                    replace("{OffsetBill2}",OffsetBill2).replace("{OffsetCoin2}",OffsetCoin2).
                    replace("{OffsetEndLine}",OffsetEndLine).
                    replace("{LabelLength}",LabelLength).replace("{LabelLength_1}",Integer.toString(Integer.parseInt(LabelLength)-1)).
                    replace("{UnitOpt}",UnitOpt).
                    replace("{OffsetUnit}",Integer.toString(Integer.parseInt(OffsetCoin)+80)).
                    replace("{Logo}",GetLogoPicture());//isSpar?"SPAR":"VOPAK"
        ;
        //byte[] ptext = String.getBytes("UTF-8")
        if(parTLP==TypeLanguagePrinter.EZPL) {
            Label=Label.replace("\n","\r\n");
            res = Label.getBytes();//("UTF-8");

        }
          else
            res=Label.getBytes("Cp1251");

        //Магія для кодових сторінок SEWOO в режимі CPCL
        if(parTLP==TypeLanguagePrinter.CPCL_SEWOO)
        {
            if(DecodeChar == null) {
                try {
                    AssetManager aa = config.context.getAssets();
                    InputStream inputStream = config.context.getAssets().open("Label/" + "to_sewoo_lk.map");

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
        String path= Environment.getExternalStorageDirectory()+"/Download/label.prn";
        try (FileOutputStream stream = new FileOutputStream(path)) {
            stream.write(res);
        }
        catch (Exception ex)
        {
            String r=ex.getMessage();
        }
        return res;

    }


}
