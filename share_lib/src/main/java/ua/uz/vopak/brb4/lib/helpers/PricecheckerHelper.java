package ua.uz.vopak.brb4.lib.helpers;

import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class PricecheckerHelper {
    private GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo getPriceCheckerData(LabelInfo LI, String BarCode, boolean isHandInput, AbstractConfig config){
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
         if (!isHandInput)
            _codeWares ="\"CodeWares\":\"" + CodeWares + "\"" ;
         else
            _article = "\"Article\":\"" + CodeWares + "\"";

        String data = config.GetApiJson(154, _barCode + _codeWares + _article);
        LI.resHttp = Http.HTTPRequest(config.getApiUrl(), data);
        //resHttp = resHttp.replace("&amp;", "&");
        //Call Progres 50%;
        LI.InfoHTTP = Http.HttpState.name();

        return LI;
    }
}