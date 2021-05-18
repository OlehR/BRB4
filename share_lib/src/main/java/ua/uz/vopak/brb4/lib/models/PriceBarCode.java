package ua.uz.vopak.brb4.lib.models;

import android.util.Log;

import ua.uz.vopak.brb4.lib.enums.eCompany;

public class PriceBarCode {
    public String BarCode;
    public int Code=0;
    public double Price=0d;
    public double PriceOpt=0d;
    public String Article =null;
    public double QuantityBarCode ;//= Double.parseDouble(Weight) / 1000d;
    public PriceBarCode(){};

    public PriceBarCode(String pBarCode, eCompany pCompany)
    {
        BarCode=pBarCode;
        if(pCompany== eCompany.Sim23)
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
        else if((pCompany== eCompany.SparPSU || pCompany== eCompany.VopakPSU) && pBarCode!=null && pBarCode.contains("-") )
        {
            try {
                String[] str = BarCode.split("-");
                switch (str.length) {
                    case 3:
                        PriceOpt = Integer.parseInt(str[2])/ 100d;
                    case 2:
                        Price = Integer.parseInt(str[1])/100d;
                        Code = Integer.parseInt(str[0]);
                        BarCode="";
                        break;
                }
            } catch (Exception e) {
                Log.e("PriceBarCode",e.getMessage());
            }

        }



    }
}
