package ua.uz.vopak.brb4.brb4.models;

public class DocumentModel {
    public String DateDoc;
    public int TypeDoc;
    public String NumberDoc;
    public String ExtInfo;
    public String NameUser;
    public String BarCode;
    public String Description;
    public String DateInsert;
    // 0 - Редагується,1 - Збережена,9 - Проведена (закрита)
    public int State;
    public int WaresType;
    // 3 - червоний, 2- оранжевий, 1 - жовтий, 0 - зелений, інше грязно жовтий-ранжевий.
    public String GetBackgroundColor()
    {
      /*  if(!DocSetting.IsViewPlan)
            return "fff3cd";*/
        switch (State) {
            case 9:
                return "FFB0B0"; //Червоний
            case 2:
                return "FFC050"; //Оранжевий
            case 0:
                return "FFFF80"; //Жовтий
            case 1:
                return "80FF80"; //Зелений
            default:
                return "fff3cd";
        }
    }
}
