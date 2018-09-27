package ua.uz.vopak.brb4.brb4;

public class LabelInfo
{
    public int Code;
    public String Name;
    public String Name1;
    public String Name2;
    public int Price;
    public int PriceBill;
    public int PriceCoin;
    public String Unit;
    public String Article;
    public String BarCode;
    public boolean Action  = false;
    public LabelInfo(String parData)
    {
        String [] varData = parData.split(";");
        if(varData.length<5)
            return;
        Code = Integer.parseInt(varData[0]);
        Name = varData[1];
        String [] varPrice =varData[2].split(",");
        PriceBill = Integer.parseInt(varPrice[0]);
        PriceCoin = Integer.parseInt(varPrice[1]);
        Unit = varData[3];
        Article = varData[4];
        BarCode = varData[5];
        if(varData[6]=="1")
            Action  = true;

    }
}
