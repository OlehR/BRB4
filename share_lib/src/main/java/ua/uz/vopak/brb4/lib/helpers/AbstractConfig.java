package ua.uz.vopak.brb4.lib.helpers;

import android.content.Context;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;

import ua.uz.vopak.brb4.lib.enums.eCompany;
import ua.uz.vopak.brb4.lib.enums.eTypeScaner;
import ua.uz.vopak.brb4.lib.enums.eTypeUsePrinter;

import static android.os.Build.USER;

public abstract class AbstractConfig {

    protected static AbstractConfig Instance = null;
    public Boolean IsDebug = false;
    public Context context;
    public int CodeWarehouse = 0;
    public eCompany Company = eCompany.NotDefined;
    public boolean IsOnline=false;

    public String SN;
    public String NameDCT = USER;

    public boolean isAutorized;
    public String Login = "";
    public String Password = "123";

    public Date LastFullUpdate=null;
    public SimpleDateFormat FormatterDate = new SimpleDateFormat("yyyy-MM-dd");

    public int GetCodeUnitWeight() { return Company == eCompany.Sim23 ?  166:7;}

    public int GetCodeUnitPiece() { return Company == eCompany.Sim23 ?  796:19;}

    public String ApiUrl,ApiURLadd,ApiUrl3="http://qlik.sim23.ua:2380/1c/hs/UTP/";

    public eTypeScaner TypeScaner = eTypeScaner.NotDefine;
    public eTypeUsePrinter TypeUsePrinter;

    /// Взагалі йому тут не місце;
    public Integer NumberPackege = 0;
    //public Integer LogOrder = 0;

    //Номер сканування цінників за день
    public int LineNumber=0;
    public  Utils cUtils;

    public AbstractConfig(){};
    public AbstractConfig(String url){
        this.ApiUrl = url;
    }
    public void Init(Context pContext) {
        context = pContext;
        cUtils=new Utils(context);
        SN= cUtils.GetSN();
        //Визначаємо тип Сканера
        TypeScaner =cUtils.GetTypeScaner();
    }

    public String GetApiJson(int parCodeData, int VersionCode, String parData) {
        return "{\"CodeData\":" + parCodeData + ",\"SerialNumber\":\"" + SN + "\",\"NameDCT\":\"" + NameDCT + "\", \"Ver\":\"" + VersionCode + "\", \"CodeWarehouse\":\"" + this.getCodeWarehouse() + "\", \"Login\": \"" + Login + "\",\"PassWord\": \"" + Password + "\"" +
                (parData == null || parData =="" ? "" : "," + parData) + "}";
    }

    public String getCodeWarehouse() {
        String code = "000000000" + CodeWarehouse;
        return code.substring(code.length() - 9);
    }

    public String getApiUrl(){
        return this.ApiUrl;
    }

    public static AbstractConfig instance() //throws Exception
    {
        /*if (Instance == null ){
            throw new Exception("Відсутній екземпляр AbstractConfig");
        }*/
        return  Instance;
    }

}
