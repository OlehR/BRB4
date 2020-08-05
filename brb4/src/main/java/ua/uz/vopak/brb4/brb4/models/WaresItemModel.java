package ua.uz.vopak.brb4.brb4.models;

import android.graphics.Color;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableInt;

import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.models.LabelInfo;

public class WaresItemModel implements Cloneable{
    GlobalConfig config = GlobalConfig.instance();
    public String NumberDoc;
    public int TypeDoc;
    public DocSetting DocSetting;
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
    public boolean IsRecord =false;
    public String GetInputQuantity() {return InputQuantity==0.0d ? "": String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",InputQuantity);}
    public String GetInputQuantityZero() {return String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",InputQuantity);}
    public String GetQuantityBase() {return String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",(double)Coefficient*InputQuantity);}
    public double BeforeQuantity;
    public String GetBeforeQuantity() {
        return String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",BeforeQuantity)+ (QuantityMax==Double.MAX_VALUE?"":"/"+String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",BeforeQuantity)) +
                (QuantityOrder>0 && QuantityMax==Double.MAX_VALUE?String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",QuantityOrder):"")  ;}

    public int ColorBackground(){return Color.parseColor(QuantityMax>0d ? "#ffffff" : "#3fffff00");}
    public boolean IsUseCamera()  {return config.TypeScaner== eTypeScaner.Camera;}

    //public boolean IsInput(){return QuantityMax>0d;}

    public double QuantityMin;
    public double QuantityMax;
    public double QuantityOld;
    public double QuantityOrder;
    public double QuantityReason;
    public double QuantityBarCode;
    public int CodeReason;
    // Лоти  3- недостача. //2 - надлишок, // 1 - є з причиною // 0 - все ОК.
    // Ревізія. // 0- Зеленим кольором пораховані, 2- оранжевим додані вручну, 0- жовтим непораховані.
    public int Ord;

    // 3 - червоний, 2- оранжевий, 1 - жовтий, 0 - зелений, інше грязно жовтий-ранжевий.
    public String GetBackgroundColor()
    {
      /*  if(!DocSetting.IsViewPlan)
            return "fff3cd";*/
        switch (Ord) {
            case 3:
                return "FFB0B0";
            case 2:
                return "FFC050";
            case 1:
                return "FFFF80";
            case 0:
                return "80FF80";
            default:
                return "fff3cd";
        }
    }


    public ObservableArrayList<String> ListReason = new ObservableArrayList<>();
    public ObservableInt ListReasonIdx = new ObservableInt(0);

    public boolean GetIsViewReason(){return DocSetting.IsViewReason;}
    //public boolean IsViewReason=false;
    //public boolean IsViewPlan=false;


    public String GetQuantityOrder() { return String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",QuantityOrder);}
    public String GetQuantityReason() { return String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",QuantityReason);}

    public  WaresItemModel(){ClearData();}
   // public  WaresItemModel(WaresItemModel p){return (WaresItemModel)clone(p);}

    public String GetQuantityOld(){return  QuantityOld==0.0d ? "": String.format(CodeUnit == config.GetCodeUnitWeight() ? "%.3f" : "%.0f",QuantityOld);}
    public void Set(WaresItemModel parWIM){
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
      CodeReason= parWIM.CodeReason;
      QuantityBarCode=parWIM.QuantityBarCode;
      
  }

    public void ClearData(String parNameWares){
        ClearData();
        NameWares=parNameWares;
    }
    public void ClearData(){
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
        CodeReason=0;

    }
    public Boolean IsInputQuantity() { return (Coefficient>0 && QuantityMax>0d);}
    public Boolean IsInputQuantityTouch() { return IsInputQuantity() && IsUseCamera();}
    public Boolean IsInputBarCodeTouch() { return !IsInputQuantity() && IsUseCamera();}

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // Трохи через Ж
    public void SetLI(LabelInfo pLI)
    {
        pLI.Code= CodeWares;
        pLI.Name=NameWares;
        pLI.Unit =NameUnit;
        pLI.BarCode.set(BarCode);
    }

}


