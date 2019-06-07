package ua.uz.vopak.brb4.lib.helpers;

import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class PricecheckerHelper {
    private GetDataHTTP Http = new GetDataHTTP();
    public LabelInfo getPriceCheckerData(LabelInfo LI, String BarCode, boolean isHandInput, AbstractConfig config){
        String CodeWares = "";

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
                        break;
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        } else {
            if (BarCode.trim().length() <= 8 && !BarCode.equals("")) {
                CodeWares = BarCode.trim();
            } else
                CodeWares = "";
        }

        String _codeWares = "";
        String _article = "";
        int index = BarCode.indexOf("-");
        String _barCode = !BarCode.equals("") && BarCode.length() > 8 && BarCode.indexOf("-") == -1 ? "\"BarCode\":\"" + BarCode + "\"" : "";

        if (!isHandInput)
            _codeWares = !CodeWares.equals("") ? "\"CodeWares\":\"" + CodeWares + "\"" : "";
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
