package ua.uz.vopak.brb4.brb4.models;

public class WaresItemModel {
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
    public String BarCode;
    public int BaseCodeUnit;
    public double InputQuantity;
    public String InputQuantity() {return InputQuantity==0.0d ? "": String.format(CodeUnit == 7 ? "%.3f" : "%.0f",InputQuantity);}
    public String GetQuantityBase() {return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",(double)Coefficient*InputQuantity);}
    public double BeforeQuantity;
    public String GetBeforeQuantity() { return String.format(CodeUnit == 7 ? "%.3f" : "%.0f",BeforeQuantity);}

    public double QuantityMin;
    public double QuantityMax;
    public double QuantityOld;

    public WaresItemModel(){};
    public WaresItemModel(DocWaresModel parDW){
        NumberDoc= parDW.Number;
        CodeWares= Integer.valueOf( parDW.CodeWares);
        OrderDoc= Integer.valueOf( parDW.OrderDoc);
        InputQuantity= Double.valueOf(parDW.Quantity);
        QuantityOld = Double.valueOf(parDW.OldQuantity);
        NameWares=parDW.NameWares;
    };
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
        QuantityMin=0 ;
        InputQuantity=0;
        QuantityMax=Integer.MAX_VALUE ;
    }
    public Boolean IsInputQuantity() { return (Coefficient>0);}
}


