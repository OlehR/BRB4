package ua.uz.vopak.brb4.brb4.models;

import android.graphics.Color;

public class


WaresItemModel {
    public String NumberDoc;
    public int TypeDoc;
    public int OrderDoc;
    public String GetOrderDoc(){return String.valueOf(OrderDoc);}
    public int CodeWares;
    public String GetCodeWares(){return String.valueOf(CodeWares);}
    public String NameWares;
    public int Coefficient;
    public String GetCoefficient(){return String.valueOf(Coefficient);}
    public int CodeUnit;
    public String NameUnit;
    public String GetNameUnit(){return NameUnit+ "X";}
    public String BarCode;
    public int BaseCodeUnit;
    public double InputQuantity;
    public String GetInputQuantity() {return InputQuantity==0.0d ? "": String.format(CodeUnit == 7 ? "%.3f" : "%.0f",InputQuantity);}
    public String GetInputQuantityZero() {return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",InputQuantity);}
    public String GetQuantityBase() {return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",(double)Coefficient*InputQuantity);}
    public double BeforeQuantity;
    public String GetBeforeQuantity() { return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",BeforeQuantity)+ (QuantityMax==Double.MAX_VALUE?"":"/"+String.format(CodeUnit == 7 ? "%.3f" : "%.0f",BeforeQuantity))   ;}

    public int ColorBackground(){return Color.parseColor(QuantityMax>0d ? "#ffffff" : "#3fffff00");}

    //public boolean IsInput(){return QuantityMax>0d;}

    public double QuantityMin;
    public double QuantityMax;
    public double QuantityOld;
    public double QuantityOrder;
    public String GetQuantityOrder() { return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",QuantityOrder);}

    public WaresItemModel(){};

    public String GetQuantityOld(){return String.valueOf(QuantityOld);}
  public void Set(WaresItemModel parWIM)
  {
      if(parWIM.NumberDoc!=null && parWIM.TypeDoc>0)
      {
          NumberDoc=parWIM.NumberDoc;
          TypeDoc=parWIM.TypeDoc;
      }
      if(parWIM.OrderDoc>0)
          OrderDoc=parWIM.OrderDoc;

      CodeWares=parWIM.CodeWares ;
      NameWares=parWIM.NameWares ;
      Coefficient=parWIM.Coefficient ;
      CodeUnit=parWIM.CodeUnit ;
      NameUnit=parWIM.NameUnit ;
      BarCode=parWIM.BarCode ;
      BaseCodeUnit =parWIM.BaseCodeUnit;
      QuantityMin=parWIM.QuantityMin ;
      QuantityMax=parWIM.QuantityMax ;
      
  }

    public void ClearData(String parNameWares)
    {
        ClearData();
        NameWares=parNameWares;
    }
    public void ClearData()
    {
        CodeWares=0 ;
        NameWares="" ;
        Coefficient=0;
        CodeUnit=0;
        NameUnit="" ;
        BarCode="" ;
        BaseCodeUnit =0;
        BeforeQuantity=0;
        QuantityMin=0 ;
        InputQuantity=0;
        QuantityMax=Double.MAX_VALUE ;

    }
    public Boolean IsInputQuantity() { return (Coefficient>0 && QuantityMax>0d);}
}


