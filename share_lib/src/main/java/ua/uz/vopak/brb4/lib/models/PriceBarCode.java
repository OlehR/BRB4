package ua.uz.vopak.brb4.lib.models;

import android.util.Log;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.helpers.AbstractConfig;

public class PriceBarCode {
    public String BarCode;
    public int Code=0;
    public double Price=0d;
    public double PriceOpt=0d;
    public PriceBarCode(){};

    public PriceBarCode(String pBarCode, eCompany pCompany)
    {
        BarCode=pBarCode;
        if(pCompany== eCompany.SevenEleven)
        {
            if(pBarCode.substring(0,2).equals("29") && pBarCode.length()==13)
            {
                try {
                    Code = Integer.parseInt(pBarCode.substring(2, 8));
                    Price = Double.valueOf(pBarCode.substring(8, 13)) / 100d;
                }
                catch (Exception e){
                    Log.e("PriceBarCode",e.getMessage());
                };
            }
        }
    }
}
