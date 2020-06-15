package ua.uz.vopak.brb4.lib.helpers;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class PricecheckerHelper {
    private GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo getPriceCheckerData(LabelInfo LI, String BarCode, boolean isHandInput, AbstractConfig config) {
        if(config.Company== eCompany.SparPSU||config.Company==eCompany.VopakPSU)
            return getPriceCheckerDataPSU(  LI,  BarCode,  isHandInput,  config);
        else if(config.Company== eCompany.SevenEleven)
            return getPriceCheckerDataSevenEleven(LI,  BarCode,  isHandInput,  config);
        return LI;
    }

    public LabelInfo getPriceCheckerDataPSU(LabelInfo LI, String BarCode, boolean isHandInput, AbstractConfig config){
        String CodeWares = "";

        LI.OldPrice =0;
        if (BarCode.indexOf('-') > 0) {
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
                        BarCode="";
                        break;
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        } else {
            if ((BarCode.trim().length() < 8 && !BarCode.equals("")) || (BarCode.trim().length() == 8 && BarCode.trim().substring(0,2).equals("00") ))
            {
                CodeWares = BarCode.trim();
                BarCode="";
            } else
                CodeWares = "";
        }

        String _codeWares = "";
        String _article = "";
        String _barCode="";
        //int index = BarCode.indexOf("-");
        if( !BarCode.equals(""))
         _barCode =  "\"BarCode\":\"" + BarCode + "\"" ;

        if(!CodeWares.equals(""))
         if (isHandInput)
             _article = "\"Article\":\"" + CodeWares + "\"";
         else
             _codeWares ="\"CodeWares\":\"" + CodeWares + "\"" ;

        String data = config.GetApiJson(154, _barCode + _codeWares + _article);
        LI.resHttp = Http.HTTPRequest(config.getApiUrl(), data);
        //resHttp = resHttp.replace("&amp;", "&");
        //Call Progres 50%;
        //LI.InfoHTTP = Http.HttpState.name();
        LI.HttpState=Http.HttpState;

        return LI;
    }


    public LabelInfo getPriceCheckerDataSevenEleven(LabelInfo LI, String BarCode, boolean isHandInput, AbstractConfig config){
        String vCode = null;
        LI.OldPrice =0;

        if (BarCode.length()>2 && BarCode.substring(0,2).equals("29") && BarCode.length()==13 ) {
            vCode="code="+BarCode.substring(2,8);
            LI.OldPrice=Integer.valueOf( BarCode.substring(8,13));
            }
            else
             vCode="BarCode="+BarCode.trim();

        LI.resHttp = Http.HTTPRequest(config.ApiUrl+"PriceTagInfo?"+vCode, null);
        LI.HttpState=Http.HttpState;
        return LI;
    }
    public String HttpState() {return Http.HttpState.name();}
}
