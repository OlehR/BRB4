package ua.uz.vopak.brb4.brb4.models;

public class WaresItemModel {
    public String NumberDoc;
    int TypeDoc;
    public int OrderDoc;
    public int CodeWares;
    public String NameWares;
    public int Coefficient;
    public String GetCoefficient(){return String.valueOf(Coefficient);}
    public int CodeUnit;
    public String NameUnit;
    public String BarCode;
    public int BaseCodeUnit;
    public double InputQuantity;
    public double GetQuantityBase () {return (double)Coefficient*InputQuantity;}
    public double BeforeQuantity;
    public String GetBeforeQuantity() { return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",BeforeQuantity);}


    public int QuantityMin;
    public int QuantityMax;
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

    public void ClearData()
    {
        CodeWares=0 ;
        NameWares="" ;
        Coefficient=0;
        CodeUnit=0;
        NameUnit="" ;
        BarCode="" ;
        BaseCodeUnit =0;
        QuantityMin=0 ;
        QuantityMax=Integer.MAX_VALUE ;
    }
}


