package ua.uz.vopak.brb4.lib.models;

public class ParseBarCode {
        //Здійснювати пошук тільки серед штрихкодів (недивлячись на те що штрихкод короткий)
        public boolean IsOnlyBarCode=false;
        public String BarCode=null;
        public int Code=0;
        public double Price=0d;
        public double PriceOpt=0d;
        public String Article =null;

        public double Quantity =0d;;//= Double.parseDouble(Weight) / 1000d;
        public ParseBarCode(){};

}
