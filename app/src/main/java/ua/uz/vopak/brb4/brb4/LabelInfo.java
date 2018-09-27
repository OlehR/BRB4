package ua.uz.vopak.brb4.brb4;

import java.io.UnsupportedEncodingException;

public class LabelInfo
{
    public int Code;
    public String Name;
    public int Price;
    public int OldPrice;
    public int PriceBill;
    public int PriceCoin;
    public String Unit;
    public String Article;
    public String BarCode;
    public boolean Action  = false;
    public LabelInfo()
    {
    }
    public LabelInfo(String parData)
    {
     Init(parData);
    }

    public void Init(String parData)
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
    public byte[] LevelForPrinter() throws UnsupportedEncodingException {
        byte [] res;
        String Label="^XA\n" +
                "^LL280\n" +
                "^FO0,12^A@N,20,20,B:904_MSSS_24.arf\n" +
                "^FDВино \"La Famiglia\"\n" +
                "^FS\n" +
                "\n" +
                "^FO0,40^A@N,20,20,B:904_MSSS_24.arf\n" +
                "^FD Delicato біле н/сол 0.75л       \n" +
                "^FS\n" +
                "\n" +
                "^FO  10,18^A@N,20,20,B:903_AB_120.arf\n" +
                "^FD155\n" +
                "^FS\n" +
                "\n" +
                "^FO335  ,51^A@N,20,20,B:901_AB_60.arf\n" +
                "^FD37\n" +
                "^FS\n" +
                "\n" +
                "^FO240,215^Ab\n" +
                "^FD21.09.2018  \n" +
                "^FS\n" +
                "\n" +
                "\n" +
                "^FO248,240^Ab\n" +
                "^FD00093272 \n" +
                "^FS\n" +
                "\n" +
                "^FO330,140^A@N,20,20,B:904_MSSS_24.arf\n" +
                "^FD грн/пл                                      \n" +
                "^FS\n" +
                "\n" +
                "^FO0,247^A@N,20,20,B:904_MSSS_24.arf\n" +
                "^FD-------------------------------------\n" +
                "^FS\n" +
                "\n" +
                "\n" +
                "\n" +
                "^FO340,180^BY3\n" +
                "^BQN,2,4^FDMM,N299123456123456^FS\n" +
                "\n" +
                "^FO15,200^BY2\n" +
                "^BEN,40,Y,N\n" +
                "^FD3083680015394\n" +
                "^FS\n" +
                "\n" +
                "\n" +
                "\n" +
                "^XZ\n";
           res=Label.getBytes("Cp1251");
          return res;

    }

}
